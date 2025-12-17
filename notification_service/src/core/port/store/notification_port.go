/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/entity"
	"gorm.io/gorm"
)

type INotificationPort interface {
	Create(ctx context.Context, tx *gorm.DB, notification *entity.NotificationEntity) (*entity.NotificationEntity, error)
	CreateBatch(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) ([]*entity.NotificationEntity, error)

	GetByID(ctx context.Context, id int64) (*entity.NotificationEntity, error)
	GetList(ctx context.Context, params *request.GetNotificationParams) ([]*entity.NotificationEntity, int64, error)

	CountUnread(ctx context.Context, userID int64) (int64, error)

	Update(ctx context.Context, tx *gorm.DB, id int64, notification *entity.NotificationEntity) (*entity.NotificationEntity, error)
	UpdateBatch(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) ([]*entity.NotificationEntity, error)
	UpdateAllUnread(ctx context.Context, tx *gorm.DB, userID int64) error

	Delete(ctx context.Context, tx *gorm.DB, id int64) error
}
