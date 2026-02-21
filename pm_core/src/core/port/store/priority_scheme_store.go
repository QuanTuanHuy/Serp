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

type IPrioritySchemeStore interface {
	CreatePriorityScheme(ctx context.Context, tx *gorm.DB, scheme *entity.PrioritySchemeEntity) (*entity.PrioritySchemeEntity, error)
	UpdatePriorityScheme(ctx context.Context, tx *gorm.DB, scheme *entity.PrioritySchemeEntity) error
	SoftDeletePriorityScheme(ctx context.Context, tx *gorm.DB, schemeID, tenantID int64) error

	GetPrioritySchemeByID(ctx context.Context, schemeID, tenantID int64) (*entity.PrioritySchemeEntity, error)
	GetPrioritySchemeWithItems(ctx context.Context, schemeID, tenantID int64) (*entity.PrioritySchemeEntity, error)
	ListPrioritySchemes(ctx context.Context, tenantID int64) ([]*entity.PrioritySchemeEntity, error)

	ExistsByName(ctx context.Context, tenantID int64, name string) (bool, error)
}
