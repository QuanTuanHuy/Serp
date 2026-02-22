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

type IIssueTypeSchemeStore interface {
	CreateIssueTypeScheme(ctx context.Context, tx *gorm.DB, scheme *entity.IssueTypeSchemeEntity) (*entity.IssueTypeSchemeEntity, error)
	UpdateIssueTypeScheme(ctx context.Context, tx *gorm.DB, scheme *entity.IssueTypeSchemeEntity) error
	SoftDeleteIssueTypeScheme(ctx context.Context, tx *gorm.DB, schemeID, tenantID int64) error

	GetIssueTypeSchemeByID(ctx context.Context, schemeID, tenantID int64) (*entity.IssueTypeSchemeEntity, error)
	GetIssueTypeSchemeWithItems(ctx context.Context, schemeID, tenantID int64) (*entity.IssueTypeSchemeEntity, error)
	ListIssueTypeSchemes(ctx context.Context, tenantID int64) ([]*entity.IssueTypeSchemeEntity, error)

	ExistsByName(ctx context.Context, tenantID int64, name string) (bool, error)
}
