/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"errors"
	"fmt"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/infrastructure/store/mapper"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type SchedulePlanAdapter struct {
	db *gorm.DB
}

func (s *SchedulePlanAdapter) GetPlanByID(ctx context.Context, ID int64) (*entity.SchedulePlanEntity, error) {
	var plan model.SchedulePlanModel
	if err := s.db.WithContext(ctx).Where("id = ?", ID).First(&plan).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToSchedulePlanEntity(&plan), nil
}

func (s *SchedulePlanAdapter) CreatePlan(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error) {
	if tx == nil {
		tx = s.db
	}
	planModel := mapper.ToSchedulePlanModel(plan)
	if err := tx.WithContext(ctx).Create(planModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToSchedulePlanEntity(planModel), nil
}

func (s *SchedulePlanAdapter) DeletePlan(ctx context.Context, tx *gorm.DB, ID int64) error {
	return tx.WithContext(ctx).Where("id = ?", ID).Delete(&model.SchedulePlanModel{}).Error
}

func (s *SchedulePlanAdapter) GetActivePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error) {
	var plan model.SchedulePlanModel
	if err := s.db.WithContext(ctx).Where("user_id = ? AND status = ?", userID, enum.PlanActive).First(&plan).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToSchedulePlanEntity(&plan), nil
}

func (s *SchedulePlanAdapter) ListPlansByUserID(ctx context.Context, userID int64, filter *port.PlanFilter) ([]*entity.SchedulePlanEntity, error) {
	var plans []model.SchedulePlanModel
	query := s.BuildPlanQuery(userID, filter)
	if err := query.WithContext(ctx).Find(&plans).Error; err != nil {
		return nil, fmt.Errorf("failed to list plans: %w", err)
	}
	return mapper.ToSchedulePlanEntities(plans), nil
}

func (s *SchedulePlanAdapter) UpdatePlan(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error) {
	planModel := mapper.ToSchedulePlanModel(plan)
	if err := tx.WithContext(ctx).Save(planModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToSchedulePlanEntity(planModel), nil
}

func (c *SchedulePlanAdapter) BuildPlanQuery(userID int64, filter *port.PlanFilter) *gorm.DB {
	if filter == nil {
		filter = port.NewPlanFilter()
	}

	query := c.db.Where("user_id = ?", userID)

	if len(filter.Statuses) > 0 {
		query = query.Where("status IN ?", filter.Statuses)
	}
	if len(filter.Algorithms) > 0 {
		query = query.Where("algorithm_used IN ?", filter.Algorithms)
	}
	if filter.DateMs != nil {
		query = query.Where("start_date_ms <= ? AND (end_date_ms IS NULL OR end_date_ms >= ?)", *filter.DateMs, *filter.DateMs)
	}

	if filter.SortBy != "" && filter.SortOrder != "" {
		query = query.Order(fmt.Sprintf("%s %s", filter.SortBy, filter.SortOrder))
	}

	if filter.Limit > 0 {
		query = query.Limit(filter.Limit)
	}
	if filter.Offset > 0 {
		query = query.Offset(filter.Offset)
	}

	return query
}

func (s *SchedulePlanAdapter) UpdatePlanStatus(ctx context.Context, tx *gorm.DB, planID int64, status string) error {
	return tx.WithContext(ctx).Model(&model.SchedulePlanModel{}).Where("id = ?", planID).Update("status", status).Error
}

func NewSchedulePlanStoreAdapter(db *gorm.DB) port.ISchedulePlanPort {
	return &SchedulePlanAdapter{
		db: db,
	}
}
