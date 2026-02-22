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

type IssueTypeSchemeItemAdapter struct {
	db     *gorm.DB
	mapper *mapper.IssueTypeSchemeItemMapper
}

func NewIssueTypeSchemeItemAdapter(db *gorm.DB) store.IIssueTypeSchemeItemStore {
	return &IssueTypeSchemeItemAdapter{
		db:     db,
		mapper: mapper.NewIssueTypeSchemeItemMapper(),
	}
}

func (a *IssueTypeSchemeItemAdapter) CreateIssueTypeSchemeItem(ctx context.Context, tx *gorm.DB, item *entity.IssueTypeSchemeItemEntity) (*entity.IssueTypeSchemeItemEntity, error) {
	db := a.getDB(tx)
	itemModel := a.mapper.ToModel(item)
	if err := db.WithContext(ctx).Create(itemModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create issue type scheme item: %w", err)
	}
	return a.mapper.ToEntity(itemModel), nil
}

func (a *IssueTypeSchemeItemAdapter) CreateIssueTypeSchemeItems(ctx context.Context, tx *gorm.DB, items []*entity.IssueTypeSchemeItemEntity) ([]*entity.IssueTypeSchemeItemEntity, error) {
	if len(items) == 0 {
		return []*entity.IssueTypeSchemeItemEntity{}, nil
	}
	db := a.getDB(tx)
	itemModels := a.mapper.ToModels(items)
	if err := db.WithContext(ctx).CreateInBatches(itemModels, 100).Error; err != nil {
		return nil, fmt.Errorf("failed to create issue type scheme items: %w", err)
	}
	return a.mapper.ToEntities(itemModels), nil
}

func (a *IssueTypeSchemeItemAdapter) DeleteIssueTypeSchemeItemsBySchemeID(ctx context.Context, tx *gorm.DB, schemeID, tenantID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).
		Model(&model.IssueTypeSchemeItemModel{}).
		Where("scheme_id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		Update("deleted_at", gorm.Expr("CURRENT_TIMESTAMP")).Error; err != nil {
		return fmt.Errorf("failed to delete issue type scheme items by scheme id: %w", err)
	}
	return nil
}

func (a *IssueTypeSchemeItemAdapter) GetIssueTypeSchemeItemsBySchemeID(ctx context.Context, schemeID, tenantID int64) ([]*entity.IssueTypeSchemeItemEntity, error) {
	var itemModels []*model.IssueTypeSchemeItemModel
	if err := a.db.WithContext(ctx).
		Where("scheme_id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, tenantID).
		Order("sequence ASC").
		Find(&itemModels).Error; err != nil {
		return nil, fmt.Errorf("failed to get issue type scheme items by scheme id: %w", err)
	}
	return a.mapper.ToEntities(itemModels), nil
}

func (a *IssueTypeSchemeItemAdapter) ExistsIssueTypeInScheme(ctx context.Context, schemeID, issueTypeID, tenantID int64) (bool, error) {
	var count int64
	if err := a.db.WithContext(ctx).Model(&model.IssueTypeSchemeItemModel{}).
		Where("scheme_id = ? AND issue_type_id = ? AND tenant_id = ? AND deleted_at IS NULL", schemeID, issueTypeID, tenantID).
		Count(&count).Error; err != nil {
		return false, fmt.Errorf("failed to check issue type in scheme: %w", err)
	}
	return count > 0, nil
}

func (a *IssueTypeSchemeItemAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
