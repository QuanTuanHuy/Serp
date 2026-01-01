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

type ScheduleTaskStoreAdapter struct {
	BaseStoreAdapter
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
	if err := s.WithTx(tx).WithContext(ctx).Create(scheduleTaskModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleTaskEntity(scheduleTaskModel), nil
}

func (s *ScheduleTaskStoreAdapter) CreateBatch(ctx context.Context, tx *gorm.DB, tasks []*entity.ScheduleTaskEntity) error {
	if len(tasks) == 0 {
		return nil
	}
	models := mapper.ToScheduleTaskModels(tasks)
	if err := s.WithTx(tx).WithContext(ctx).Create(&models).Error; err != nil {
		return err
	}

	for i, m := range models {
		tasks[i].ID = m.ID
	}
	return nil
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
	return s.WithTx(tx).WithContext(ctx).
		Where("id = ?", scheduleTaskID).
		Delete(&model.ScheduleTaskModel{}).Error
}

func (s *ScheduleTaskStoreAdapter) DeleteByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error {
	return s.WithTx(tx).WithContext(ctx).
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

func (s *ScheduleTaskStoreAdapter) ListScheduleTasks(ctx context.Context, filter *port.ScheduleTaskFilter) ([]*entity.ScheduleTaskEntity, int64, error) {
	var scheduleTasksModel []*model.ScheduleTaskModel
	var totalCount int64

	query := s.BuildScheduleTaskQuery(filter)
	if err := query.WithContext(ctx).Find(&scheduleTasksModel).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to list schedule tasks: %w", err)
	}

	filter.Limit = 0
	filter.Offset = 0
	countQuery := s.BuildScheduleTaskQuery(filter)
	if err := countQuery.WithContext(ctx).Count(&totalCount).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to count schedule tasks: %w", err)
	}

	return mapper.ToScheduleTaskEntities(scheduleTasksModel), totalCount, nil

}

func (s *ScheduleTaskStoreAdapter) BuildScheduleTaskQuery(filter *port.ScheduleTaskFilter) *gorm.DB {
	if filter == nil {
		filter = port.NewScheduleTaskFilter()
	}

	query := s.db.Model(&model.ScheduleTaskModel{})
	if filter.UserID != nil {
		query = query.Where("user_id = ?", *filter.UserID)
	}
	if filter.PlanID != nil {
		query = query.Where("schedule_plan_id = ?", *filter.PlanID)
	}
	if len(filter.Statuses) > 0 {
		query = query.Where("schedule_status IN ?", filter.Statuses)
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

func (s *ScheduleTaskStoreAdapter) UpdateScheduleTask(ctx context.Context, tx *gorm.DB, ID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	scheduleTaskModel := mapper.ToScheduleTaskModel(scheduleTask)
	if err := s.WithTx(tx).WithContext(ctx).Where("id = ?", ID).Updates(scheduleTaskModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToScheduleTaskEntity(scheduleTaskModel), nil
}

func (s *ScheduleTaskStoreAdapter) UpdateScheduleStatusBatch(ctx context.Context, tx *gorm.DB, ids []int64, status enum.ScheduleTaskStatus, reason *string) error {
	if len(ids) == 0 {
		return nil
	}

	updates := map[string]any{
		"schedule_status": status,
	}
	if reason != nil {
		updates["unscheduled_reason"] = *reason
	} else {
		updates["unscheduled_reason"] = nil
	}

	return s.WithTx(tx).WithContext(ctx).
		Model(&model.ScheduleTaskModel{}).
		Where("id IN ?", ids).
		Updates(updates).Error
}

func (s *ScheduleTaskStoreAdapter) DeleteByPlanID(ctx context.Context, tx *gorm.DB, planID int64) error {
	return s.WithTx(tx).WithContext(ctx).
		Where("schedule_plan_id = ?", planID).
		Delete(&model.ScheduleTaskModel{}).Error
}

func NewScheduleTaskStoreAdapter(db *gorm.DB) port.IScheduleTaskPort {
	return &ScheduleTaskStoreAdapter{
		BaseStoreAdapter: BaseStoreAdapter{db: db},
	}
}
