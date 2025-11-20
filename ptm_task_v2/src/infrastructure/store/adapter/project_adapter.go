/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
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

func (a *ProjectAdapter) CreateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) error {
	db := a.getDB(tx)
	projectModel := a.mapper.ToModel(project)

	if err := db.WithContext(ctx).Create(projectModel).Error; err != nil {
		return fmt.Errorf("failed to create project: %w", err)
	}

	project.ID = projectModel.ID
	project.CreatedAt = projectModel.CreatedAt.UnixMilli()
	project.UpdatedAt = projectModel.UpdatedAt.UnixMilli()

	return nil
}

func (a *ProjectAdapter) CreateProjects(ctx context.Context, tx *gorm.DB, projects []*entity.ProjectEntity) error {
	if len(projects) == 0 {
		return nil
	}

	db := a.getDB(tx)
	projectModels := a.mapper.ToModels(projects)

	if err := db.WithContext(ctx).CreateInBatches(projectModels, 100).Error; err != nil {
		return fmt.Errorf("failed to create projects: %w", err)
	}

	return nil
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

func (a *ProjectAdapter) GetProjectsByIDs(ctx context.Context, ids []int64) ([]*entity.ProjectEntity, error) {
	if len(ids) == 0 {
		return []*entity.ProjectEntity{}, nil
	}

	var projectModels []*model.ProjectModel

	if err := a.db.WithContext(ctx).Where("id IN ?", ids).Find(&projectModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get projects by ids: %w", err)
	}

	return a.mapper.ToEntities(projectModels), nil
}

func (a *ProjectAdapter) GetProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error) {
	var projectModels []*model.ProjectModel

	query := a.buildProjectQuery(userID, filter)

	if err := query.WithContext(ctx).Find(&projectModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get projects by user id: %w", err)
	}

	return a.mapper.ToEntities(projectModels), nil
}

func (a *ProjectAdapter) CountProjectsByUserID(ctx context.Context, userID int64, filter *store.ProjectFilter) (int64, error) {
	var count int64

	query := a.buildProjectQuery(userID, filter)

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

	project.UpdatedAt = projectModel.UpdatedAt.UnixMilli()

	return nil
}

func (a *ProjectAdapter) UpdateProjectStatus(ctx context.Context, tx *gorm.DB, projectID int64, status string) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.ProjectModel{}).
		Where("id = ?", projectID).
		Update("status", status).Error; err != nil {
		return fmt.Errorf("failed to update project status: %w", err)
	}

	return nil
}

func (a *ProjectAdapter) UpdateProjectProgress(ctx context.Context, tx *gorm.DB, projectID int64, progressPercentage int) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.ProjectModel{}).
		Where("id = ?", projectID).
		Update("progress_percentage", progressPercentage).Error; err != nil {
		return fmt.Errorf("failed to update project progress: %w", err)
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

func (a *ProjectAdapter) SoftDeleteProjects(ctx context.Context, tx *gorm.DB, projectIDs []int64) error {
	if len(projectIDs) == 0 {
		return nil
	}

	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.ProjectModel{}).
		Where("id IN ?", projectIDs).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete projects: %w", err)
	}

	return nil
}

func (a *ProjectAdapter) GetProjectStats(ctx context.Context, projectID int64) (*store.ProjectStats, error) {

	var projectModel model.ProjectModel

	if err := a.db.WithContext(ctx).First(&projectModel, projectID).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get project stats: %w", err)
	}

	stats := &store.ProjectStats{
		ProjectID:          projectID,
		TotalTasks:         projectModel.TotalTasks,
		CompletedTasks:     projectModel.CompletedTasks,
		ProgressPercentage: projectModel.ProgressPercentage,
	}

	return stats, nil
}

func (a *ProjectAdapter) GetProjectsWithStats(ctx context.Context, userID int64, filter *store.ProjectFilter) ([]*entity.ProjectEntity, error) {
	return a.GetProjectsByUserID(ctx, userID, filter)
}

func (a *ProjectAdapter) GetFavoriteProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error) {
	var projectModels []*model.ProjectModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND is_favorite = ? AND active_status = ?", userID, true, "ACTIVE").
		Order("updated_at DESC").
		Find(&projectModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get favorite projects: %w", err)
	}

	return a.mapper.ToEntities(projectModels), nil
}

func (a *ProjectAdapter) GetOverdueProjects(ctx context.Context, userID int64, currentTimeMs int64) ([]*entity.ProjectEntity, error) {
	var projectModels []*model.ProjectModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND deadline_ms < ? AND status NOT IN ? AND active_status = ?",
			userID, currentTimeMs, []string{"COMPLETED", "ARCHIVED"}, "ACTIVE").
		Order("deadline_ms ASC").
		Find(&projectModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get overdue projects: %w", err)
	}

	return a.mapper.ToEntities(projectModels), nil
}

// Helper functions

func (a *ProjectAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *ProjectAdapter) buildProjectQuery(userID int64, filter *store.ProjectFilter) *gorm.DB {
	if filter == nil {
		filter = store.NewProjectFilter()
	}

	query := a.db.Where("user_id = ?", userID)

	// Status filter
	if len(filter.Statuses) > 0 {
		query = query.Where("status IN ?", filter.Statuses)
	}

	// Active status filter
	if filter.ActiveStatus != nil {
		query = query.Where("active_status = ?", *filter.ActiveStatus)
	} else {
		query = query.Where("active_status = ?", "ACTIVE")
	}

	// Priority filter
	if len(filter.Priorities) > 0 {
		query = query.Where("priority IN ?", filter.Priorities)
	}

	// Deadline filter
	if filter.DeadlineFrom != nil {
		query = query.Where("deadline_ms >= ?", *filter.DeadlineFrom)
	}
	if filter.DeadlineTo != nil {
		query = query.Where("deadline_ms <= ?", *filter.DeadlineTo)
	}

	// Created date filter
	if filter.CreatedFrom != nil {
		query = query.Where("created_at >= ?", *filter.CreatedFrom)
	}
	if filter.CreatedTo != nil {
		query = query.Where("created_at <= ?", *filter.CreatedTo)
	}

	// Favorite filter
	if filter.IsFavorite != nil {
		query = query.Where("is_favorite = ?", *filter.IsFavorite)
	}

	// Progress filter
	if filter.MinProgress != nil {
		query = query.Where("progress_percentage >= ?", *filter.MinProgress)
	}
	if filter.MaxProgress != nil {
		query = query.Where("progress_percentage <= ?", *filter.MaxProgress)
	}

	// Has tasks filter
	if filter.HasTasks != nil {
		if *filter.HasTasks {
			query = query.Where("total_tasks > 0")
		} else {
			query = query.Where("total_tasks = 0")
		}
	}

	// Sorting
	if filter.SortBy != "" && filter.SortOrder != "" {
		query = query.Order(fmt.Sprintf("%s %s", filter.SortBy, filter.SortOrder))
	}

	// Pagination
	if filter.Limit > 0 {
		query = query.Limit(filter.Limit)
	}
	if filter.Offset > 0 {
		query = query.Offset(filter.Offset)
	}

	return query
}
