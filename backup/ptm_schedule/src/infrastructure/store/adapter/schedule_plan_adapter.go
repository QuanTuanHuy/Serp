/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"errors"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/infrastructure/store/mapper"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type SchedulePlanAdapter struct {
	db *gorm.DB
}

func (s *SchedulePlanAdapter) CreateSchedulePlan(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error) {
	schedulePlanModel := mapper.ToSchedulePlanModel(schedulePlan)
	if err := tx.WithContext(ctx).Create(schedulePlanModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToSchedulePlanEntity(schedulePlanModel), nil
}

func (s *SchedulePlanAdapter) DeleteSchedulePlan(ctx context.Context, tx *gorm.DB, ID int64) error {
	return tx.WithContext(ctx).Where("id = ?", ID).Delete(&model.SchedulePlanModel{}).Error
}

func (s *SchedulePlanAdapter) GetSchedulePlanByID(ctx context.Context, ID int64) (*entity.SchedulePlanEntity, error) {
	var schedulePlan model.SchedulePlanModel
	if err := s.db.WithContext(ctx).Where("id = ?", ID).First(&schedulePlan).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToSchedulePlanEntity(&schedulePlan), nil
}

func (s *SchedulePlanAdapter) GetSchedulePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error) {
	var schedulePlan model.SchedulePlanModel
	if err := s.db.WithContext(ctx).Where("user_id = ?", userID).First(&schedulePlan).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToSchedulePlanEntity(&schedulePlan), nil
}

func (s *SchedulePlanAdapter) UpdateSchedulePlan(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error) {
	schedulePlanModel := mapper.ToSchedulePlanModel(schedulePlan)
	if err := tx.WithContext(ctx).Save(schedulePlanModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToSchedulePlanEntity(schedulePlanModel), nil
}

func NewSchedulePlanStoreAdapter(db *gorm.DB) port.ISchedulePlanPort {
	return &SchedulePlanAdapter{
		db: db,
	}
}
