/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	port "github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ProjectStoreAdapter struct {
	db *gorm.DB
}

func (p *ProjectStoreAdapter) UpdateProject(ctx context.Context, tx *gorm.DB, projectID int64, project *entity.ProjectEntity) (*entity.ProjectEntity, error) {
	projectModel := mapper.ToProjectModel(project)
	if err := tx.WithContext(ctx).
		Model(&model.ProjectModel{}).
		Where("id = ?", projectID).
		Updates(projectModel).Error; err != nil {
		return nil, err
	}
	return project, nil
}

func (p *ProjectStoreAdapter) GetProjects(ctx context.Context, params *request.GetProjectParams) ([]*entity.ProjectEntity, int64, error) {
	var projects []*model.ProjectModel
	var count int64

	query := p.db.WithContext(ctx)
	if params.UserID != nil {
		query = query.Where("owner_id = ?", *params.UserID)
	}
	if params.Status != nil {
		query = query.Where("status = ?", *params.Status)
	}
	if params.ActiveStatus != nil {
		query = query.Where("active_status = ?", *params.ActiveStatus)
	}
	if params.Limit != nil && params.Offset != nil {
		query = query.Limit(int(*params.Limit)).Offset(int(*params.Offset))
	}

	if err := query.Find(&projects).Count(&count).Error; err != nil {
		return nil, 0, err
	}

	return mapper.ToProjectEntityList(projects), count, nil
}

func (p *ProjectStoreAdapter) CreateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) (*entity.ProjectEntity, error) {
	projectModel := mapper.ToProjectModel(project)
	if err := tx.WithContext(ctx).Create(projectModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToProjectEntity(projectModel), nil
}

func (p *ProjectStoreAdapter) GetDefaultProjectByUserID(ctx context.Context, userID int64) (*entity.ProjectEntity, error) {
	var project model.ProjectModel
	if err := p.db.WithContext(ctx).
		Where("owner_id = ? AND is_default = ? AND active_status = ?", userID, true, enum.Active).
		First(&project).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToProjectEntity(&project), nil
}

func (p *ProjectStoreAdapter) GetProjectByID(ctx context.Context, ID int64) (*entity.ProjectEntity, error) {
	var project model.ProjectModel
	if err := p.db.WithContext(ctx).First(&project, ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToProjectEntity(&project), nil
}

func (p *ProjectStoreAdapter) GetProjectsByUserID(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error) {
	var projects []*model.ProjectModel
	if err := p.db.WithContext(ctx).Where("owner_id = ? AND active_status = ?", userID, enum.Active).Find(&projects).Error; err != nil {
		return nil, err
	}
	return mapper.ToProjectEntityList(projects), nil
}

func NewProjectStoreAdapter(db *gorm.DB) port.IProjectPort {
	return &ProjectStoreAdapter{
		db: db,
	}
}
