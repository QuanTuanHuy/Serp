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

type ScheduleTaskStoreAdapter struct {
	db *gorm.DB
}

func (s *ScheduleTaskStoreAdapter) GetScheduleTaskByTaskID(ctx context.Context, taskID int64) ([]*entity.ScheduleTaskEntity, error) {
	var scheduleTasksModel []*model.ScheduleTaskModel
	if err := s.db.WithContext(ctx).
		Where("task_id = ?", taskID).
		Find(&scheduleTasksModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleTaskEntities(scheduleTasksModel), nil
}

func (s *ScheduleTaskStoreAdapter) GetScheduleTasksByIDs(ctx context.Context, scheduleTaskIDs []int64) ([]*entity.ScheduleTaskEntity, error) {
	var scheduleTasksModel []*model.ScheduleTaskModel
	if err := s.db.WithContext(ctx).
		Where("id IN ?", scheduleTaskIDs).
		Find(&scheduleTasksModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleTaskEntities(scheduleTasksModel), nil
}

func (s *ScheduleTaskStoreAdapter) CreateScheduleTask(ctx context.Context, tx *gorm.DB, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	scheduleTaskModel := mapper.ToScheduleTaskModel(scheduleTask)
	if err := tx.WithContext(ctx).Create(scheduleTaskModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleTaskEntity(scheduleTaskModel), nil
}

func (s *ScheduleTaskStoreAdapter) GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error) {
	var scheduleTasksModel []*model.ScheduleTaskModel
	if err := s.db.WithContext(ctx).
		Where("schedule_plan_id = ?", schedulePlanID).
		Find(&scheduleTasksModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleTaskEntities(scheduleTasksModel), nil
}

func (s *ScheduleTaskStoreAdapter) GetScheduleTaskByID(ctx context.Context, scheduleTaskID int64) (*entity.ScheduleTaskEntity, error) {
	var scheduleTaskModel model.ScheduleTaskModel
	if err := s.db.WithContext(ctx).
		Where("id = ?", scheduleTaskID).
		First(&scheduleTaskModel).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToScheduleTaskEntity(&scheduleTaskModel), nil
}

func (s *ScheduleTaskStoreAdapter) DeleteScheduleTask(ctx context.Context, tx *gorm.DB, scheduleTaskID int64) error {
	return tx.WithContext(ctx).
		Where("id = ?", scheduleTaskID).
		Delete(&model.ScheduleTaskModel{}).Error
}

func (s *ScheduleTaskStoreAdapter) DeleteByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error {
	return tx.WithContext(ctx).
		Where("task_id = ?", taskID).
		Delete(&model.ScheduleTaskModel{}).Error
}

func (s *ScheduleTaskStoreAdapter) GetByPlanIDAndTaskID(ctx context.Context, planID, taskID int64) (*entity.ScheduleTaskEntity, error) {
	var scheduleTaskModel model.ScheduleTaskModel
	if err := s.db.WithContext(ctx).
		Where("schedule_plan_id = ? AND task_id = ?", planID, taskID).
		First(&scheduleTaskModel).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToScheduleTaskEntity(&scheduleTaskModel), nil
}

func (s *ScheduleTaskStoreAdapter) UpdateScheduleTask(ctx context.Context, tx *gorm.DB, ID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	scheduleTaskModel := mapper.ToScheduleTaskModel(scheduleTask)
	if err := tx.WithContext(ctx).Where("id = ?", ID).Updates(scheduleTaskModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleTaskEntity(scheduleTaskModel), nil
}

func NewScheduleTaskStoreAdapter(db *gorm.DB) port.IScheduleTaskPort {
	return &ScheduleTaskStoreAdapter{
		db: db,
	}
}
