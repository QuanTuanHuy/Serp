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

type PrioritySchemeItemAdapter struct {
	db     *gorm.DB
	mapper *mapper.PrioritySchemeItemMapper
}

func NewPrioritySchemeItemAdapter(db *gorm.DB) store.IPrioritySchemeItemStore {
	return &PrioritySchemeItemAdapter{
		db:     db,
		mapper: mapper.NewPrioritySchemeItemMapper(),
	}
}

func (a *PrioritySchemeItemAdapter) CreatePrioritySchemeItem(ctx context.Context, tx *gorm.DB, item *entity.PrioritySchemeItemEntity) (*entity.PrioritySchemeItemEntity, error) {
	db := a.getDB(tx)
	itemModel := a.mapper.ToModel(item)
	if err := db.WithContext(ctx).Create(itemModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create priority scheme item: %w", err)
	}
	return a.mapper.ToEntity(itemModel), nil
}

func (a *PrioritySchemeItemAdapter) CreatePrioritySchemeItems(ctx context.Context, tx *gorm.DB, items []*entity.PrioritySchemeItemEntity) ([]*entity.PrioritySchemeItemEntity, error) {
	if len(items) == 0 {
		return []*entity.PrioritySchemeItemEntity{}, nil
	}
	db := a.getDB(tx)
	itemModels := a.mapper.ToModels(items)
	if err := db.WithContext(ctx).CreateInBatches(itemModels, 100).Error; err != nil {
		return nil, fmt.Errorf("failed to create priority scheme items: %w", err)
	}
	return a.mapper.ToEntities(itemModels), nil
}

func (a *PrioritySchemeItemAdapter) DeletePrioritySchemeItemsBySchemeID(ctx context.Context, tx *gorm.DB, schemeID, tenantID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).
		Model(&model.PrioritySchemeItemModel{}).
		Where("scheme_id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		Update("deleted_at", gorm.Expr("CURRENT_TIMESTAMP")).Error; err != nil {
		return fmt.Errorf("failed to delete priority scheme items by scheme id: %w", err)
	}
	return nil
}

func (a *PrioritySchemeItemAdapter) GetPrioritySchemeItemsBySchemeID(ctx context.Context, schemeID, tenantID int64) ([]*entity.PrioritySchemeItemEntity, error) {
	var itemModels []*model.PrioritySchemeItemModel
	if err := a.db.WithContext(ctx).
		Where("scheme_id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		Order("sequence ASC").
		Find(&itemModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get priority scheme items by scheme id: %w", err)
	}
	return a.mapper.ToEntities(itemModels), nil
}

func (a *PrioritySchemeItemAdapter) ExistsPriorityInScheme(ctx context.Context, schemeID, priorityID, tenantID int64) (bool, error) {
	var count int64
	if err := a.db.WithContext(ctx).Model(&model.PrioritySchemeItemModel{}).
		Where("scheme_id = ? AND priority_id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, priorityID, tenantID).
		Count(&count).Error; err != nil {
		return false, fmt.Errorf("failed to check priority in scheme: %w", err)
	}
	return count > 0, nil
}

func (a *PrioritySchemeItemAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
