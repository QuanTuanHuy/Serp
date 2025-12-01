/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IRescheduleQueuePort interface {
	Upsert(ctx context.Context, tx *gorm.DB, item *entity.RescheduleQueueItem) error

	GetDirtyPlanIDs(ctx context.Context, limit int) ([]int64, error)

	FetchAndLockBatch(ctx context.Context, tx *gorm.DB, planID int64) ([]*entity.RescheduleQueueItem, error)

	UpdateBatchStatus(ctx context.Context, tx *gorm.DB, ids []int64, status string, errMsg *string) error

	MarkProcessing(ctx context.Context, tx *gorm.DB, ids []int64) error

	IncrementRetryCount(ctx context.Context, tx *gorm.DB, ids []int64) error

	DeleteCompleted(ctx context.Context, olderThan int64) (int64, error)
}
