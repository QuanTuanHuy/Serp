/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type ITaskTemplateUseCase interface {
	// Create operations
	CreateTemplate(ctx context.Context, userID int64, template *entity.TaskTemplateEntity) (*entity.TaskTemplateEntity, error)

	// Update operations
	UpdateTemplate(ctx context.Context, userID int64, template *entity.TaskTemplateEntity) error
	ToggleFavorite(ctx context.Context, userID int64, templateID int64, isFavorite bool) error

	// Delete operations
	DeleteTemplate(ctx context.Context, userID int64, templateID int64) error

	// Query operations
	GetTemplateByID(ctx context.Context, userID int64, templateID int64) (*entity.TaskTemplateEntity, error)
	GetTemplatesByUserID(ctx context.Context, userID int64, filter *store.TaskTemplateFilter) ([]*entity.TaskTemplateEntity, error)
	GetFavoriteTemplates(ctx context.Context, userID int64) ([]*entity.TaskTemplateEntity, error)
}

type taskTemplateUseCase struct {
	db              *gorm.DB
	templateService service.ITaskTemplateService
	txService       service.ITransactionService
}

func NewTaskTemplateUseCase(
	db *gorm.DB,
	templateService service.ITaskTemplateService,
	txService service.ITransactionService,
) ITaskTemplateUseCase {
	return &taskTemplateUseCase{
		db:              db,
		templateService: templateService,
		txService:       txService,
	}
}

func (u *taskTemplateUseCase) CreateTemplate(ctx context.Context, userID int64, template *entity.TaskTemplateEntity) (*entity.TaskTemplateEntity, error) {
	var createdTemplate *entity.TaskTemplateEntity
	err := u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		created, err := u.templateService.CreateTemplate(ctx, tx, userID, template)
		if err != nil {
			return err
		}
		createdTemplate = created
		return nil
	})
	if err != nil {
		return nil, err
	}
	return createdTemplate, nil
}

func (u *taskTemplateUseCase) UpdateTemplate(ctx context.Context, userID int64, template *entity.TaskTemplateEntity) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		return u.templateService.UpdateTemplate(ctx, tx, userID, template)
	})
}

func (u *taskTemplateUseCase) ToggleFavorite(ctx context.Context, userID int64, templateID int64, isFavorite bool) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		return u.templateService.ToggleFavorite(ctx, tx, userID, templateID, isFavorite)
	})
}

func (u *taskTemplateUseCase) DeleteTemplate(ctx context.Context, userID int64, templateID int64) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		return u.templateService.DeleteTemplate(ctx, tx, userID, templateID)
	})
}

func (u *taskTemplateUseCase) GetTemplateByID(ctx context.Context, userID int64, templateID int64) (*entity.TaskTemplateEntity, error) {
	template, err := u.templateService.GetTemplateByID(ctx, templateID)
	if err != nil {
		return nil, err
	}
	if err := u.templateService.ValidateTemplateOwnership(userID, template); err != nil {
		return nil, err
	}
	return template, nil
}

func (u *taskTemplateUseCase) GetTemplatesByUserID(ctx context.Context, userID int64, filter *store.TaskTemplateFilter) ([]*entity.TaskTemplateEntity, error) {
	return u.templateService.GetTemplatesByUserID(ctx, userID, filter)
}

func (u *taskTemplateUseCase) GetFavoriteTemplates(ctx context.Context, userID int64) ([]*entity.TaskTemplateEntity, error) {
	return u.templateService.GetFavoriteTemplates(ctx, userID)
}
