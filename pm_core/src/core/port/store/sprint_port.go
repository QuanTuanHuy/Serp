/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/pm-core/src/core/domain/entity"
	"gorm.io/gorm"
)

type ISprintPort interface {
	CreateSprint(ctx context.Context, tx *gorm.DB, sprint *entity.SprintEntity) (*entity.SprintEntity, error)
	GetSprintByID(ctx context.Context, id int64) (*entity.SprintEntity, error)
	GetSprintsByProjectID(ctx context.Context, projectID int64) ([]*entity.SprintEntity, error)
	UpdateSprint(ctx context.Context, tx *gorm.DB, sprint *entity.SprintEntity) error
	SoftDeleteSprint(ctx context.Context, tx *gorm.DB, sprintID int64) error
}
