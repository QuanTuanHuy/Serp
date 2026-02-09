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

type SprintAdapter struct {
	db     *gorm.DB
	mapper *mapper.SprintMapper
}

func NewSprintAdapter(db *gorm.DB) store.ISprintPort {
	return &SprintAdapter{
		db:     db,
		mapper: mapper.NewSprintMapper(),
	}
}

func (a *SprintAdapter) CreateSprint(ctx context.Context, tx *gorm.DB, sprint *entity.SprintEntity) (*entity.SprintEntity, error) {
	db := a.getDB(tx)
	sprintModel := a.mapper.ToModel(sprint)
	if err := db.WithContext(ctx).Create(sprintModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create sprint: %w", err)
	}
	return a.mapper.ToEntity(sprintModel), nil
}

func (a *SprintAdapter) GetSprintByID(ctx context.Context, id int64) (*entity.SprintEntity, error) {
	var sprintModel model.SprintModel
	if err := a.db.WithContext(ctx).First(&sprintModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get sprint by id: %w", err)
	}
	return a.mapper.ToEntity(&sprintModel), nil
}

func (a *SprintAdapter) GetSprintsByProjectID(ctx context.Context, projectID int64) ([]*entity.SprintEntity, error) {
	var models []*model.SprintModel
	if err := a.db.WithContext(ctx).Where("project_id = ? AND active_status = ?", projectID, "ACTIVE").Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get sprints: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *SprintAdapter) UpdateSprint(ctx context.Context, tx *gorm.DB, sprint *entity.SprintEntity) error {
	db := a.getDB(tx)
	sprintModel := a.mapper.ToModel(sprint)
	if err := db.WithContext(ctx).Save(sprintModel).Error; err != nil {
		return fmt.Errorf("failed to update sprint: %w", err)
	}
	return nil
}

func (a *SprintAdapter) SoftDeleteSprint(ctx context.Context, tx *gorm.DB, sprintID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Model(&model.SprintModel{}).
		Where("id = ?", sprintID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete sprint: %w", err)
	}
	return nil
}

func (a *SprintAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
