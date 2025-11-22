package enum

type ActivityEventType string

const (
	EventTypeTaskCreated   ActivityEventType = "task_created"
	EventTypeTaskUpdated   ActivityEventType = "task_updated"
	EventTypeTaskDeleted   ActivityEventType = "task_deleted"
	EventTypeTaskCompleted ActivityEventType = "task_completed"

	EventTypeProjectCreated    ActivityEventType = "project_created"
	EventTypeProjectUpdated    ActivityEventType = "project_updated"
	EventTypeProjectDeleted    ActivityEventType = "project_deleted"
	EventTypeProjectArchived   ActivityEventType = "project_archived"
	EventTypeProjectUnarchived ActivityEventType = "project_unarchived"

	EventTypeScheduleOptimized ActivityEventType = "schedule_optimized"

	EventTypeAlgorithmExecuted ActivityEventType = "algorithm_executed"
)

func (e ActivityEventType) IsValid() bool {
	switch e {
	case EventTypeTaskCreated, EventTypeTaskUpdated, EventTypeTaskDeleted, EventTypeTaskCompleted,
		EventTypeProjectCreated, EventTypeProjectUpdated, EventTypeProjectDeleted,
		EventTypeProjectArchived, EventTypeProjectUnarchived,
		EventTypeScheduleOptimized,
		EventTypeAlgorithmExecuted:
		return true
	}
	return false
}
