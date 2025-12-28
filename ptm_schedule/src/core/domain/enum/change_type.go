package enum

type ChangeType string

const (
	ChangeNone       ChangeType = "NONE"
	ChangeMetadata   ChangeType = "METADATA"   // Title, Desc -> Update DB only
	ChangeConstraint ChangeType = "CONSTRAINT" // Duration, Deadline -> Trigger Scheduler
	ChangeStructural ChangeType = "STRUCTURAL" // Subtask info -> Trigger Scheduler
)
