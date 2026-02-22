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

type PrioritySchemeAdapter struct {
	db         *gorm.DB
	mapper     *mapper.PrioritySchemeMapper
	itemMapper *mapper.PrioritySchemeItemMapper
}

func NewPrioritySchemeAdapter(db *gorm.DB) store.IPrioritySchemeStore {
	return &PrioritySchemeAdapter{
		db:         db,
		mapper:     mapper.NewPrioritySchemeMapper(),
		itemMapper: &mapper.PrioritySchemeItemMapper{},
	}
}

func (a *PrioritySchemeAdapter) CreatePriorityScheme(ctx context.Context, tx *gorm.DB, scheme *entity.PrioritySchemeEntity) (*entity.PrioritySchemeEntity, error) {
	db := a.getDB(tx)
	schemeModel := a.mapper.ToModel(scheme)
	if err := db.WithContext(ctx).Create(schemeModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create priority scheme: %w", err)
	}
	return a.mapper.ToEntity(schemeModel), nil
}

func (a *PrioritySchemeAdapter) UpdatePriorityScheme(ctx context.Context, tx *gorm.DB, scheme *entity.PrioritySchemeEntity) error {
	db := a.getDB(tx)
	schemeModel := a.mapper.ToModel(scheme)
	if err := db.WithContext(ctx).Save(schemeModel).Error; err != nil {
		return fmt.Errorf("failed to update priority scheme: %w", err)
	}
	return nil
}

func (a *PrioritySchemeAdapter) SoftDeletePriorityScheme(ctx context.Context, tx *gorm.DB, schemeID, tenantID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).
		Model(&model.PrioritySchemeModel{}).
		Where("id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		Update("deleted_at", gorm.Expr("CURRENT_TIMESTAMP")).Error; err != nil {
		return fmt.Errorf("failed to soft delete priority scheme: %w", err)
	}
	return nil
}

func (a *PrioritySchemeAdapter) GetPrioritySchemeByID(ctx context.Context, schemeID, tenantID int64) (*entity.PrioritySchemeEntity, error) {
	var schemeModel model.PrioritySchemeModel
	if err := a.db.WithContext(ctx).
		Where("id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		First(&schemeModel).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get priority scheme by id: %w", err)
	}
	return a.mapper.ToEntity(&schemeModel), nil
}

func (a *PrioritySchemeAdapter) GetPrioritySchemeWithItems(ctx context.Context, schemeID, tenantID int64) (*entity.PrioritySchemeEntity, error) {
	schemeEntity, err := a.GetPrioritySchemeByID(ctx, schemeID, tenantID)
	if err != nil || schemeEntity == nil {
		return schemeEntity, err
	}

	var itemModels []*model.PrioritySchemeItemModel
	if err := a.db.WithContext(ctx).
		Where("scheme_id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		Order("sequence ASC").
		Find(&itemModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get priority scheme items: %w", err)
	}

	for _, itemModel := range itemModels {
		schemeEntity.Items = append(schemeEntity.Items, *a.itemMapper.ToEntity(itemModel))
	}
	return schemeEntity, nil
}

func (a *PrioritySchemeAdapter) ListPrioritySchemes(ctx context.Context, tenantID int64) ([]*entity.PrioritySchemeEntity, error) {
	var schemeModels []*model.PrioritySchemeModel
	if err := a.db.WithContext(ctx).
		Where("tenant_id = ? AND deleted_at IS NULL", tenantID).
		Order("created_at DESC").
		Find(&schemeModels).Error; err != nil {
		return nil, fmt.Errorf("failed to list priority schemes: %w", err)
	}
	return a.mapper.ToEntities(schemeModels), nil
}

func (a *PrioritySchemeAdapter) ExistsByName(ctx context.Context, tenantID int64, name string) (bool, error) {
	var count int64
	if err := a.db.WithContext(ctx).Model(&model.PrioritySchemeModel{}).
		Where("tenant_id = ? AND name = ? AND deleted_at IS NULL", tenantID, name).
		Count(&count).Error; err != nil {
		return false, fmt.Errorf("failed to check priority scheme name existence: %w", err)
	}
	return count > 0, nil
}

func (a *PrioritySchemeAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
