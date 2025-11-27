/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/infrastructure/store/mapper"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type TimeBubblesStoreAdapter struct {
	db *gorm.DB
}

func (t *TimeBubblesStoreAdapter) CreateTimeBubblesBatch(ctx context.Context, tx *gorm.DB, timeBubbles []*entity.TimeBubblesEntity) ([]*entity.TimeBubblesEntity, error) {
	timeBubblesModels := mapper.ToTimeBubblesModels(timeBubbles)
	if err := tx.WithContext(ctx).CreateInBatches(&timeBubblesModels, len(timeBubblesModels)).Error; err != nil {
		return nil, err
	}
	return mapper.ToTimeBubblesEntities(timeBubblesModels), nil
}

func (t *TimeBubblesStoreAdapter) GetAllByUserID(ctx context.Context, userID int64) ([]*entity.TimeBubblesEntity, error) {
	timeBubbles := []*model.TimeBubblesModel{}
	if err := t.db.WithContext(ctx).Where("user_id = ?", userID).Find(&timeBubbles).Error; err != nil {
		return nil, err
	}
	return mapper.ToTimeBubblesEntities(timeBubbles), nil
}

func (t *TimeBubblesStoreAdapter) GetByUserIDAndDayOfWeek(ctx context.Context, userID int64, dayOfWeek int) ([]*entity.TimeBubblesEntity, error) {
	timeBubbles := []*model.TimeBubblesModel{}
	if err := t.db.WithContext(ctx).Where("user_id = ? AND day_of_week = ?", userID, dayOfWeek).Find(&timeBubbles).Error; err != nil {
		return nil, err
	}
	return mapper.ToTimeBubblesEntities(timeBubbles), nil
}

func (t *TimeBubblesStoreAdapter) UpdateTimeBubbles(ctx context.Context, tx *gorm.DB, timeBubbleID int64, timeBubble *entity.TimeBubblesEntity) (*entity.TimeBubblesEntity, error) {
	timeBubbleModel := mapper.ToTimeBubblesModel(timeBubble)
	if err := tx.WithContext(ctx).Model(&model.TimeBubblesModel{}).Where("id = ?", timeBubbleID).
		Updates(timeBubbleModel).Error; err != nil {
		return nil, err
	}
	timeBubble.UpdatedAt = time.Now().UnixMilli()
	return timeBubble, nil
}

func (t *TimeBubblesStoreAdapter) DeleteTimeBubbles(ctx context.Context, tx *gorm.DB, userID int64, dayOfWeek int, status enum.ActiveStatus) error {
	if err := tx.WithContext(ctx).
		Where("user_id = ? AND day_of_week = ? AND status = ?", userID, dayOfWeek, string(status)).
		Delete(&model.TimeBubblesModel{}).Error; err != nil {
		return err
	}
	return nil
}

func NewTimeBubblesStoreAdapter(db *gorm.DB) port.ITimeBubblesPort {
	return &TimeBubblesStoreAdapter{db: db}
}
