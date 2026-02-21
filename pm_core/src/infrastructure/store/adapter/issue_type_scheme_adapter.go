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

type IssueTypeSchemeAdapter struct {
	db         *gorm.DB
	mapper     *mapper.IssueTypeSchemeMapper
	itemMapper *mapper.IssueTypeSchemeItemMapper
}

func NewIssueTypeSchemeAdapter(db *gorm.DB) store.IIssueTypeSchemeStore {
	return &IssueTypeSchemeAdapter{
		db:         db,
		mapper:     mapper.NewIssueTypeSchemeMapper(),
		itemMapper: &mapper.IssueTypeSchemeItemMapper{},
	}
}

func (a *IssueTypeSchemeAdapter) CreateIssueTypeScheme(ctx context.Context, tx *gorm.DB, scheme *entity.IssueTypeSchemeEntity) (*entity.IssueTypeSchemeEntity, error) {
	db := a.getDB(tx)
	schemeModel := a.mapper.ToModel(scheme)
	if err := db.WithContext(ctx).Create(schemeModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create issue type scheme: %w", err)
	}
	return a.mapper.ToEntity(schemeModel), nil
}

func (a *IssueTypeSchemeAdapter) UpdateIssueTypeScheme(ctx context.Context, tx *gorm.DB, scheme *entity.IssueTypeSchemeEntity) error {
	db := a.getDB(tx)
	schemeModel := a.mapper.ToModel(scheme)
	if err := db.WithContext(ctx).Save(schemeModel).Error; err != nil {
		return fmt.Errorf("failed to update issue type scheme: %w", err)
	}
	return nil
}

func (a *IssueTypeSchemeAdapter) SoftDeleteIssueTypeScheme(ctx context.Context, tx *gorm.DB, schemeID, tenantID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).
		Model(&model.IssueTypeSchemeModel{}).
		Where("id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		Update("deleted_at", gorm.Expr("CURRENT_TIMESTAMP")).Error; err != nil {
		return fmt.Errorf("failed to soft delete issue type scheme: %w", err)
	}
	return nil
}

func (a *IssueTypeSchemeAdapter) GetIssueTypeSchemeByID(ctx context.Context, schemeID, tenantID int64) (*entity.IssueTypeSchemeEntity, error) {
	var schemeModel model.IssueTypeSchemeModel
	if err := a.db.WithContext(ctx).
		Where("id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		First(&schemeModel).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get issue type scheme by id: %w", err)
	}
	return a.mapper.ToEntity(&schemeModel), nil
}

func (a *IssueTypeSchemeAdapter) GetIssueTypeSchemeWithItems(ctx context.Context, schemeID, tenantID int64) (*entity.IssueTypeSchemeEntity, error) {
	schemeEntity, err := a.GetIssueTypeSchemeByID(ctx, schemeID, tenantID)
	if err != nil || schemeEntity == nil {
		return schemeEntity, err
	}

	var itemModels []*model.IssueTypeSchemeItemModel
	if err := a.db.WithContext(ctx).
		Where("scheme_id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		Order("sequence ASC").
		Find(&itemModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get issue type scheme items: %w", err)
	}

	for _, itemModel := range itemModels {
		schemeEntity.Items = append(schemeEntity.Items, *a.itemMapper.ToEntity(itemModel))
	}
	return schemeEntity, nil
}

func (a *IssueTypeSchemeAdapter) ListIssueTypeSchemes(ctx context.Context, tenantID int64) ([]*entity.IssueTypeSchemeEntity, error) {
	var schemeModels []*model.IssueTypeSchemeModel
	if err := a.db.WithContext(ctx).
		Where("tenant_id = ? AND deleted_at IS NULL", tenantID).
		Order("created_at DESC").
		Find(&schemeModels).Error; err != nil {
		return nil, fmt.Errorf("failed to list issue type schemes: %w", err)
	}
	return a.mapper.ToEntities(schemeModels), nil
}

func (a *IssueTypeSchemeAdapter) ExistsByName(ctx context.Context, tenantID int64, name string) (bool, error) {
	var count int64
	if err := a.db.WithContext(ctx).Model(&model.IssueTypeSchemeModel{}).
		Where("tenant_id = ? AND name = ? AND deleted_at IS NULL", tenantID, name).
		Count(&count).Error; err != nil {
		return false, fmt.Errorf("failed to check issue type scheme name existence: %w", err)
	}
	return count > 0, nil
}

func (a *IssueTypeSchemeAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
