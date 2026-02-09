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

type WorkItemDependencyAdapter struct {
	db     *gorm.DB
	mapper *mapper.WorkItemDependencyMapper
}

func NewWorkItemDependencyAdapter(db *gorm.DB) store.IWorkItemDependencyPort {
	return &WorkItemDependencyAdapter{
		db:     db,
		mapper: mapper.NewWorkItemDependencyMapper(),
	}
}

func (a *WorkItemDependencyAdapter) CreateDependency(ctx context.Context, tx *gorm.DB, dependency *entity.WorkItemDependencyEntity) (*entity.WorkItemDependencyEntity, error) {
	db := a.getDB(tx)
	dependencyModel := a.mapper.ToModel(dependency)
	if err := db.WithContext(ctx).Create(dependencyModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create dependency: %w", err)
	}
	return a.mapper.ToEntity(dependencyModel), nil
}

func (a *WorkItemDependencyAdapter) GetDependenciesByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.WorkItemDependencyEntity, error) {
	var models []*model.WorkItemDependencyModel
	if err := a.db.WithContext(ctx).Where("work_item_id = ?", workItemID).Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get dependencies: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *WorkItemDependencyAdapter) DeleteDependency(ctx context.Context, tx *gorm.DB, dependencyID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Delete(&model.WorkItemDependencyModel{}, dependencyID).Error; err != nil {
		return fmt.Errorf("failed to delete dependency: %w", err)
	}
	return nil
}

func (a *WorkItemDependencyAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
