/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"gorm.io/gorm"
)

type ITimeBubblesPort interface {
	CreateTimeBubblesBatch(ctx context.Context, tx *gorm.DB, timeBubbles []*entity.TimeBubblesEntity) ([]*entity.TimeBubblesEntity, error)
	UpdateTimeBubbles(ctx context.Context, tx *gorm.DB, timeBubbleID int64, timeBubble *entity.TimeBubblesEntity) (*entity.TimeBubblesEntity, error)
	GetAllByUserID(ctx context.Context, userID int64) ([]*entity.TimeBubblesEntity, error)
	GetByUserIDAndDayOfWeek(ctx context.Context, userID int64, dayOfWeek int) ([]*entity.TimeBubblesEntity, error)
	DeleteTimeBubbles(ctx context.Context, tx *gorm.DB, userID int64, dayOfWeek int, status enum.ActiveStatus) error
}
