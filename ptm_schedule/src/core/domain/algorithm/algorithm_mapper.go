/*
Author: QuanTuanHuy
Description: Part of Serp Project - Mapper between domain entities and algorithm DTOs
*/

package algorithm

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/optimization"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

type AlgorithmMapper struct{}

func NewAlgorithmMapper() *AlgorithmMapper {
	return &AlgorithmMapper{}
}

// TaskInput conversion

func (m *AlgorithmMapper) TaskEntityToInput(task *entity.ScheduleTaskEntity) *TaskInput {
	return &TaskInput{
		TaskID:          task.TaskID,
		ScheduleTaskID:  task.ID,
		Title:           task.Title,
		DurationMin:     task.DurationMin,
		Priority:        task.Priority,
		PriorityScore:   task.PriorityScore,
		IsDeepWork:      task.IsDeepWork,
		EarliestStartMs: task.EarliestStartMs,
		DeadlineMs:      task.DeadlineMs,
		AllowSplit:      task.AllowSplit,
		MinSplitMin:     task.MinSplitDurationMin,
		MaxSplitCount:   task.MaxSplitCount,
		IsPinned:        task.IsPinned,
		IsCompleted:     task.IsCompleted(),
		BufferBeforeMin: task.BufferBeforeMin,
	}
}

func (m *AlgorithmMapper) TaskEntitiesToInputs(tasks []*entity.ScheduleTaskEntity) []*TaskInput {
	inputs := make([]*TaskInput, len(tasks))
	for i, t := range tasks {
		inputs[i] = m.TaskEntityToInput(t)
	}
	return inputs
}

// Window conversion

func (m *AlgorithmMapper) WindowEntityToWindow(window *entity.ScheduleWindowEntity) *Window {
	return &Window{
		DateMs:     window.DateMs,
		StartMin:   window.StartMin,
		EndMin:     window.EndMin,
		IsDeepWork: false, // Default, can be enhanced based on user preferences
	}
}

func (m *AlgorithmMapper) WindowEntitiesToWindows(windows []*entity.ScheduleWindowEntity) []*Window {
	result := make([]*Window, len(windows))
	for i, w := range windows {
		result[i] = m.WindowEntityToWindow(w)
	}
	return result
}

// Assignment conversion

// EventEntityToAssignmentWithTask converts with proper TaskID from ScheduleTaskEntity
func (m *AlgorithmMapper) EventEntityToAssignmentWithTask(event *entity.ScheduleEventEntity, task *entity.ScheduleTaskEntity) *Assignment {
	var utilityScore float64
	if event.UtilityScore != nil {
		utilityScore = *event.UtilityScore
	}

	taskID := int64(0)
	if task != nil {
		taskID = task.TaskID
	}

	var status *string
	if event.Status != "" {
		statusStr := string(event.Status)
		status = &statusStr
	}

	return &Assignment{
		EventID:        &event.ID,
		TaskID:         taskID,
		ScheduleTaskID: event.ScheduleTaskID,
		DateMs:         event.DateMs,
		StartMin:       event.StartMin,
		EndMin:         event.EndMin,
		PartIndex:      event.PartIndex,
		TotalParts:     event.TotalParts,
		IsPinned:       event.IsPinned,
		Status:         status,
		UtilityScore:   utilityScore,
		Title:          event.Title,
	}
}

// EventEntitiesToAssignmentsWithTaskMap converts events with proper TaskID resolution
func (m *AlgorithmMapper) EventEntitiesToAssignmentsWithTaskMap(
	events []*entity.ScheduleEventEntity,
	taskMap map[int64]*entity.ScheduleTaskEntity,
) []*Assignment {
	result := make([]*Assignment, len(events))
	for i, e := range events {
		task := taskMap[e.ScheduleTaskID]
		result[i] = m.EventEntityToAssignmentWithTask(e, task)
	}
	return result
}

// Assignment back to EventEntity (for creating new events from schedule output)

func (m *AlgorithmMapper) AssignmentToEventEntity(
	assignment *Assignment,
	planID int64,
	task *entity.ScheduleTaskEntity,
) *entity.ScheduleEventEntity {
	event := entity.NewScheduleEvent(
		planID,
		task.ID,
		assignment.DateMs,
		assignment.StartMin,
		assignment.EndMin,
		assignment.Title,
	)

	event.PartIndex = assignment.PartIndex
	event.TotalParts = assignment.TotalParts
	event.IsPinned = assignment.IsPinned
	event.UtilityScore = &assignment.UtilityScore
	event.Status = enum.ScheduleEventPlanned

	return event
}

func (m *AlgorithmMapper) AssignmentsToEventEntities(
	assignments []*Assignment,
	planID int64,
	taskMap map[int64]*entity.ScheduleTaskEntity, // scheduleTaskID -> ScheduleTaskEntity
) []*entity.ScheduleEventEntity {
	result := make([]*entity.ScheduleEventEntity, 0, len(assignments))
	for _, a := range assignments {
		task, ok := taskMap[a.ScheduleTaskID]
		if !ok {
			continue
		}
		result = append(result, m.AssignmentToEventEntity(a, planID, task))
	}
	return result
}

// ScheduleInput builder from entities

func (m *AlgorithmMapper) BuildScheduleInput(
	tasks []*entity.ScheduleTaskEntity,
	windows []*entity.ScheduleWindowEntity,
	existingEvents []*entity.ScheduleEventEntity,
) *ScheduleInput {
	taskMap := make(map[int64]*entity.ScheduleTaskEntity)
	for _, t := range tasks {
		taskMap[t.ID] = t
	}

	return &ScheduleInput{
		Tasks:          m.TaskEntitiesToInputs(tasks),
		Windows:        m.WindowEntitiesToWindows(windows),
		ExistingEvents: m.EventEntitiesToAssignmentsWithTaskMap(existingEvents, taskMap),
	}
}

// Categorize output for batch processing

type ScheduleChanges struct {
	ToCreate               []*entity.ScheduleEventEntity
	ToUpdate               []*entity.ScheduleEventEntity
	ToDelete               []int64          // Event IDs to delete
	TasksToMarkScheduled   []int64          // ScheduleTaskIDs that were successfully scheduled
	TasksToMarkUnscheduled map[int64]string // ScheduleTaskID -> reason
}

func (m *AlgorithmMapper) DiffScheduleOutput(
	output *ScheduleOutput,
	existingEvents []*entity.ScheduleEventEntity,
	planID int64,
	taskMap map[int64]*entity.ScheduleTaskEntity,
) *ScheduleChanges {
	changes := &ScheduleChanges{
		ToCreate:               make([]*entity.ScheduleEventEntity, 0),
		ToUpdate:               make([]*entity.ScheduleEventEntity, 0),
		ToDelete:               make([]int64, 0),
		TasksToMarkScheduled:   make([]int64, 0),
		TasksToMarkUnscheduled: make(map[int64]string),
	}

	// Build existing event lookup
	existingMap := make(map[int64]*entity.ScheduleEventEntity)
	for _, e := range existingEvents {
		existingMap[e.ID] = e
	}

	// Track which existing events are still present
	keptEventIDs := make(map[int64]bool)

	// Track scheduled task IDs
	scheduledTaskIDs := make(map[int64]bool)

	// Process new assignments
	for _, assignment := range output.Assignments {
		// Mark this task as scheduled
		scheduledTaskIDs[assignment.ScheduleTaskID] = true

		if assignment.EventID != nil {
			// Existing event - check if updated
			keptEventIDs[*assignment.EventID] = true

			existing, ok := existingMap[*assignment.EventID]
			if ok && m.assignmentDiffersFromEvent(assignment, existing) {
				// Update existing event
				updated := m.updateEventFromAssignment(existing, assignment)
				changes.ToUpdate = append(changes.ToUpdate, updated)
			}
		} else {
			// New event
			task, ok := taskMap[assignment.ScheduleTaskID]
			if ok {
				newEvent := m.AssignmentToEventEntity(assignment, planID, task)
				changes.ToCreate = append(changes.ToCreate, newEvent)
			}
		}
	}

	// Collect scheduled task IDs for status update
	for scheduleTaskID := range scheduledTaskIDs {
		changes.TasksToMarkScheduled = append(changes.TasksToMarkScheduled, scheduleTaskID)
	}

	// Find events to delete (no longer in schedule)
	for id := range existingMap {
		if !keptEventIDs[id] {
			changes.ToDelete = append(changes.ToDelete, id)
		}
	}

	// Mark unscheduled tasks
	for _, u := range output.UnscheduledTasks {
		changes.TasksToMarkUnscheduled[u.ScheduleTaskID] = u.Reason
	}

	return changes
}

func (m *AlgorithmMapper) assignmentDiffersFromEvent(a *Assignment, e *entity.ScheduleEventEntity) bool {
	return a.DateMs != e.DateMs ||
		a.StartMin != e.StartMin ||
		a.EndMin != e.EndMin ||
		a.PartIndex != e.PartIndex ||
		a.TotalParts != e.TotalParts ||
		a.IsPinned != e.IsPinned
}

func (m *AlgorithmMapper) updateEventFromAssignment(e *entity.ScheduleEventEntity, a *Assignment) *entity.ScheduleEventEntity {
	e.DateMs = a.DateMs
	e.StartMin = a.StartMin
	e.EndMin = a.EndMin
	e.PartIndex = a.PartIndex
	e.TotalParts = a.TotalParts
	e.IsPinned = a.IsPinned
	e.UtilityScore = &a.UtilityScore
	return e
}

// Optimization Service Mappers (for ptm_optimization)
// Note: ptm_optimization uses taskId as a generic identifier, not specifically ptm_task's TaskID
// We send ScheduleTaskID as taskId for simpler mapping

// ToOptimizationTaskInput converts ScheduleTaskEntity to optimization.TaskInput
// Uses ScheduleTaskID (task.ID) as taskId for simpler response mapping
func (m *AlgorithmMapper) ToOptimizationTaskInput(task *entity.ScheduleTaskEntity) *optimization.TaskInput {
	return &optimization.TaskInput{
		TaskID:           task.ID, // Use ScheduleTaskID, not ptm_task's TaskID
		DurationMin:      task.DurationMin,
		PriorityScore:    task.PriorityScore,
		DeadlineMs:       task.DeadlineMs,
		EarliestStartMs:  task.EarliestStartMs,
		Effort:           1.0, // Default value, can be enhanced with user preferences
		Enjoyability:     0.5, // Default value, can be enhanced with user preferences
		DependentTaskIds: task.DependentTaskIDs,
	}
}

// ToOptimizationTaskInputs converts multiple ScheduleTaskEntity to optimization.TaskInput slice
func (m *AlgorithmMapper) ToOptimizationTaskInputs(tasks []*entity.ScheduleTaskEntity) []*optimization.TaskInput {
	inputs := make([]*optimization.TaskInput, 0, len(tasks))
	for _, t := range tasks {
		if t.IsSchedulable() {
			inputs = append(inputs, m.ToOptimizationTaskInput(t))
		}
	}
	return inputs
}

// ToOptimizationWindow converts ScheduleWindowEntity to optimization.Window
func (m *AlgorithmMapper) ToOptimizationWindow(window *entity.ScheduleWindowEntity) *optimization.Window {
	return &optimization.Window{
		DateMs:     window.DateMs,
		StartMin:   window.StartMin,
		EndMin:     window.EndMin,
		IsDeepWork: false, // Default, can be enhanced from FocusTimeBlocks
	}
}

// ToOptimizationWindows converts multiple ScheduleWindowEntity to optimization.Window slice
func (m *AlgorithmMapper) ToOptimizationWindows(windows []*entity.ScheduleWindowEntity) []*optimization.Window {
	result := make([]*optimization.Window, len(windows))
	for i, w := range windows {
		result[i] = m.ToOptimizationWindow(w)
	}
	return result
}

// BuildOptimizationRequest creates an OptimizationRequest from entities
func (m *AlgorithmMapper) BuildOptimizationRequest(
	tasks []*entity.ScheduleTaskEntity,
	windows []*entity.ScheduleWindowEntity,
) *optimization.OptimizationRequest {
	return &optimization.OptimizationRequest{
		Tasks:   m.ToOptimizationTaskInputs(tasks),
		Windows: m.ToOptimizationWindows(windows),
		Weights: optimization.DefaultWeights(),
		Params:  optimization.DefaultParams(),
	}
}

// OptimizationResultToAssignments converts optimization.PlanResult to local Assignments
// taskMap: ScheduleTaskID -> ScheduleTaskEntity (since we send ScheduleTaskID as taskId)
func (m *AlgorithmMapper) OptimizationResultToAssignments(
	result *optimization.PlanResult,
	taskMap map[int64]*entity.ScheduleTaskEntity,
) []*Assignment {
	assignments := make([]*Assignment, 0, len(result.Assignments))

	for _, optAssignment := range result.Assignments {
		// optAssignment.TaskID is actually ScheduleTaskID (we sent it that way)
		scheduleTaskID := optAssignment.TaskID
		task, ok := taskMap[scheduleTaskID]
		if !ok {
			continue
		}

		assignment := &Assignment{
			TaskID:         task.TaskID, // Original ptm_task's TaskID
			ScheduleTaskID: scheduleTaskID,
			DateMs:         optAssignment.DateMs,
			StartMin:       optAssignment.StartMin,
			EndMin:         optAssignment.EndMin,
			PartIndex:      optAssignment.PartIndex,
			TotalParts:     optAssignment.TotalParts,
			Title:          task.Title,
		}

		if optAssignment.UtilityScore != nil {
			assignment.UtilityScore = *optAssignment.UtilityScore
		}

		assignments = append(assignments, assignment)
	}

	return assignments
}

// OptimizationResultToScheduleOutput converts optimization.PlanResult to local ScheduleOutput
// taskMap: ScheduleTaskID -> ScheduleTaskEntity
func (m *AlgorithmMapper) OptimizationResultToScheduleOutput(
	result *optimization.PlanResult,
	taskMap map[int64]*entity.ScheduleTaskEntity,
) *ScheduleOutput {
	output := NewScheduleOutput()

	output.Assignments = m.OptimizationResultToAssignments(result, taskMap)

	// unscheduled.TaskID is actually ScheduleTaskID (we sent it that way)
	for _, unscheduled := range result.UnScheduled {
		scheduleTaskID := unscheduled.TaskID
		task, ok := taskMap[scheduleTaskID]
		ptmTaskID := int64(0)
		if ok {
			ptmTaskID = task.TaskID // Original ptm_task's TaskID
		}
		output.UnscheduledTasks = append(output.UnscheduledTasks, &UnscheduledTask{
			TaskID:         ptmTaskID,
			ScheduleTaskID: scheduleTaskID,
			Reason:         unscheduled.Reason,
		})
	}

	// Calculate metrics
	output.Metrics = &ScheduleMetrics{
		TotalTasks:       len(taskMap),
		ScheduledTasks:   result.ScheduledCount(),
		UnscheduledTasks: len(result.UnScheduled),
	}

	return output
}

// ScheduleOutputToEvents converts ScheduleOutput assignments to ScheduleEventEntity slice
// taskMap: ScheduleTaskID -> ScheduleTaskEntity
func (m *AlgorithmMapper) ScheduleOutputToEvents(
	output *ScheduleOutput,
	planID int64,
	taskMap map[int64]*entity.ScheduleTaskEntity,
) []*entity.ScheduleEventEntity {
	events := make([]*entity.ScheduleEventEntity, 0, len(output.Assignments))

	for _, assignment := range output.Assignments {
		task, ok := taskMap[assignment.ScheduleTaskID]
		if !ok {
			continue
		}

		utilityScore := assignment.UtilityScore
		event := &entity.ScheduleEventEntity{
			SchedulePlanID: planID,
			ScheduleTaskID: assignment.ScheduleTaskID,
			DateMs:         assignment.DateMs,
			StartMin:       assignment.StartMin,
			EndMin:         assignment.EndMin,
			Title:          task.Title,
			PartIndex:      1,
			TotalParts:     1,
			Status:         enum.ScheduleEventPlanned,
			IsPinned:       false,
			UtilityScore:   &utilityScore,
		}
		events = append(events, event)
	}

	return events
}
