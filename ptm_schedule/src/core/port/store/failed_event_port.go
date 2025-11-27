/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
)

type IFailedEventPort interface {
	GetFailedEvent(ctx context.Context, eventID string) (*entity.FailedEventEntity, error)

	RecordFailedEvent(ctx context.Context, failedEvent *entity.FailedEventEntity) (*entity.FailedEventEntity, error)

	MarkAsSentToDLQ(ctx context.Context, eventID string) error

	DeleteFailedEvent(ctx context.Context, eventID string) error
}
