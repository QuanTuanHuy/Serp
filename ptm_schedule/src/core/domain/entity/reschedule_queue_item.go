/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type RescheduleQueueItem struct {
	BaseEntity
	UserID         int64
	SchedulePlanID int64

	TriggerType enum.TriggerType
	EntityID    int64
	EntityType  string

	ChangePayload string

	Status   enum.QueueStatus
	Priority int

	DebounceUntil  int64
	FirstCreatedAt int64

	ProcessedAt          *int64
	ProcessingDurationMs *int
	ErrorMessage         *string
	RetryCount           int
}

type RescheduleBatch struct {
	PlanID   int64
	UserID   int64
	Items    []*RescheduleQueueItem
	Strategy enum.RescheduleStrategy
}

func (b *RescheduleBatch) ItemIDs() []int64 {
	ids := make([]int64, len(b.Items))
	for i, item := range b.Items {
		ids[i] = item.ID
	}
	return ids
}

func (b *RescheduleBatch) HasTrigger(t enum.TriggerType) bool {
	for _, item := range b.Items {
		if item.TriggerType == t {
			return true
		}
	}
	return false
}

func (b *RescheduleBatch) DetermineStrategy() enum.RescheduleStrategy {
	if b.HasTrigger(enum.TriggerAvailability) {
		return enum.StrategyFullReplan
	}
	if b.HasTrigger(enum.TriggerConstraintChange) || b.HasTrigger(enum.TriggerTaskAdded) || b.HasTrigger(enum.TriggerTaskDeleted) {
		return enum.StrategyInsertion
	}
	return enum.StrategyRipple
}
