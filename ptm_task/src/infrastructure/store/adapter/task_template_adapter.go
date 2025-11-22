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

func (a *TaskTemplateAdapter) CreateTaskTemplate(ctx context.Context, tx *gorm.DB, template *entity.TaskTemplateEntity) error {
	db := a.getDB(tx)
	templateModel := a.mapper.ToModel(template)

	if err := db.WithContext(ctx).Create(templateModel).Error; err != nil {
		return fmt.Errorf("failed to create task template: %w", err)
	}
	return nil
}

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

func (a *TaskTemplateAdapter) GetTaskTemplatesByUserID(ctx context.Context, userID int64, filter *store.TaskTemplateFilter) ([]*entity.TaskTemplateEntity, error) {
	var templateModels []*model.TaskTemplateModel

	query := a.buildTemplateQuery(userID, filter)

	if err := query.WithContext(ctx).Find(&templateModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get task templates by user id: %w", err)
	}

	return a.mapper.ToEntities(templateModels), nil
}

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

func (a *TaskTemplateAdapter) CountTaskTemplatesByUserID(ctx context.Context, userID int64) (int64, error) {
	var count int64

	if err := a.db.WithContext(ctx).Model(&model.TaskTemplateModel{}).
		Where("user_id = ? AND active_status = ?", userID, "ACTIVE").
		Count(&count).Error; err != nil {
		return 0, fmt.Errorf("failed to count task templates: %w", err)
	}

	return count, nil
}

func (a *TaskTemplateAdapter) UpdateTaskTemplate(ctx context.Context, tx *gorm.DB, template *entity.TaskTemplateEntity) error {
	db := a.getDB(tx)
	templateModel := a.mapper.ToModel(template)

	if err := db.WithContext(ctx).Save(templateModel).Error; err != nil {
		return fmt.Errorf("failed to update task template: %w", err)
	}
	return nil
}

func (a *TaskTemplateAdapter) UpdateTemplateUsage(ctx context.Context, tx *gorm.DB, templateID int64, currentTimeMs int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskTemplateModel{}).
		Where("id = ?", templateID).
		Updates(map[string]any{
			"usage_count":  gorm.Expr("usage_count + 1"),
			"last_used_at": currentTimeMs,
		}).Error; err != nil {
		return fmt.Errorf("failed to update template usage: %w", err)
	}

	return nil
}

func (a *TaskTemplateAdapter) ToggleFavorite(ctx context.Context, tx *gorm.DB, templateID int64, isFavorite bool) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskTemplateModel{}).
		Where("id = ?", templateID).
		Update("is_favorite", isFavorite).Error; err != nil {
		return fmt.Errorf("failed to toggle template favorite: %w", err)
	}

	return nil
}

func (a *TaskTemplateAdapter) SoftDeleteTaskTemplate(ctx context.Context, tx *gorm.DB, templateID int64) error {
	db := a.getDB(tx)

	if err := db.WithContext(ctx).Model(&model.TaskTemplateModel{}).
		Where("id = ?", templateID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete task template: %w", err)
	}

	return nil
}

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

	if filter.Category != nil {
		query = query.Where("category = ?", *filter.Category)
	}
	if filter.IsFavorite != nil {
		query = query.Where("is_favorite = ?", *filter.IsFavorite)
	}

	if filter.NameContains != nil && *filter.NameContains != "" {
		searchPattern := "%" + *filter.NameContains + "%"
		query = query.Where("template_name ILIKE ?", searchPattern)
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
