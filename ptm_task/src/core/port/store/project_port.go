/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type IProjectPort interface {
	CreateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) (*entity.ProjectEntity, error)
	UpdateProject(ctx context.Context, tx *gorm.DB, projectID int64, project *entity.ProjectEntity) (*entity.ProjectEntity, error)
	GetProjectByID(ctx context.Context, ID int64) (*entity.ProjectEntity, error)
	GetDefaultProjectByUserID(ctx context.Context, userID int64) (*entity.ProjectEntity, error)
	GetProjects(ctx context.Context, params *request.GetProjectParams) ([]*entity.ProjectEntity, int64, error)
	GetProjectsByUserID(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
}
