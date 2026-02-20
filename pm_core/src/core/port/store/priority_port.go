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

type IPriorityStore interface {
	CreatePriority(ctx context.Context, tx *gorm.DB, priority *entity.PriorityEntity) (*entity.PriorityEntity, error)
	GetPriorityByID(ctx context.Context, ID, tenantID int64) (*entity.PriorityEntity, error)
	ListPriorities(ctx context.Context, tenantID int64) ([]*entity.PriorityEntity, error)
	UpdatePriority(ctx context.Context, tx *gorm.DB, priority *entity.PriorityEntity) error
	SoftDeletePriority(ctx context.Context, tx *gorm.DB, ID, tenantID int64) error
	ExistsByName(ctx context.Context, tenantID int64, name string) (bool, error)
}
