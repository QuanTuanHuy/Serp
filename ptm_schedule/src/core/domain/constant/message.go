package constant

type EventType string

// Inbound events (from ptm_task)
const (
	TaskCreatedEvent string = "ptm.task.created"
	TaskUpdatedEvent string = "ptm.task.updated"
	TaskDeletedEvent string = "ptm.task.deleted"
)

// Outbound events (to ptm_task)
const (
	TaskCompletedFromScheduleEvent string = "ptm.schedule.task.completed"
	ScheduleTaskPinnedEvent        string = "ptm.schedule.task.pinned"
)

// Internal events (for local processing)
const (
	RippleEffectJobEvent string = "ptm.schedule.ripple.effect"
)

// Kafka topics
const (
	TaskEventTopic     string = "ptm-task-events"
	ScheduleEventTopic string = "ptm-schedule-events"
	RippleEffectTopic  string = "ptm-schedule-ripple-effect"
)
