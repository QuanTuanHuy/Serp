/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	port "github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type TaskStoreAdapter struct {
	db *gorm.DB
}

func (t *TaskStoreAdapter) CreateTask(ctx context.Context, tx *gorm.DB, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	taskModel := mapper.ToTaskModel(task)
	if err := tx.WithContext(ctx).Create(taskModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToTaskEntity(taskModel), nil
}

func (t *TaskStoreAdapter) DeleteTask(ctx context.Context, tx *gorm.DB, taskID int64) error {
	if err := tx.WithContext(ctx).
		Where("id = ?", taskID).
		Delete(&model.TaskModel{}).Error; err != nil {
		return err
	}
	return nil
}

func (t *TaskStoreAdapter) GetTaskByID(ctx context.Context, taskID int64) (*entity.TaskEntity, error) {
	var task model.TaskModel
	if err := t.db.WithContext(ctx).Where("id = ?", taskID).
		First(&task).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToTaskEntity(&task), nil
}

func (t *TaskStoreAdapter) GetTasksByGroupTaskID(ctx context.Context, groupTaskID int64) ([]*entity.TaskEntity, error) {
	var tasks []*model.TaskModel
	if err := t.db.WithContext(ctx).Where("group_task_id = ?", groupTaskID).
		Find(&tasks).Error; err != nil {
		return nil, err
	}
	return mapper.ToTaskEntityList(tasks), nil
}

func (t *TaskStoreAdapter) UpdateTask(ctx context.Context, tx *gorm.DB, taskID int64, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	taskModel := mapper.ToTaskModel(task)
	if err := tx.WithContext(ctx).
		Model(&model.TaskModel{}).
		Where("id = ?", taskID).
		Updates(taskModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToTaskEntity(taskModel), nil
}

func NewTaskStoreAdapter(db *gorm.DB) port.ITaskPort {
	return &TaskStoreAdapter{db: db}
}
