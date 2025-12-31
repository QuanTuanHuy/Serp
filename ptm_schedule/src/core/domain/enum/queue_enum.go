/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type TriggerType string

const (
	// Event-level triggers (highest priority - direct user actions)
	TriggerManualDrag    TriggerType = "MANUAL_DRAG"    // User drags event to new time → Strategy: RIPPLE
	TriggerEventSplit    TriggerType = "EVENT_SPLIT"    // Split event into multiple parts → Strategy: RIPPLE
	TriggerEventComplete TriggerType = "EVENT_COMPLETE" // Mark event as completed → Strategy: RIPPLE
	TriggerEventSkip     TriggerType = "EVENT_SKIP"     // Skip/cancel event → Strategy: RIPPLE

	// Task-level triggers (medium priority - task data changes)
	TriggerConstraintChange TriggerType = "CONSTRAINT_CHANGE" // Duration/deadline/priority/HasSubtasks change → Strategy: INSERTION
	TriggerTaskAdded        TriggerType = "TASK_ADDED"        // New task created → Strategy: INSERTION
	TriggerTaskDeleted      TriggerType = "TASK_DELETED"      // Task removed → Strategy: INSERTION

	// System-level triggers (lowest priority - fundamental changes)
	TriggerAvailability TriggerType = "AVAILABILITY_CHANGE" // Work hours changed → Strategy: FULL_REPLAN
)

func (t TriggerType) Priority() int {
	switch t {
	case TriggerManualDrag, TriggerEventSplit:
		return 1
	case TriggerEventComplete, TriggerEventSkip:
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
	// StrategyRipple: Greedy Insertion + Ripple Effect
	// - Tries to insert task in gaps
	// - If critical and no gaps, displaces lower-priority tasks
	// - Best for: Event manipulations (MANUAL_DRAG, EVENT_SPLIT, EVENT_COMPLETE)
	StrategyRipple RescheduleStrategy = "RIPPLE"

	// StrategyInsertion: Greedy Insertion Only (no displacement)
	// - Keeps existing events (except affected task's events)
	// - Inserts affected task(s) in available gaps
	// - Fails gracefully if no gaps available
	// - Best for: Task changes (CONSTRAINT_CHANGE, TASK_ADDED, TASK_DELETED)
	StrategyInsertion RescheduleStrategy = "INSERTION"

	// StrategyFullReplan: Complete Schedule Rebuild
	// - Clears all non-pinned events
	// - Re-schedules all tasks from scratch
	// - Provides optimal schedule but slower
	// - Best for: Fundamental changes (AVAILABILITY_CHANGE)
	StrategyFullReplan RescheduleStrategy = "FULL_REPLAN"
)
