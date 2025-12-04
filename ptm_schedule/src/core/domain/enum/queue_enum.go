/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type TriggerType string

const (
	TriggerManualDrag       TriggerType = "MANUAL_DRAG"
	TriggerConstraintChange TriggerType = "CONSTRAINT_CHANGE"
	TriggerTaskAdded        TriggerType = "TASK_ADDED"
	TriggerTaskDeleted      TriggerType = "TASK_DELETED"
	TriggerAvailability     TriggerType = "AVAILABILITY_CHANGE"
	TriggerEventSplit       TriggerType = "EVENT_SPLIT"
	TriggerEventComplete    TriggerType = "EVENT_COMPLETE"
	TriggerEventSkip        TriggerType = "EVENT_SKIP"
)

func (t TriggerType) Priority() int {
	switch t {
	case TriggerManualDrag:
		return 1
	case TriggerEventSplit, TriggerEventComplete:
		return 2
	case TriggerConstraintChange, TriggerTaskAdded, TriggerTaskDeleted:
		return 5
	case TriggerAvailability:
		return 9
	default:
		return 5
	}
}

type QueueStatus string

const (
	QueuePending    QueueStatus = "PENDING"
	QueueProcessing QueueStatus = "PROCESSING"
	QueueCompleted  QueueStatus = "COMPLETED"
	QueueFailed     QueueStatus = "FAILED"
)

type RescheduleStrategy string

const (
	StrategyRipple     RescheduleStrategy = "RIPPLE"
	StrategyInsertion  RescheduleStrategy = "INSERTION"
	StrategyFullReplan RescheduleStrategy = "FULL_REPLAN"
)
