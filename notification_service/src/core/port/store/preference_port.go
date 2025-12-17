/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/notification-service/src/core/domain/entity"
	"gorm.io/gorm"
)

type IPreferencePort interface {
	Create(ctx context.Context, tx *gorm.DB, preference *entity.NotificationPreferenceEntity) (*entity.NotificationPreferenceEntity, error)
	Update(ctx context.Context, tx *gorm.DB, preference *entity.NotificationPreferenceEntity) (*entity.NotificationPreferenceEntity, error)
	GetByUserID(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error)
}
