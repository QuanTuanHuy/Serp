/*
Author: QuanTuanHuy
Description: Part of Serp Project - Mapper between domain entities and algorithm DTOs
*/

package algorithm

import (
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

func (m *AlgorithmMapper) EventEntityToAssignment(event *entity.ScheduleEventEntity) *Assignment {
	var utilityScore float64
	if event.UtilityScore != nil {
		utilityScore = *event.UtilityScore
	}

	return &Assignment{
		EventID:        &event.ID,
		TaskID:         event.ScheduleTaskID, // This maps to original TaskID via ScheduleTask
		ScheduleTaskID: event.ScheduleTaskID,
		DateMs:         event.DateMs,
		StartMin:       event.StartMin,
		EndMin:         event.EndMin,
		PartIndex:      event.PartIndex,
		TotalParts:     event.TotalParts,
		IsPinned:       event.IsPinned,
		UtilityScore:   utilityScore,
		Title:          event.Title,
	}
}

func (m *AlgorithmMapper) EventEntitiesToAssignments(events []*entity.ScheduleEventEntity) []*Assignment {
	result := make([]*Assignment, len(events))
	for i, e := range events {
		result[i] = m.EventEntityToAssignment(e)
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
	return &ScheduleInput{
		Tasks:          m.TaskEntitiesToInputs(tasks),
		Windows:        m.WindowEntitiesToWindows(windows),
		ExistingEvents: m.EventEntitiesToAssignments(existingEvents),
	}
}

// Categorize output for batch processing

type ScheduleChanges struct {
	ToCreate               []*entity.ScheduleEventEntity
	ToUpdate               []*entity.ScheduleEventEntity
	ToDelete               []int64          // Event IDs to delete
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
		TasksToMarkUnscheduled: make(map[int64]string),
	}

	// Build existing event lookup
	existingMap := make(map[int64]*entity.ScheduleEventEntity)
	for _, e := range existingEvents {
		existingMap[e.ID] = e
	}

	// Track which existing events are still present
	keptEventIDs := make(map[int64]bool)

	// Process new assignments
	for _, assignment := range output.Assignments {
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

	// Find events to delete (no longer in schedule)
	for id := range existingMap {
		if !keptEventIDs[id] {
			changes.ToDelete = append(changes.ToDelete, id)
		}
	}

	// Mark unscheduled tasks
	for _, u := range output.UnscheduledTasks {
		// Find scheduleTaskID from taskID
		for _, task := range taskMap {
			if task.TaskID == u.TaskID {
				changes.TasksToMarkUnscheduled[task.ID] = u.Reason
				break
			}
		}
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
