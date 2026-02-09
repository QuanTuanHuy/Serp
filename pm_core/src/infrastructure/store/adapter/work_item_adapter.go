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

type WorkItemAdapter struct {
	db     *gorm.DB
	mapper *mapper.WorkItemMapper
}

func NewWorkItemAdapter(db *gorm.DB) store.IWorkItemPort {
	return &WorkItemAdapter{
		db:     db,
		mapper: mapper.NewWorkItemMapper(),
	}
}

func (a *WorkItemAdapter) CreateWorkItem(ctx context.Context, tx *gorm.DB, workItem *entity.WorkItemEntity) (*entity.WorkItemEntity, error) {
	db := a.getDB(tx)
	workItemModel := a.mapper.ToModel(workItem)
	if err := db.WithContext(ctx).Create(workItemModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create work item: %w", err)
	}
	return a.mapper.ToEntity(workItemModel), nil
}

func (a *WorkItemAdapter) GetWorkItemByID(ctx context.Context, id int64) (*entity.WorkItemEntity, error) {
	var workItemModel model.WorkItemModel
	if err := a.db.WithContext(ctx).First(&workItemModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get work item by id: %w", err)
	}
	return a.mapper.ToEntity(&workItemModel), nil
}

func (a *WorkItemAdapter) UpdateWorkItem(ctx context.Context, tx *gorm.DB, workItem *entity.WorkItemEntity) error {
	db := a.getDB(tx)
	workItemModel := a.mapper.ToModel(workItem)
	if err := db.WithContext(ctx).Save(workItemModel).Error; err != nil {
		return fmt.Errorf("failed to update work item: %w", err)
	}
	return nil
}

func (a *WorkItemAdapter) SoftDeleteWorkItem(ctx context.Context, tx *gorm.DB, workItemID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Model(&model.WorkItemModel{}).
		Where("id = ?", workItemID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete work item: %w", err)
	}
	return nil
}

func (a *WorkItemAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
