/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"time"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type ITaskTemplateService interface {
	// Validation
	ValidateTemplateData(template *entity.TaskTemplateEntity) error
	ValidateTemplateOwnership(userID int64, template *entity.TaskTemplateEntity) error

	// Template operations
	CreateTemplate(ctx context.Context, tx *gorm.DB, userID int64, template *entity.TaskTemplateEntity) (*entity.TaskTemplateEntity, error)
	UpdateTemplate(ctx context.Context, tx *gorm.DB, userID int64, template *entity.TaskTemplateEntity) error
	DeleteTemplate(ctx context.Context, tx *gorm.DB, userID int64, templateID int64) error
	SetFavorite(ctx context.Context, tx *gorm.DB, userID int64, templateID int64, isFavorite bool) error
	IncrementUsageCount(ctx context.Context, tx *gorm.DB, templateID int64) error

	// Query operations
	GetTemplateByID(ctx context.Context, templateID int64) (*entity.TaskTemplateEntity, error)
	GetTemplatesByUserID(ctx context.Context, userID int64, filter *store.TaskTemplateFilter) ([]*entity.TaskTemplateEntity, error)
	GetFavoriteTemplates(ctx context.Context, userID int64) ([]*entity.TaskTemplateEntity, error)
}

type taskTemplateService struct {
	templatePort store.ITaskTemplatePort
}

func NewTaskTemplateService(templatePort store.ITaskTemplatePort) ITaskTemplateService {
	return &taskTemplateService{
		templatePort: templatePort,
	}
}

func (s *taskTemplateService) ValidateTemplateData(template *entity.TaskTemplateEntity) error {
	if template.TemplateName == "" {
		return errors.New(constant.TemplateNameRequired)
	}
	if len(template.TemplateName) > 200 {
		return errors.New(constant.TemplateNameTooLong)
	}
	if template.TitleTemplate == "" {
		return errors.New(constant.TemplateTitleRequired)
	}
	if !enum.TaskPriority(template.Priority).IsValid() {
		return errors.New(constant.InvalidTemplatePriority)
	}
	if template.EstimatedDurationMin <= 0 {
		return errors.New(constant.InvalidTemplateDuration)
	}
	return nil
}

func (s *taskTemplateService) ValidateTemplateOwnership(userID int64, template *entity.TaskTemplateEntity) error {
	if template.UserID != userID {
		return errors.New(constant.UpdateTemplateForbidden)
	}
	return nil
}

func (s *taskTemplateService) CreateTemplate(ctx context.Context, tx *gorm.DB, userID int64, template *entity.TaskTemplateEntity) (*entity.TaskTemplateEntity, error) {
	template.UserID = userID
	now := time.Now().UnixMilli()
	template.CreatedAt = now
	template.UpdatedAt = now
	if template.ActiveStatus == "" {
		template.ActiveStatus = string(enum.Active)
	}
	if template.UsageCount == 0 {
		template.UsageCount = 0
	}
	if err := s.ValidateTemplateData(template); err != nil {
		return nil, err
	}
	if err := s.templatePort.CreateTaskTemplate(ctx, tx, template); err != nil {
		return nil, err
	}
	return template, nil
}

func (s *taskTemplateService) UpdateTemplate(ctx context.Context, tx *gorm.DB, userID int64, template *entity.TaskTemplateEntity) error {
	if err := s.ValidateTemplateOwnership(userID, template); err != nil {
		return err
	}
	if err := s.ValidateTemplateData(template); err != nil {
		return err
	}
	now := time.Now().UnixMilli()
	template.UpdatedAt = now
	return s.templatePort.UpdateTaskTemplate(ctx, tx, template)
}

func (s *taskTemplateService) DeleteTemplate(ctx context.Context, tx *gorm.DB, userID int64, templateID int64) error {
	template, err := s.templatePort.GetTaskTemplateByID(ctx, templateID)
	if err != nil {
		return err
	}
	if err := s.ValidateTemplateOwnership(userID, template); err != nil {
		return err
	}
	return s.templatePort.SoftDeleteTaskTemplate(ctx, tx, templateID)
}

func (s *taskTemplateService) SetFavorite(ctx context.Context, tx *gorm.DB, userID int64, templateID int64, isFavorite bool) error {
	template, err := s.templatePort.GetTaskTemplateByID(ctx, templateID)
	if err != nil {
		return err
	}
	if err := s.ValidateTemplateOwnership(userID, template); err != nil {
		return err
	}

	template.IsFavorite = isFavorite
	template.UpdatedAt = time.Now().UnixMilli()

	return s.templatePort.ToggleFavorite(ctx, tx, templateID, isFavorite)
}

func (s *taskTemplateService) IncrementUsageCount(ctx context.Context, tx *gorm.DB, templateID int64) error {
	template, err := s.templatePort.GetTaskTemplateByID(ctx, templateID)
	if err != nil {
		return err
	}
	if template == nil {
		return errors.New(constant.TemplateNotFound)
	}

	now := time.Now().UnixMilli()
	template.IncrementUsage(now)
	return s.templatePort.UpdateTemplateUsage(ctx, tx, templateID, now)
}

func (s *taskTemplateService) GetTemplateByID(ctx context.Context, templateID int64) (*entity.TaskTemplateEntity, error) {
	return s.templatePort.GetTaskTemplateByID(ctx, templateID)
}

func (s *taskTemplateService) GetTemplatesByUserID(ctx context.Context, userID int64, filter *store.TaskTemplateFilter) ([]*entity.TaskTemplateEntity, error) {
	return s.templatePort.GetTaskTemplatesByUserID(ctx, userID, filter)
}

func (s *taskTemplateService) GetFavoriteTemplates(ctx context.Context, userID int64) ([]*entity.TaskTemplateEntity, error) {
	return s.templatePort.GetFavoriteTemplates(ctx, userID)
}
