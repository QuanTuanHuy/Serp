/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/infrastructure/store/mapper"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ScheduleDayStoreAdapter struct {
	db *gorm.DB
}

func (s *ScheduleDayStoreAdapter) CreateScheduleDayInBatch(ctx context.Context, tx *gorm.DB, scheduleDays []*entity.ScheduleDayEntity) ([]*entity.ScheduleDayEntity, error) {
	scheduleDayModels := mapper.ToScheduleDayModels(scheduleDays)
	if err := tx.WithContext(ctx).CreateInBatches(&scheduleDayModels, len(scheduleDayModels)).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleDayEntities(scheduleDayModels), nil
}

func (s *ScheduleDayStoreAdapter) DeleteScheduleDayByWeekDay(ctx context.Context, tx *gorm.DB, userID int64, dayOfWeek int) error {
	if err := tx.WithContext(ctx).
		Where("user_id = ? AND week_day = ?", userID, dayOfWeek).
		Delete(&model.ScheduleDayModel{}).Error; err != nil {
		return err
	}
	return nil
}

func (s *ScheduleDayStoreAdapter) GetAllByUserID(ctx context.Context, userID int64) ([]*entity.ScheduleDayEntity, error) {
	scheduleDays := []*model.ScheduleDayModel{}
	if err := s.db.WithContext(ctx).
		Where("user_id = ?", userID).
		Find(&scheduleDays).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleDayEntities(scheduleDays), nil
}

func (s *ScheduleDayStoreAdapter) GetByUserIDAndWeekDay(ctx context.Context, userID int64, dayOfWeek int) ([]*entity.ScheduleDayEntity, error) {
	scheduleDays := []*model.ScheduleDayModel{}
	if err := s.db.WithContext(ctx).
		Where("user_id = ? AND week_day = ?", userID, dayOfWeek).
		Find(&scheduleDays).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleDayEntities(scheduleDays), nil
}

func (s *ScheduleDayStoreAdapter) UpdateScheduleDay(ctx context.Context, tx *gorm.DB, scheduleDayID int64, scheduleDay *entity.ScheduleDayEntity) (*entity.ScheduleDayEntity, error) {
	scheduleDayModel := mapper.ToScheduleDayModel(scheduleDay)
	if err := tx.WithContext(ctx).Model(&model.ScheduleDayModel{}).Where("id = ?", scheduleDayID).
		Updates(scheduleDayModel).Error; err != nil {
		return nil, err
	}
	scheduleDay.UpdatedAt = time.Now().UnixMilli()
	return scheduleDay, nil
}

func (s *ScheduleDayStoreAdapter) GetScheduleDayByID(ctx context.Context, scheduleDayID int64) (*entity.ScheduleDayEntity, error) {
	scheduleDay := &model.ScheduleDayModel{}
	if err := s.db.WithContext(ctx).Where("id = ?", scheduleDayID).First(scheduleDay).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToScheduleDayEntity(scheduleDay), nil
}

func NewScheduleDayStoreAdapter(db *gorm.DB) port.IScheduleDayPort {
	return &ScheduleDayStoreAdapter{db: db}
}
