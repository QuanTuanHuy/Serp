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

type IIssueTypeSchemeItemStore interface {
	CreateIssueTypeSchemeItem(ctx context.Context, tx *gorm.DB, item *entity.IssueTypeSchemeItemEntity) (*entity.IssueTypeSchemeItemEntity, error)
	CreateIssueTypeSchemeItems(ctx context.Context, tx *gorm.DB, items []*entity.IssueTypeSchemeItemEntity) ([]*entity.IssueTypeSchemeItemEntity, error)
	DeleteIssueTypeSchemeItemsBySchemeID(ctx context.Context, tx *gorm.DB, schemeID, tenantID int64) error

	GetIssueTypeSchemeItemsBySchemeID(ctx context.Context, schemeID, tenantID int64) ([]*entity.IssueTypeSchemeItemEntity, error)
	ExistsIssueTypeInScheme(ctx context.Context, schemeID, issueTypeID, tenantID int64) (bool, error)
}
