package constant

type EventType string

const (
	TaskCreatedEvent string = "ptm.task.created"
	TaskUpdatedEvent string = "ptm.task.updated"
	TaskDeletedEvent string = "ptm.task.deleted"
)
