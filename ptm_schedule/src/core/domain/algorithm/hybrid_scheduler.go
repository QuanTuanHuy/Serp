/*
Author: QuanTuanHuy
Description: Part of Serp Project - Hybrid Scheduler (Greedy Insertion + Ripple Effect)
*/

package algorithm

import (
	"sort"
)

const (
	DefaultMinChunkDuration = 30
)

type HybridScheduler struct {
	scoring   *ScoringService
	slotUtils *TimeSlotUtils
}

func NewHybridScheduler() *HybridScheduler {
	return &HybridScheduler{
		scoring:   NewScoringService(),
		slotUtils: NewTimeSlotUtils(),
	}
}

// Schedule is the main entry point for scheduling tasks
func (s *HybridScheduler) Schedule(input *ScheduleInput) *ScheduleOutput {
	output := NewScheduleOutput()

	// 1. Separate pinned tasks (already scheduled, don't move)
	pinnedAssignments := s.extractPinnedAssignments(input.Tasks, input.ExistingEvents)

	// 2. Sort tasks by score (highest first)
	sortedTasks := s.sortTasksByScore(input.Tasks)

	// 3. Initialize current schedule with pinned events
	currentSchedule := make([]*Assignment, len(pinnedAssignments))
	copy(currentSchedule, pinnedAssignments)

	// 4. Task lookup map for ripple effect
	taskMap := s.buildTaskMap(sortedTasks)

	// 5. Main scheduling loop
	for _, task := range sortedTasks {
		// Skip pinned tasks (already in schedule)
		if task.IsPinned {
			continue
		}

		// Step A: Try greedy insertion
		inserted := s.tryInsertTask(task, &currentSchedule, input.Windows)

		// Step B: If failed and critical, try ripple effect
		if !inserted && s.scoring.IsCriticalTask(task) {
			inserted = s.tryRippleEffect(task, &currentSchedule, input.Windows, taskMap)
		}

		// Step C: Record if unscheduled
		if !inserted {
			output.UnscheduledTasks = append(output.UnscheduledTasks, &UnscheduledTask{
				TaskID: task.TaskID,
				Reason: "No available time slot found",
			})
		}
	}

	output.Assignments = currentSchedule
	output.Metrics = s.calculateMetrics(sortedTasks, currentSchedule, output.UnscheduledTasks)

	return output
}

// ScheduleIncremental handles adding new tasks to existing schedule
func (s *HybridScheduler) ScheduleIncremental(input *ScheduleInput, newTaskIDs []int64) *ScheduleOutput {
	output := NewScheduleOutput()

	// Identify ScheduleTaskIDs to exclude from existing events
	// Include all newTaskIDs directly (handles both existing tasks with changed constraints
	// AND deleted tasks whose events should be excluded)
	affectedTaskIDs := make(map[int64]bool)
	for _, id := range newTaskIDs {
		affectedTaskIDs[id] = true
	}

	// Start with existing events, excluding those belonging to affected tasks
	currentSchedule := make([]*Assignment, 0, len(input.ExistingEvents))
	for _, e := range input.ExistingEvents {
		if !affectedTaskIDs[e.ScheduleTaskID] {
			currentSchedule = append(currentSchedule, e)
		}
	}

	// Filter to only new tasks (tasks that still exist and need rescheduling)
	newTasks := s.filterTasksByIDs(input.Tasks, newTaskIDs)
	sortedNewTasks := s.sortTasksByScore(newTasks)

	taskMap := s.buildTaskMap(input.Tasks)

	for _, task := range sortedNewTasks {
		if task.IsPinned {
			continue
		}

		inserted := s.tryInsertTask(task, &currentSchedule, input.Windows)

		if !inserted && s.scoring.IsCriticalTask(task) {
			inserted = s.tryRippleEffect(task, &currentSchedule, input.Windows, taskMap)
		}

		if !inserted {
			output.UnscheduledTasks = append(output.UnscheduledTasks, &UnscheduledTask{
				TaskID: task.TaskID,
				Reason: "No available time slot found",
			})
		}
	}

	output.Assignments = currentSchedule
	output.Metrics = s.calculateMetrics(input.Tasks, currentSchedule, output.UnscheduledTasks)

	return output
}

// tryInsertTask attempts greedy insertion of a task
func (s *HybridScheduler) tryInsertTask(task *TaskInput, schedule *[]*Assignment, windows []*Window) bool {
	// Calculate current gaps
	gaps := s.slotUtils.CalculateGaps(windows, *schedule)

	// Sort gaps by window score for this task
	sortedGaps := s.sortGapsByScore(gaps, task)

	remainingDuration := task.DurationMin
	tempAssignments := make([]*Assignment, 0)
	partIndex := 1

	minChunk := task.MinSplitMin
	if minChunk <= 0 {
		minChunk = DefaultMinChunkDuration
	}

	for _, gap := range sortedGaps {
		if remainingDuration <= 0 {
			break
		}

		// Check deadline constraint
		if task.DeadlineMs != nil && gap.DateMs > *task.DeadlineMs {
			continue
		}

		// Check earliest start constraint
		if task.EarliestStartMs != nil && gap.DateMs < *task.EarliestStartMs {
			continue
		}

		gapDuration := gap.Duration()

		// Calculate allocatable duration (considering buffer)
		effectiveGap := gapDuration
		if partIndex == 1 && task.BufferBeforeMin > 0 {
			effectiveGap -= task.BufferBeforeMin
		}

		allocatable := min(effectiveGap, remainingDuration)

		// Skip if chunk too small (unless it's the final piece)
		if allocatable < minChunk && allocatable < remainingDuration {
			continue
		}

		// Skip if task doesn't allow splitting but gap is too small
		if !task.AllowSplit && gapDuration < task.DurationMin {
			continue
		}

		// Check max split count
		if task.MaxSplitCount > 0 && partIndex > task.MaxSplitCount {
			continue
		}

		// Calculate actual start/end with buffer
		startMin := gap.StartMin
		if partIndex == 1 && task.BufferBeforeMin > 0 {
			startMin += task.BufferBeforeMin
		}
		endMin := startMin + allocatable

		// Create temporary assignment
		assignment := &Assignment{
			TaskID:         task.TaskID,
			ScheduleTaskID: task.ScheduleTaskID,
			DateMs:         gap.DateMs,
			StartMin:       startMin,
			EndMin:         endMin,
			PartIndex:      partIndex,
			IsPinned:       false,
			UtilityScore:   s.scoring.CalculateTaskScore(task),
			Title:          task.Title,
		}

		tempAssignments = append(tempAssignments, assignment)
		remainingDuration -= allocatable
		partIndex++
	}

	// Check if fully scheduled
	if remainingDuration <= 0 {
		// Update TotalParts for all assignments
		totalParts := len(tempAssignments)
		for _, a := range tempAssignments {
			a.TotalParts = totalParts
		}

		// Commit to schedule
		*schedule = append(*schedule, tempAssignments...)
		return true
	}

	return false
}

// tryRippleEffect attempts to displace lower-priority tasks
func (s *HybridScheduler) tryRippleEffect(
	urgentTask *TaskInput,
	schedule *[]*Assignment,
	windows []*Window,
	taskMap map[int64]*TaskInput,
) bool {
	// Find best window for this task (ignoring existing assignments)
	targetWindow := s.findBestWindow(urgentTask, windows)
	if targetWindow == nil {
		return false
	}

	// Calculate where we want to place the urgent task
	startMin := targetWindow.StartMin
	if urgentTask.BufferBeforeMin > 0 {
		startMin += urgentTask.BufferBeforeMin
	}
	endMin := startMin + urgentTask.DurationMin

	// Check if window is large enough
	if endMin > targetWindow.EndMin {
		return false
	}

	// Find victims (assignments that overlap with target slot)
	conflicts := s.slotUtils.FindConflictingAssignments(*schedule, targetWindow.DateMs, startMin, endMin)

	// Check if we can displace all victims
	urgentScore := s.scoring.CalculateTaskScore(urgentTask)
	victimsToDisplace := make(map[int64]bool) // scheduleTaskID -> should displace

	for _, conflict := range conflicts {
		// Cannot displace pinned events
		if conflict.IsPinned {
			return false
		}

		// Cannot displace higher priority tasks
		if conflict.UtilityScore >= urgentScore {
			return false
		}

		// Mark entire task (all parts) for displacement
		victimsToDisplace[conflict.ScheduleTaskID] = true
	}

	// Build new schedule without victims
	keptAssignments := make([]*Assignment, 0)
	for _, a := range *schedule {
		if !victimsToDisplace[a.ScheduleTaskID] {
			keptAssignments = append(keptAssignments, a)
		}
	}

	// Add urgent task assignment
	urgentAssignment := &Assignment{
		TaskID:         urgentTask.TaskID,
		ScheduleTaskID: urgentTask.ScheduleTaskID,
		DateMs:         targetWindow.DateMs,
		StartMin:       startMin,
		EndMin:         endMin,
		PartIndex:      1,
		TotalParts:     1,
		IsPinned:       false,
		UtilityScore:   urgentScore,
		Title:          urgentTask.Title,
	}
	keptAssignments = append(keptAssignments, urgentAssignment)

	// Try to reschedule victims
	victimTasks := make([]*TaskInput, 0)
	for scheduleTaskID := range victimsToDisplace {
		if task, ok := taskMap[scheduleTaskID]; ok {
			victimTasks = append(victimTasks, task)
		}
	}

	// Sort victims by priority (highest first for best chance)
	sort.Slice(victimTasks, func(i, j int) bool {
		return s.scoring.CalculateTaskScore(victimTasks[i]) > s.scoring.CalculateTaskScore(victimTasks[j])
	})

	// Try to reinsert each victim
	for _, victim := range victimTasks {
		success := s.tryInsertTask(victim, &keptAssignments, windows)
		if !success {
			// Ripple failed - cannot reschedule victim
			// Rollback by returning false (original schedule unchanged)
			return false
		}
	}

	// Success - update schedule
	*schedule = keptAssignments
	return true
}

// Helper methods

func (s *HybridScheduler) extractPinnedAssignments(_ []*TaskInput, existing []*Assignment) []*Assignment {
	pinned := make([]*Assignment, 0)
	for _, a := range existing {
		if a.IsPinned {
			pinned = append(pinned, a)
		}
	}
	return pinned
}

func (s *HybridScheduler) sortTasksByScore(tasks []*TaskInput) []*TaskInput {
	sorted := make([]*TaskInput, len(tasks))
	copy(sorted, tasks)
	sort.Slice(sorted, func(i, j int) bool {
		return s.scoring.CalculateTaskScore(sorted[i]) > s.scoring.CalculateTaskScore(sorted[j])
	})
	return sorted
}

func (s *HybridScheduler) sortGapsByScore(gaps []*Window, task *TaskInput) []*Window {
	sorted := make([]*Window, len(gaps))
	copy(sorted, gaps)
	sort.Slice(sorted, func(i, j int) bool {
		return s.scoring.ScoreWindow(sorted[i], task) > s.scoring.ScoreWindow(sorted[j], task)
	})
	return sorted
}

func (s *HybridScheduler) buildTaskMap(tasks []*TaskInput) map[int64]*TaskInput {
	m := make(map[int64]*TaskInput)
	for _, t := range tasks {
		m[t.ScheduleTaskID] = t
	}
	return m
}

func (s *HybridScheduler) filterTasksByIDs(tasks []*TaskInput, ids []int64) []*TaskInput {
	idSet := make(map[int64]bool)
	for _, id := range ids {
		idSet[id] = true
	}

	result := make([]*TaskInput, 0)
	for _, t := range tasks {
		if idSet[t.ScheduleTaskID] {
			result = append(result, t)
		}
	}
	return result
}

func (s *HybridScheduler) findBestWindow(task *TaskInput, windows []*Window) *Window {
	var best *Window
	bestScore := -1.0

	for _, w := range windows {
		// Check basic constraints
		if w.Duration() < task.DurationMin+task.BufferBeforeMin {
			continue
		}
		if task.DeadlineMs != nil && w.DateMs > *task.DeadlineMs {
			continue
		}
		if task.EarliestStartMs != nil && w.DateMs < *task.EarliestStartMs {
			continue
		}

		score := s.scoring.ScoreWindow(w, task)
		if score > bestScore {
			bestScore = score
			best = w
		}
	}

	return best
}

func (s *HybridScheduler) calculateMetrics(
	allTasks []*TaskInput,
	scheduled []*Assignment,
	unscheduled []*UnscheduledTask,
) *ScheduleMetrics {
	totalDuration := 0
	for _, t := range allTasks {
		totalDuration += t.DurationMin
	}

	usedDuration := 0
	for _, a := range scheduled {
		usedDuration += a.Duration()
	}

	utilization := 0.0
	if totalDuration > 0 {
		utilization = float64(usedDuration) / float64(totalDuration) * 100
	}

	return &ScheduleMetrics{
		TotalTasks:       len(allTasks),
		ScheduledTasks:   len(allTasks) - len(unscheduled),
		UnscheduledTasks: len(unscheduled),
		TotalDurationMin: totalDuration,
		UsedDurationMin:  usedDuration,
		UtilizationPct:   utilization,
	}
}
