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

type WorkItemAssignmentAdapter struct {
	db     *gorm.DB
	mapper *mapper.WorkItemAssignmentMapper
}

func NewWorkItemAssignmentAdapter(db *gorm.DB) store.IWorkItemAssignmentPort {
	return &WorkItemAssignmentAdapter{
		db:     db,
		mapper: mapper.NewWorkItemAssignmentMapper(),
	}
}

func (a *WorkItemAssignmentAdapter) CreateAssignment(ctx context.Context, tx *gorm.DB, assignment *entity.WorkItemAssignmentEntity) (*entity.WorkItemAssignmentEntity, error) {
	db := a.getDB(tx)
	assignmentModel := a.mapper.ToModel(assignment)
	if err := db.WithContext(ctx).Create(assignmentModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create assignment: %w", err)
	}
	return a.mapper.ToEntity(assignmentModel), nil
}

func (a *WorkItemAssignmentAdapter) GetAssignmentsByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.WorkItemAssignmentEntity, error) {
	var models []*model.WorkItemAssignmentModel
	if err := a.db.WithContext(ctx).Where("work_item_id = ?", workItemID).Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get assignments: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *WorkItemAssignmentAdapter) DeleteAssignment(ctx context.Context, tx *gorm.DB, assignmentID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Delete(&model.WorkItemAssignmentModel{}, assignmentID).Error; err != nil {
		return fmt.Errorf("failed to delete assignment: %w", err)
	}
	return nil
}

func (a *WorkItemAssignmentAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
