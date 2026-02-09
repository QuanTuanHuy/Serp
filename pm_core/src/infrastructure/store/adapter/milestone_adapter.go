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

type MilestoneAdapter struct {
	db     *gorm.DB
	mapper *mapper.MilestoneMapper
}

func NewMilestoneAdapter(db *gorm.DB) store.IMilestonePort {
	return &MilestoneAdapter{
		db:     db,
		mapper: mapper.NewMilestoneMapper(),
	}
}

func (a *MilestoneAdapter) CreateMilestone(ctx context.Context, tx *gorm.DB, milestone *entity.MilestoneEntity) (*entity.MilestoneEntity, error) {
	db := a.getDB(tx)
	milestoneModel := a.mapper.ToModel(milestone)
	if err := db.WithContext(ctx).Create(milestoneModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create milestone: %w", err)
	}
	return a.mapper.ToEntity(milestoneModel), nil
}

func (a *MilestoneAdapter) GetMilestoneByID(ctx context.Context, id int64) (*entity.MilestoneEntity, error) {
	var milestoneModel model.MilestoneModel
	if err := a.db.WithContext(ctx).First(&milestoneModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get milestone by id: %w", err)
	}
	return a.mapper.ToEntity(&milestoneModel), nil
}

func (a *MilestoneAdapter) GetMilestonesByProjectID(ctx context.Context, projectID int64) ([]*entity.MilestoneEntity, error) {
	var models []*model.MilestoneModel
	if err := a.db.WithContext(ctx).Where("project_id = ? AND active_status = ?", projectID, "ACTIVE").Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get milestones: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *MilestoneAdapter) UpdateMilestone(ctx context.Context, tx *gorm.DB, milestone *entity.MilestoneEntity) error {
	db := a.getDB(tx)
	milestoneModel := a.mapper.ToModel(milestone)
	if err := db.WithContext(ctx).Save(milestoneModel).Error; err != nil {
		return fmt.Errorf("failed to update milestone: %w", err)
	}
	return nil
}

func (a *MilestoneAdapter) SoftDeleteMilestone(ctx context.Context, tx *gorm.DB, milestoneID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Model(&model.MilestoneModel{}).
		Where("id = ?", milestoneID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete milestone: %w", err)
	}
	return nil
}

func (a *MilestoneAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
