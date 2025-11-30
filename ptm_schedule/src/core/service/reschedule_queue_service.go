/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"encoding/json"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type IRescheduleQueueService interface {
	EnqueueEventMove(ctx context.Context, tx *gorm.DB, planID, userID, eventID int64, payload map[string]any) error
	EnqueueEventSplit(ctx context.Context, tx *gorm.DB, planID, userID, eventID int64, payload map[string]any) error
	EnqueueEventComplete(ctx context.Context, tx *gorm.DB, planID, userID, eventID int64) error
	EnqueueTaskChange(ctx context.Context, tx *gorm.DB, planID, userID, taskID int64, trigger enum.TriggerType) error
	EnqueueAvailabilityChange(ctx context.Context, tx *gorm.DB, planID, userID int64) error
}

type RescheduleQueueService struct {
	queuePort port.IRescheduleQueuePort
}

func NewRescheduleQueueService(queuePort port.IRescheduleQueuePort) IRescheduleQueueService {
	return &RescheduleQueueService{queuePort: queuePort}
}

func (s *RescheduleQueueService) EnqueueEventMove(ctx context.Context, tx *gorm.DB, planID, userID, eventID int64, payload map[string]any) error {
	return s.enqueue(ctx, tx, planID, userID, eventID, constant.EntityTypeEvent, enum.TriggerManualDrag, payload)
}

func (s *RescheduleQueueService) EnqueueEventSplit(ctx context.Context, tx *gorm.DB, planID, userID, eventID int64, payload map[string]any) error {
	return s.enqueue(ctx, tx, planID, userID, eventID, constant.EntityTypeEvent, enum.TriggerEventSplit, payload)
}

func (s *RescheduleQueueService) EnqueueEventComplete(ctx context.Context, tx *gorm.DB, planID, userID, eventID int64) error {
	return s.enqueue(ctx, tx, planID, userID, eventID, constant.EntityTypeEvent, enum.TriggerEventComplete, nil)
}

func (s *RescheduleQueueService) EnqueueTaskChange(ctx context.Context, tx *gorm.DB, planID, userID, taskID int64, trigger enum.TriggerType) error {
	return s.enqueue(ctx, tx, planID, userID, taskID, constant.EntityTypeTask, trigger, nil)
}

func (s *RescheduleQueueService) EnqueueAvailabilityChange(ctx context.Context, tx *gorm.DB, planID, userID int64) error {
	return s.enqueue(ctx, tx, planID, userID, 0, "", enum.TriggerAvailability, nil)
}

func (s *RescheduleQueueService) enqueue(ctx context.Context, tx *gorm.DB, planID, userID, entityID int64, entityType string, trigger enum.TriggerType, payload map[string]any) error {
	nowMs := time.Now().UnixMilli()
	var payloadStr string
	if payload != nil {
		bytes, _ := json.Marshal(payload)
		payloadStr = string(bytes)
	}

	item := &entity.RescheduleQueueItem{
		BaseEntity: entity.BaseEntity{
			CreatedAt: nowMs,
			UpdatedAt: nowMs,
		},
		UserID:         userID,
		SchedulePlanID: planID,
		TriggerType:    trigger,
		EntityID:       entityID,
		EntityType:     entityType,
		ChangePayload:  payloadStr,
		Status:         enum.QueuePending,
		Priority:       trigger.Priority(),
		DebounceUntil:  nowMs + constant.DebounceWindowMs,
		FirstCreatedAt: nowMs,
	}

	return s.queuePort.Upsert(ctx, tx, item)
}
