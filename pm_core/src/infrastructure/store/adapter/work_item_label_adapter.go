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

type WorkItemLabelAdapter struct {
	db     *gorm.DB
	mapper *mapper.WorkItemLabelMapper
}

func NewWorkItemLabelAdapter(db *gorm.DB) store.IWorkItemLabelPort {
	return &WorkItemLabelAdapter{
		db:     db,
		mapper: mapper.NewWorkItemLabelMapper(),
	}
}

func (a *WorkItemLabelAdapter) CreateWorkItemLabel(ctx context.Context, tx *gorm.DB, wil *entity.WorkItemLabelEntity) (*entity.WorkItemLabelEntity, error) {
	db := a.getDB(tx)
	wilModel := a.mapper.ToModel(wil)
	if err := db.WithContext(ctx).Create(wilModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create work item label: %w", err)
	}
	return a.mapper.ToEntity(wilModel), nil
}

func (a *WorkItemLabelAdapter) GetLabelsByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.WorkItemLabelEntity, error) {
	var models []*model.WorkItemLabelModel
	if err := a.db.WithContext(ctx).Where("work_item_id = ?", workItemID).Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get work item labels: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *WorkItemLabelAdapter) DeleteWorkItemLabel(ctx context.Context, tx *gorm.DB, workItemLabelID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Delete(&model.WorkItemLabelModel{}, workItemLabelID).Error; err != nil {
		return fmt.Errorf("failed to delete work item label: %w", err)
	}
	return nil
}

func (a *WorkItemLabelAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
