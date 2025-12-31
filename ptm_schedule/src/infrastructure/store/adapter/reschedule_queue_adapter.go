/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/infrastructure/store/mapper"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

type RescheduleQueueAdapter struct {
	BaseStoreAdapter
}

func NewRescheduleQueueAdapter(db *gorm.DB) port.IRescheduleQueuePort {
	return &RescheduleQueueAdapter{BaseStoreAdapter: BaseStoreAdapter{db: db}}
}

func (a *RescheduleQueueAdapter) Upsert(ctx context.Context, tx *gorm.DB, item *entity.RescheduleQueueItem) error {
	mo := mapper.RescheduleQueueMapper.ToModel(item)

	return a.WithTx(tx).WithContext(ctx).Clauses(clause.OnConflict{
		Columns: []clause.Column{
			{Name: "schedule_plan_id"},
			{Name: "entity_id"},
			{Name: "trigger_type"},
		},
		Where: clause.Where{
			Exprs: []clause.Expression{
				clause.Eq{Column: clause.Column{Table: "reschedule_queues", Name: "status"}, Value: string(enum.QueuePending)},
			},
		},
		DoUpdates: clause.Assignments(map[string]any{
			"change_payload": mo.ChangePayload,
			"debounce_until": mo.DebounceUntil,
			"priority":       mo.Priority,
			"updated_at":     time.Now(),
		}),
	}).Create(mo).Error
}

func (a *RescheduleQueueAdapter) GetDirtyPlanIDs(ctx context.Context, limit int) ([]int64, error) {
	var planIDs []int64
	now := time.Now()
	maxWaitTime := now.Add(-constant.MaxDebounceWait)

	err := a.db.WithContext(ctx).
		Model(&model.RescheduleQueueModel{}).
		Select("DISTINCT schedule_plan_id").
		Where("status = ?", string(enum.QueuePending)).
		Where("debounce_until <= ? OR first_created_at <= ?", now, maxWaitTime).
		Limit(limit).
		Pluck("schedule_plan_id", &planIDs).Error

	return planIDs, err
}

func (a *RescheduleQueueAdapter) FetchAndLockBatch(ctx context.Context, tx *gorm.DB, planID int64) ([]*entity.RescheduleQueueItem, error) {
	var models []*model.RescheduleQueueModel
	now := time.Now()
	maxWaitTime := now.Add(-constant.MaxDebounceWait)

	err := a.WithTx(tx).WithContext(ctx).
		Clauses(clause.Locking{Strength: "UPDATE", Options: "SKIP LOCKED"}).
		Where("schedule_plan_id = ?", planID).
		Where("status = ?", string(enum.QueuePending)).
		Where("debounce_until <= ? OR first_created_at <= ?", now, maxWaitTime).
		Order("priority ASC, created_at ASC").
		Find(&models).Error

	if err != nil {
		return nil, err
	}
	return mapper.RescheduleQueueMapper.ToEntities(models), nil
}

func (a *RescheduleQueueAdapter) UpdateBatchStatus(ctx context.Context, tx *gorm.DB, ids []int64, status string, errMsg *string) error {
	if len(ids) == 0 {
		return nil
	}
	updates := map[string]any{
		"status":     status,
		"updated_at": time.Now(),
	}
	if status == string(enum.QueueCompleted) || status == string(enum.QueueFailed) {
		now := time.Now()
		updates["processed_at"] = &now
	}
	if errMsg != nil {
		updates["error_message"] = errMsg
	}
	return a.WithTx(tx).WithContext(ctx).
		Model(&model.RescheduleQueueModel{}).
		Where("id IN ?", ids).
		Updates(updates).Error
}

func (a *RescheduleQueueAdapter) MarkProcessing(ctx context.Context, tx *gorm.DB, ids []int64) error {
	return a.UpdateBatchStatus(ctx, tx, ids, string(enum.QueueProcessing), nil)
}

func (a *RescheduleQueueAdapter) IncrementRetryCount(ctx context.Context, tx *gorm.DB, ids []int64) error {
	if len(ids) == 0 {
		return nil
	}
	return a.WithTx(tx).WithContext(ctx).
		Model(&model.RescheduleQueueModel{}).
		Where("id IN ?", ids).
		UpdateColumn("retry_count", gorm.Expr("retry_count + 1")).Error
}

func (a *RescheduleQueueAdapter) DeleteCompleted(ctx context.Context, olderThan int64) (int64, error) {
	cutoff := time.UnixMilli(olderThan)
	result := a.WithTx(nil).WithContext(ctx).
		Where("status = ?", string(enum.QueueCompleted)).
		Where("processed_at < ?", cutoff).
		Delete(&model.RescheduleQueueModel{})
	return result.RowsAffected, result.Error
}
