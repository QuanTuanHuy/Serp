/*
Author: QuanTuanHuy
Description: Part of Serp Project - Tests for Hybrid Scheduler
*/

package algorithm

import (
	"testing"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/stretchr/testify/assert"
)

func TestNewHybridScheduler(t *testing.T) {
	scheduler := NewHybridScheduler()
	assert.NotNil(t, scheduler)
	assert.NotNil(t, scheduler.scoring)
	assert.NotNil(t, scheduler.slotUtils)
}

func TestSchedule_EmptyInput(t *testing.T) {
	scheduler := NewHybridScheduler()
	input := &ScheduleInput{
		Tasks:          []*TaskInput{},
		Windows:        []*Window{},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	assert.NotNil(t, output)
	assert.Empty(t, output.Assignments)
	assert.Empty(t, output.UnscheduledTasks)
	assert.Equal(t, 0, output.Metrics.TotalTasks)
}

func TestSchedule_SingleTaskFitsInWindow(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Task 1",
				DurationMin:    60,
				Priority:       enum.PriorityHigh,
				PriorityScore:  80,
				AllowSplit:     false,
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,  // 9:00 AM
				EndMin:   17 * 60, // 5:00 PM
			},
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	assert.NotNil(t, output)
	assert.Len(t, output.Assignments, 1)
	assert.Empty(t, output.UnscheduledTasks)
	assert.Equal(t, int64(1), output.Assignments[0].TaskID)
	assert.Equal(t, 60, output.Assignments[0].Duration())
	assert.Equal(t, 1, output.Metrics.ScheduledTasks)
}

func TestSchedule_TaskWithBuffer(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:          1,
				ScheduleTaskID:  100,
				Title:           "Task with buffer",
				DurationMin:     60,
				Priority:        enum.PriorityMedium,
				PriorityScore:   50,
				BufferBeforeMin: 15,
				AllowSplit:      false,
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,
				EndMin:   12 * 60,
			},
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	assert.Len(t, output.Assignments, 1)
	// Task should start at 9:15 (9:00 + 15min buffer)
	assert.Equal(t, 9*60+15, output.Assignments[0].StartMin)
	assert.Equal(t, 10*60+15, output.Assignments[0].EndMin)
}

func TestSchedule_TaskSplitting(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Splittable Task",
				DurationMin:    120, // 2 hours
				Priority:       enum.PriorityHigh,
				PriorityScore:  80,
				AllowSplit:     true,
				MinSplitMin:    30,
				MaxSplitCount:  4,
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,
				EndMin:   10 * 60, // 1 hour gap
			},
			{
				DateMs:   now,
				StartMin: 14 * 60,
				EndMin:   15 * 60, // 1 hour gap
			},
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	assert.Empty(t, output.UnscheduledTasks)
	assert.Len(t, output.Assignments, 2) // Split into 2 parts

	// Verify parts
	totalDuration := 0
	for _, a := range output.Assignments {
		assert.Equal(t, int64(1), a.TaskID)
		assert.Equal(t, 2, a.TotalParts)
		totalDuration += a.Duration()
	}
	assert.Equal(t, 120, totalDuration)
}

func TestSchedule_NoSplitTaskDoesNotFit(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Large Task",
				DurationMin:    180, // 3 hours
				Priority:       enum.PriorityHigh,
				PriorityScore:  80,
				AllowSplit:     false,
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,
				EndMin:   10 * 60, // Only 1 hour
			},
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	assert.Empty(t, output.Assignments)
	assert.Len(t, output.UnscheduledTasks, 1)
	assert.Equal(t, int64(1), output.UnscheduledTasks[0].TaskID)
}

func TestSchedule_PinnedTasksPreserved(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()
	eventID := int64(999)

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Pinned Task",
				DurationMin:    60,
				Priority:       enum.PriorityHigh,
				PriorityScore:  80,
				IsPinned:       true,
			},
			{
				TaskID:         2,
				ScheduleTaskID: 101,
				Title:          "Normal Task",
				DurationMin:    60,
				Priority:       enum.PriorityMedium,
				PriorityScore:  50,
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,
				EndMin:   17 * 60,
			},
		},
		ExistingEvents: []*Assignment{
			{
				EventID:        &eventID,
				TaskID:         1,
				ScheduleTaskID: 100,
				DateMs:         now,
				StartMin:       10 * 60,
				EndMin:         11 * 60,
				IsPinned:       true,
			},
		},
	}

	output := scheduler.Schedule(input)

	// Both tasks should be scheduled
	assert.Len(t, output.Assignments, 2)

	// Pinned task should be preserved at original time
	var pinnedAssignment *Assignment
	for _, a := range output.Assignments {
		if a.IsPinned {
			pinnedAssignment = a
			break
		}
	}
	assert.NotNil(t, pinnedAssignment)
	assert.Equal(t, 10*60, pinnedAssignment.StartMin)
	assert.Equal(t, 11*60, pinnedAssignment.EndMin)
}

func TestDeadlineConstraint(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour)
	todayMs := now.UnixMilli()
	tomorrowMs := now.Add(24 * time.Hour).UnixMilli()
	deadlineMs := todayMs + 12*3600*1000 // Deadline at noon today

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Urgent Task",
				DurationMin:    60,
				Priority:       enum.PriorityHigh,
				PriorityScore:  100,
				DeadlineMs:     &deadlineMs,
			},
		},
		Windows: []*Window{
			{
				DateMs:   tomorrowMs, // Tomorrow - after deadline
				StartMin: 9 * 60,
				EndMin:   17 * 60,
			},
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	// Task cannot be scheduled after deadline
	assert.Empty(t, output.Assignments)
	assert.Len(t, output.UnscheduledTasks, 1)
}

func TestSchedule_MultipleTasksPriorityOrder(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Low Priority",
				DurationMin:    60,
				Priority:       enum.PriorityLow,
				PriorityScore:  20,
			},
			{
				TaskID:         2,
				ScheduleTaskID: 101,
				Title:          "High Priority",
				DurationMin:    60,
				Priority:       enum.PriorityHigh,
				PriorityScore:  80,
			},
			{
				TaskID:         3,
				ScheduleTaskID: 102,
				Title:          "Medium Priority",
				DurationMin:    60,
				Priority:       enum.PriorityMedium,
				PriorityScore:  50,
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,
				EndMin:   12 * 60, // 3 hours = exactly fits 3 tasks
			},
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	assert.Len(t, output.Assignments, 3)
	assert.Empty(t, output.UnscheduledTasks)

	// High priority task should be scheduled first (earliest slot)
	var highPriorityAssignment *Assignment
	for _, a := range output.Assignments {
		if a.TaskID == 2 {
			highPriorityAssignment = a
			break
		}
	}
	assert.NotNil(t, highPriorityAssignment)
	assert.Equal(t, 9*60, highPriorityAssignment.StartMin)
}

func TestScheduleIncremental_AddNewTask(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()
	existingEventID := int64(999)

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Existing Task",
				DurationMin:    60,
				Priority:       enum.PriorityMedium,
				PriorityScore:  50,
			},
			{
				TaskID:         2,
				ScheduleTaskID: 101,
				Title:          "New Task",
				DurationMin:    60,
				Priority:       enum.PriorityHigh,
				PriorityScore:  80,
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,
				EndMin:   17 * 60,
			},
		},
		ExistingEvents: []*Assignment{
			{
				EventID:        &existingEventID,
				TaskID:         1,
				ScheduleTaskID: 100,
				DateMs:         now,
				StartMin:       10 * 60,
				EndMin:         11 * 60,
				PartIndex:      1,
				TotalParts:     1,
			},
		},
	}

	output := scheduler.ScheduleIncremental(input, []int64{2})

	assert.Len(t, output.Assignments, 2)
	assert.Empty(t, output.UnscheduledTasks)

	// Existing task should be preserved
	var existingAssignment *Assignment
	for _, a := range output.Assignments {
		if a.TaskID == 1 {
			existingAssignment = a
			break
		}
	}
	assert.NotNil(t, existingAssignment)
	assert.Equal(t, 10*60, existingAssignment.StartMin)
}

func TestTryRippleEffect_DisplaceLowerPriority(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()
	deadlineMs := now + 6*3600*1000 // Deadline in 6 hours (critical)

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Low Priority Task",
				DurationMin:    60,
				Priority:       enum.PriorityLow,
				PriorityScore:  20,
			},
			{
				TaskID:         2,
				ScheduleTaskID: 101,
				Title:          "Critical Task",
				DurationMin:    60,
				Priority:       enum.PriorityHigh,
				PriorityScore:  100,
				DeadlineMs:     &deadlineMs, // Makes it critical
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,
				EndMin:   10 * 60, // Only 1 hour - one task at a time
			},
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	// Critical task should be scheduled, low priority might be unscheduled
	var criticalScheduled bool
	for _, a := range output.Assignments {
		if a.TaskID == 2 {
			criticalScheduled = true
			break
		}
	}
	assert.True(t, criticalScheduled)
}

func TestTryRippleEffect_CannotDisplacePinned(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()
	eventID := int64(999)
	deadlineMs := now + 6*3600*1000

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Pinned Task",
				DurationMin:    60,
				Priority:       enum.PriorityLow,
				PriorityScore:  20,
				IsPinned:       true,
			},
			{
				TaskID:         2,
				ScheduleTaskID: 101,
				Title:          "Critical Task",
				DurationMin:    60,
				Priority:       enum.PriorityHigh,
				PriorityScore:  100,
				DeadlineMs:     &deadlineMs,
			},
		},
		Windows: []*Window{
			{
				DateMs:   now,
				StartMin: 9 * 60,
				EndMin:   10 * 60, // Only 1 hour
			},
		},
		ExistingEvents: []*Assignment{
			{
				EventID:        &eventID,
				TaskID:         1,
				ScheduleTaskID: 100,
				DateMs:         now,
				StartMin:       9 * 60,
				EndMin:         10 * 60,
				IsPinned:       true,
			},
		},
	}

	output := scheduler.Schedule(input)

	// Pinned task should be preserved
	var pinnedPreserved bool
	for _, a := range output.Assignments {
		if a.TaskID == 1 && a.IsPinned {
			pinnedPreserved = true
			break
		}
	}
	assert.True(t, pinnedPreserved)

	// Critical task should be unscheduled (cannot displace pinned)
	var criticalUnscheduled bool
	for _, u := range output.UnscheduledTasks {
		if u.TaskID == 2 {
			criticalUnscheduled = true
			break
		}
	}
	assert.True(t, criticalUnscheduled)
}

func TestCalculateMetrics(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{TaskID: 1, ScheduleTaskID: 100, Title: "Task 1", DurationMin: 60, PriorityScore: 50},
			{TaskID: 2, ScheduleTaskID: 101, Title: "Task 2", DurationMin: 60, PriorityScore: 50},
			{TaskID: 3, ScheduleTaskID: 102, Title: "Task 3", DurationMin: 60, PriorityScore: 50},
		},
		Windows: []*Window{
			{DateMs: now, StartMin: 9 * 60, EndMin: 11 * 60}, // Only 2 hours
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	assert.Equal(t, 3, output.Metrics.TotalTasks)
	assert.Equal(t, 2, output.Metrics.ScheduledTasks)
	assert.Equal(t, 1, output.Metrics.UnscheduledTasks)
	assert.Equal(t, 180, output.Metrics.TotalDurationMin) // 3 tasks * 60 min
	assert.Equal(t, 120, output.Metrics.UsedDurationMin)  // 2 tasks scheduled
}

func TestMaxSplitCount(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:         1,
				ScheduleTaskID: 100,
				Title:          "Splittable Task",
				DurationMin:    120, // 2 hours
				Priority:       enum.PriorityHigh,
				PriorityScore:  80,
				AllowSplit:     true,
				MinSplitMin:    30,
				MaxSplitCount:  2, // Max 2 splits
			},
		},
		Windows: []*Window{
			{DateMs: now, StartMin: 9 * 60, EndMin: 9*60 + 40},   // 40 min
			{DateMs: now, StartMin: 11 * 60, EndMin: 11*60 + 40}, // 40 min
			{DateMs: now, StartMin: 14 * 60, EndMin: 14*60 + 40}, // 40 min
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	// Should only create max 2 splits, total 80 min scheduled
	if len(output.Assignments) > 0 {
		assert.LessOrEqual(t, len(output.Assignments), 2)
	}
}

func TestEarliestStartConstraint(t *testing.T) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour)
	todayMs := now.UnixMilli()
	earliestMs := todayMs + 12*3600*1000 // Earliest start at noon

	input := &ScheduleInput{
		Tasks: []*TaskInput{
			{
				TaskID:          1,
				ScheduleTaskID:  100,
				Title:           "Task with earliest start",
				DurationMin:     60,
				Priority:        enum.PriorityHigh,
				PriorityScore:   80,
				EarliestStartMs: &earliestMs,
			},
		},
		Windows: []*Window{
			{
				DateMs:   todayMs,
				StartMin: 9 * 60, // Morning window - before earliest start
				EndMin:   11 * 60,
			},
		},
		ExistingEvents: []*Assignment{},
	}

	output := scheduler.Schedule(input)

	// Task cannot be scheduled before earliest start date
	assert.Empty(t, output.Assignments)
	assert.Len(t, output.UnscheduledTasks, 1)
}

// Benchmark tests

func BenchmarkSchedule_SmallInput(b *testing.B) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks:   make([]*TaskInput, 10),
		Windows: make([]*Window, 5),
	}

	for i := 0; i < 10; i++ {
		input.Tasks[i] = &TaskInput{
			TaskID:         int64(i + 1),
			ScheduleTaskID: int64(100 + i),
			Title:          "Task",
			DurationMin:    30,
			PriorityScore:  float64(50 + i),
			AllowSplit:     true,
			MinSplitMin:    15,
		}
	}

	for i := 0; i < 5; i++ {
		input.Windows[i] = &Window{
			DateMs:   now + int64(i)*24*3600*1000,
			StartMin: 9 * 60,
			EndMin:   17 * 60,
		}
	}

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		scheduler.Schedule(input)
	}
}

func BenchmarkSchedule_LargeInput(b *testing.B) {
	scheduler := NewHybridScheduler()
	now := time.Now().Truncate(24 * time.Hour).UnixMilli()

	input := &ScheduleInput{
		Tasks:   make([]*TaskInput, 100),
		Windows: make([]*Window, 14), // 2 weeks
	}

	for i := 0; i < 100; i++ {
		input.Tasks[i] = &TaskInput{
			TaskID:         int64(i + 1),
			ScheduleTaskID: int64(100 + i),
			Title:          "Task",
			DurationMin:    30 + (i % 60),
			PriorityScore:  float64(i % 100),
			AllowSplit:     i%2 == 0,
			MinSplitMin:    15,
		}
	}

	for i := 0; i < 14; i++ {
		input.Windows[i] = &Window{
			DateMs:   now + int64(i)*24*3600*1000,
			StartMin: 9 * 60,
			EndMin:   17 * 60,
		}
	}

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		scheduler.Schedule(input)
	}
}
