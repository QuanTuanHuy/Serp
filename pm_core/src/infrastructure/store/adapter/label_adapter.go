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

type LabelAdapter struct {
	db     *gorm.DB
	mapper *mapper.LabelMapper
}

func NewLabelAdapter(db *gorm.DB) store.ILabelPort {
	return &LabelAdapter{
		db:     db,
		mapper: mapper.NewLabelMapper(),
	}
}

func (a *LabelAdapter) CreateLabel(ctx context.Context, tx *gorm.DB, label *entity.LabelEntity) (*entity.LabelEntity, error) {
	db := a.getDB(tx)
	labelModel := a.mapper.ToModel(label)
	if err := db.WithContext(ctx).Create(labelModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create label: %w", err)
	}
	return a.mapper.ToEntity(labelModel), nil
}

func (a *LabelAdapter) GetLabelByID(ctx context.Context, id int64) (*entity.LabelEntity, error) {
	var labelModel model.LabelModel
	if err := a.db.WithContext(ctx).First(&labelModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get label by id: %w", err)
	}
	return a.mapper.ToEntity(&labelModel), nil
}

func (a *LabelAdapter) GetLabelsByProjectID(ctx context.Context, projectID int64) ([]*entity.LabelEntity, error) {
	var models []*model.LabelModel
	if err := a.db.WithContext(ctx).Where("project_id = ? AND active_status = ?", projectID, "ACTIVE").Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get labels: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *LabelAdapter) UpdateLabel(ctx context.Context, tx *gorm.DB, label *entity.LabelEntity) error {
	db := a.getDB(tx)
	labelModel := a.mapper.ToModel(label)
	if err := db.WithContext(ctx).Save(labelModel).Error; err != nil {
		return fmt.Errorf("failed to update label: %w", err)
	}
	return nil
}

func (a *LabelAdapter) SoftDeleteLabel(ctx context.Context, tx *gorm.DB, labelID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Model(&model.LabelModel{}).
		Where("id = ?", labelID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete label: %w", err)
	}
	return nil
}

func (a *LabelAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
