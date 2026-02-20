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

type IIssueTypeStore interface {
	CreateIssueType(ctx context.Context, tx *gorm.DB, issueType *entity.IssueTypeEntity) (*entity.IssueTypeEntity, error)
	UpdateIssueType(ctx context.Context, tx *gorm.DB, issueType *entity.IssueTypeEntity) error
	SoftDeleteIssueType(ctx context.Context, tx *gorm.DB, issueTypeID, tenantID int64) error

	GetIssueTypeByID(ctx context.Context, issueTypeID, tenantID int64) (*entity.IssueTypeEntity, error)
	ListIssueTypes(ctx context.Context, tenantID int64) ([]*entity.IssueTypeEntity, error)
	ExistsTypeKey(ctx context.Context, tenantID int64, typeKey string) (bool, error)
}
