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

type IPrioritySchemeItemStore interface {
	CreatePrioritySchemeItem(ctx context.Context, tx *gorm.DB, item *entity.PrioritySchemeItemEntity) (*entity.PrioritySchemeItemEntity, error)
	CreatePrioritySchemeItems(ctx context.Context, tx *gorm.DB, items []*entity.PrioritySchemeItemEntity) ([]*entity.PrioritySchemeItemEntity, error)
	DeletePrioritySchemeItemsBySchemeID(ctx context.Context, tx *gorm.DB, schemeID, tenantID int64) error

	GetPrioritySchemeItemsBySchemeID(ctx context.Context, schemeID, tenantID int64) ([]*entity.PrioritySchemeItemEntity, error)
	ExistsPriorityInScheme(ctx context.Context, schemeID, priorityID, tenantID int64) (bool, error)
}
