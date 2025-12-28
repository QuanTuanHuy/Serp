/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"encoding/json"

	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

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

	if b.HasTrigger(enum.TriggerConstraintChange) ||
		b.HasTrigger(enum.TriggerTaskAdded) ||
		b.HasTrigger(enum.TriggerTaskDeleted) {
		return enum.StrategyInsertion
	}

	return enum.StrategyRipple
}

func (b *RescheduleBatch) AffectedScheduleTaskIDs() []int64 {
	seen := make(map[int64]bool)
	result := make([]int64, 0)

	for _, item := range b.Items {
		var taskID int64

		if item.EntityType == constant.EntityTypeTask && item.EntityID > 0 {
			taskID = item.EntityID
		} else if item.EntityType == constant.EntityTypeEvent && item.ChangePayload != "" {
			var payload map[string]any
			if json.Unmarshal([]byte(item.ChangePayload), &payload) == nil {
				if id, ok := payload["scheduleTaskID"].(float64); ok {
					taskID = int64(id)
				}
			}
		}

		if taskID > 0 && !seen[taskID] {
			seen[taskID] = true
			result = append(result, taskID)
		}
	}

	return result
}
