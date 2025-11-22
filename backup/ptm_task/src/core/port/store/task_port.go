/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type ITaskPort interface {
	CreateTask(ctx context.Context, tx *gorm.DB, task *entity.TaskEntity) (*entity.TaskEntity, error)
	UpdateTask(ctx context.Context, tx *gorm.DB, taskID int64, task *entity.TaskEntity) (*entity.TaskEntity, error)
	GetTaskByID(ctx context.Context, taskID int64) (*entity.TaskEntity, error)
	GetTasksByGroupTaskID(ctx context.Context, groupTaskID int64) ([]*entity.TaskEntity, error)
	DeleteTask(ctx context.Context, tx *gorm.DB, taskID int64) error
}
