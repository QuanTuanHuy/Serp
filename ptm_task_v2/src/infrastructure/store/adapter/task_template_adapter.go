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

type TaskTemplateAdapter struct {
	db     *gorm.DB
	mapper *mapper.TaskTemplateMapper
}

func NewTaskTemplateAdapter(db *gorm.DB) store.ITaskTemplatePort {
	return &TaskTemplateAdapter{
		db:     db,
		mapper: mapper.NewTaskTemplateMapper(),
	}
}

// CreateTaskTemplate creates a new task template
func (a *TaskTemplateAdapter) CreateTaskTemplate(ctx context.Context, tx *gorm.DB, template *entity.TaskTemplateEntity) error {
	db := a.getDB(tx)
	templateModel := a.mapper.ToModel(template)

	if err := db.WithContext(ctx).Create(templateModel).Error; err != nil {
		return fmt.Errorf("failed to create task template: %w", err)
	}

	template.ID = templateModel.ID
	template.CreatedAt = templateModel.CreatedAt.UnixMilli()
	template.UpdatedAt = templateModel.UpdatedAt.UnixMilli()

	return nil
}

// GetTaskTemplateByID retrieves a task template by ID
func (a *TaskTemplateAdapter) GetTaskTemplateByID(ctx context.Context, id int64) (*entity.TaskTemplateEntity, error) {
	var templateModel model.TaskTemplateModel

	if err := a.db.WithContext(ctx).First(&templateModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get task template by id: %w", err)
	}

	return a.mapper.ToEntity(&templateModel), nil
}

// GetTaskTemplatesByUserID retrieves task templates for a user with filters
func (a *TaskTemplateAdapter) GetTaskTemplatesByUserID(ctx context.Context, userID int64, filter *store.TaskTemplateFilter) ([]*entity.TaskTemplateEntity, error) {
	var templateModels []*model.TaskTemplateModel

	query := a.buildTemplateQuery(userID, filter)

	if err := query.WithContext(ctx).Find(&templateModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get task templates by user id: %w", err)
	}

	return a.mapper.ToEntities(templateModels), nil
}

// GetFavoriteTemplates retrieves favorite templates for a user
func (a *TaskTemplateAdapter) GetFavoriteTemplates(ctx context.Context, userID int64) ([]*entity.TaskTemplateEntity, error) {
	var templateModels []*model.TaskTemplateModel

	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND is_favorite = ? AND active_status = ?", userID, true, "ACTIVE").
		Order("usage_count DESC, created_at DESC").
		Find(&templateModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get favorite templates: %w", err)
	}

	return a.mapper.ToEntities(templateModels), nil
}

// CountTaskTemplatesByUserID counts task templates for a user
func (a *TaskTemplateAdapter) CountTaskTemplatesByUserID(ctx context.Context, userID int64) (int64, error) {
	var count int64

	if err := a.db.WithContext(ctx).Model(&model.TaskTemplateModel{}).
		Where("user_id = ? AND active_status = ?", userID, "ACTIVE").
		Count(&count).Error; err != nil {
		return 0, fmt.Errorf("failed to count task templates: %w", err)
	}

	return count, nil
}

// UpdateTaskTemplate updates a task template
func (a *TaskTemplateAdapter) UpdateTaskTemplate(ctx context.Context, tx *gorm.DB, template *entity.TaskTemplateEntity) error {
	db := a.getDB(tx)
	templateModel := a.mapper.ToModel(template)

	if err := db.WithContext(ctx).Save(templateModel).Error; err != nil {
		return fmt.Errorf("failed to update task template: %w", err)
	}

	template.UpdatedAt = templateModel.UpdatedAt.UnixMilli()

	return nil
}

// UpdateTemplateUsage updates template usage count and last used time
func (a *TaskTemplateAdapter) UpdateTemplateUsage(ctx context.Context, tx *gorm.DB, templateID int64, currentTimeMs int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskTemplateModel{}).
		Where("id = ?", templateID).
		Updates(map[string]interface{}{
			"usage_count":  gorm.Expr("usage_count + 1"),
			"last_used_ms": currentTimeMs,
		}).Error; err != nil {
		return fmt.Errorf("failed to update template usage: %w", err)
	}

	return nil
}

// ToggleFavorite toggles favorite status of a template
func (a *TaskTemplateAdapter) ToggleFavorite(ctx context.Context, tx *gorm.DB, templateID int64, isFavorite bool) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskTemplateModel{}).
		Where("id = ?", templateID).
		Update("is_favorite", isFavorite).Error; err != nil {
		return fmt.Errorf("failed to toggle template favorite: %w", err)
	}

	return nil
}

// SoftDeleteTaskTemplate soft deletes a task template
func (a *TaskTemplateAdapter) SoftDeleteTaskTemplate(ctx context.Context, tx *gorm.DB, templateID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskTemplateModel{}).
		Where("id = ?", templateID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete task template: %w", err)
	}

	return nil
}

// Helper functions

func (a *TaskTemplateAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}

func (a *TaskTemplateAdapter) buildTemplateQuery(userID int64, filter *store.TaskTemplateFilter) *gorm.DB {
	if filter == nil {
		filter = store.NewTaskTemplateFilter()
	}

	query := a.db.Where("user_id = ? AND active_status = ?", userID, "ACTIVE")

	// Category filter
	if filter.Category != nil {
		query = query.Where("category = ?", *filter.Category)
	}

	// Favorite filter
	if filter.IsFavorite != nil {
		query = query.Where("is_favorite = ?", *filter.IsFavorite)
	}

	// Name search
	if filter.NameContains != nil && *filter.NameContains != "" {
		searchPattern := fmt.Sprintf("%%%s%%", *filter.NameContains)
		query = query.Where("name ILIKE ?", searchPattern)
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
