/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/serp/notification-service/src/core/domain/entity"
	port "github.com/serp/notification-service/src/core/port/store"
	"github.com/serp/notification-service/src/infrastructure/store/mapper"
	"github.com/serp/notification-service/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type PreferenceAdapter struct {
	db     *gorm.DB
	mapper *mapper.NotificationPreferenceMapper
}

func NewPreferenceAdapter(db *gorm.DB) port.IPreferencePort {
	return &PreferenceAdapter{
		db:     db,
		mapper: mapper.NewNotificationPreferenceMapper(),
	}
}

func (a *PreferenceAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *PreferenceAdapter) Create(ctx context.Context, tx *gorm.DB, preference *entity.NotificationPreferenceEntity) (*entity.NotificationPreferenceEntity, error) {
	db := a.getDB(tx)
	modelObj := a.mapper.ToModel(preference)
	if err := db.WithContext(ctx).Create(modelObj).Error; err != nil {
		return nil, fmt.Errorf("failed to create notification preference: %w", err)
	}
	return a.mapper.ToEntity(modelObj), nil
}

func (a *PreferenceAdapter) Update(ctx context.Context, tx *gorm.DB, preference *entity.NotificationPreferenceEntity) (*entity.NotificationPreferenceEntity, error) {
	db := a.getDB(tx)
	modelObj := a.mapper.ToModel(preference)
	if err := db.WithContext(ctx).
		Model(&model.NotificationPreferenceModel{}).
		Where("user_id = ? AND tenant_id = ?", preference.UserID, preference.TenantID).
		Updates(map[string]any{
			"enable_in_app":         modelObj.EnableInApp,
			"enable_email":          modelObj.EnableEmail,
			"enable_push":           modelObj.EnablePush,
			"quiet_hours_enabled":   modelObj.QuietHoursEnabled,
			"quiet_hours_start_min": modelObj.QuietHoursStartMin,
			"quiet_hours_end_min":   modelObj.QuietHoursEndMin,
		}).Error; err != nil {
		return nil, fmt.Errorf("failed to update notification preference: %w", err)
	}
	return a.getByUserIDWithDB(ctx, db, preference.UserID)
}

func (a *PreferenceAdapter) GetByUserID(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error) {
	return a.getByUserIDWithDB(ctx, a.db, userID)
}

func (a *PreferenceAdapter) getByUserIDWithDB(ctx context.Context, db *gorm.DB, userID int64) (*entity.NotificationPreferenceEntity, error) {
	var modelObj model.NotificationPreferenceModel
	if err := db.WithContext(ctx).
		Where("user_id = ?", userID).
		First(&modelObj).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get notification preference: %w", err)
	}
	return a.mapper.ToEntity(&modelObj), nil
}
