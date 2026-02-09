/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/pm-core/src/core/domain/entity"
	"gorm.io/gorm"
)

type IWorkItemAssignmentPort interface {
	CreateAssignment(ctx context.Context, tx *gorm.DB, assignment *entity.WorkItemAssignmentEntity) (*entity.WorkItemAssignmentEntity, error)
	GetAssignmentsByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.WorkItemAssignmentEntity, error)
	DeleteAssignment(ctx context.Context, tx *gorm.DB, assignmentID int64) error
}
