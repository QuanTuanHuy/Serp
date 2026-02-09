/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/core/port/store"
	"github.com/serp/pm-core/src/infrastructure/store/mapper"
	"github.com/serp/pm-core/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ProjectAdapter struct {
	db     *gorm.DB
	mapper *mapper.ProjectMapper
}

func NewProjectAdapter(db *gorm.DB) store.IProjectPort {
	return &ProjectAdapter{
		db:     db,
		mapper: mapper.NewProjectMapper(),
	}
}

func (a *ProjectAdapter) CreateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) (*entity.ProjectEntity, error) {
	db := a.getDB(tx)
	projectModel := a.mapper.ToModel(project)
	if err := db.WithContext(ctx).Create(projectModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create project: %w", err)
	}
	return a.mapper.ToEntity(projectModel), nil
}

func (a *ProjectAdapter) GetProjectByID(ctx context.Context, id int64) (*entity.ProjectEntity, error) {
	var projectModel model.ProjectModel

	if err := a.db.WithContext(ctx).First(&projectModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get project by id: %w", err)
	}
	return a.mapper.ToEntity(&projectModel), nil
}

func (a *ProjectAdapter) GetProjectByKey(ctx context.Context, tenantID int64, key string) (*entity.ProjectEntity, error) {
	var projectModel model.ProjectModel

	if err := a.db.WithContext(ctx).
		Where("tenant_id = ? AND key = ? AND active_status = ?", tenantID, key, "ACTIVE").
		First(&projectModel).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get project by key: %w", err)
	}
	return a.mapper.ToEntity(&projectModel), nil
}

func (a *ProjectAdapter) GetProjectsByTenantID(ctx context.Context, tenantID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error) {
	var projectModels []*model.ProjectModel

	query := a.buildProjectQuery(tenantID, filter)
	if err := query.WithContext(ctx).Find(&projectModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get projects by tenant id: %w", err)
	}
	return a.mapper.ToEntities(projectModels), nil
}

func (a *ProjectAdapter) CountProjectsByTenantID(ctx context.Context, tenantID int64, filter *store.ProjectFilter) (int64, error) {
	var count int64
	if filter != nil {
		filter.Limit = 0
		filter.Offset = 0
	}

	query := a.buildProjectQuery(tenantID, filter)
	if err := query.WithContext(ctx).Model(&model.ProjectModel{}).Count(&count).Error; err != nil {
		return 0, fmt.Errorf("failed to count projects: %w", err)
	}
	return count, nil
}

func (a *ProjectAdapter) UpdateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) error {
	db := a.getDB(tx)
	projectModel := a.mapper.ToModel(project)

	if err := db.WithContext(ctx).Save(projectModel).Error; err != nil {
		return fmt.Errorf("failed to update project: %w", err)
	}

	return nil
}

func (a *ProjectAdapter) SoftDeleteProject(ctx context.Context, tx *gorm.DB, projectID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.ProjectModel{}).
		Where("id = ?", projectID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete project: %w", err)
	}

	return nil
}

func (a *ProjectAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *ProjectAdapter) buildProjectQuery(tenantID int64, filter *store.ProjectFilter) *gorm.DB {
	if filter == nil {
		filter = store.NewProjectFilter()
	}

	query := a.db.Where("tenant_id = ?", tenantID)

	if len(filter.Statuses) > 0 {
		query = query.Where("status IN ?", filter.Statuses)
	}
	if filter.ActiveStatus != nil {
		query = query.Where("active_status = ?", *filter.ActiveStatus)
	} else {
		query = query.Where("active_status = ?", "ACTIVE")
	}
	if filter.Visibility != nil {
		query = query.Where("visibility = ?", *filter.Visibility)
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
