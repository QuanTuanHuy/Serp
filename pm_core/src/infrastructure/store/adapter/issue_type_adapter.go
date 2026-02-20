/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/core/port/store"
	"github.com/serp/pm-core/src/infrastructure/store/mapper"
	"github.com/serp/pm-core/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type IssueTypeAdapter struct {
	db     *gorm.DB
	mapper *mapper.IssueTypeMapper
}

func NewIssueTypeAdapter(db *gorm.DB) store.IIssueTypeStore {
	return &IssueTypeAdapter{
		db:     db,
		mapper: mapper.NewIssueTypeMapper(),
	}
}

func (i *IssueTypeAdapter) CreateIssueType(ctx context.Context, tx *gorm.DB, issueType *entity.IssueTypeEntity) (*entity.IssueTypeEntity, error) {
	db := i.getDB(tx)
	issueTypeModel := i.mapper.ToModel(issueType)
	if err := db.WithContext(ctx).Create(issueTypeModel).Error; err != nil {
		return nil, err
	}
	return i.mapper.ToEntity(issueTypeModel), nil
}

func (i *IssueTypeAdapter) ExistsTypeKey(ctx context.Context, tenantID int64, typeKey string) (bool, error) {
	var count int64
	if err := i.db.WithContext(ctx).Model(&model.IssueTypeModel{}).
		Where("tenant_id = ? AND type_key = ? AND deleted_at IS NULL", tenantID, typeKey).
		Count(&count).Error; err != nil {
		return false, err
	}
	return count > 0, nil
}

func (i *IssueTypeAdapter) GetIssueTypeByID(ctx context.Context, issueTypeID int64, tenantID int64) (*entity.IssueTypeEntity, error) {
	var issueTypeModel model.IssueTypeModel
	if err := i.db.WithContext(ctx).
		Where("id = ? AND tenant_id = ? AND deleted_at IS NULL", issueTypeID, tenantID).
		First(&issueTypeModel).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return i.mapper.ToEntity(&issueTypeModel), nil
}

func (i *IssueTypeAdapter) ListIssueTypes(ctx context.Context, tenantID int64) ([]*entity.IssueTypeEntity, error) {
	var issueTypeModels []*model.IssueTypeModel
	if err := i.db.WithContext(ctx).
		Where("tenant_id = ? AND deleted_at IS NULL", tenantID).
		Order("hierarchy_level ASC").
		Find(&issueTypeModels).Error; err != nil {
		return nil, err
	}
	return i.mapper.ToEntities(issueTypeModels), nil
}

func (i *IssueTypeAdapter) SoftDeleteIssueType(ctx context.Context, tx *gorm.DB, issueTypeID int64, tenantID int64) error {
	db := i.getDB(tx)
	if err := db.WithContext(ctx).
		Model(&model.IssueTypeModel{}).
		Where("id = ? AND tenant_id = ? AND deleted_at IS NULL", issueTypeID, tenantID).
		Update("deleted_at", gorm.Expr("CURRENT_TIMESTAMP")).Error; err != nil {
		return err
	}
	return nil
}

func (i *IssueTypeAdapter) UpdateIssueType(ctx context.Context, tx *gorm.DB, issueType *entity.IssueTypeEntity) error {
	db := i.getDB(tx)
	issueTypeModel := i.mapper.ToModel(issueType)
	if err := db.WithContext(ctx).Save(issueTypeModel).Error; err != nil {
		return err
	}
	return nil
}

func (a *IssueTypeAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
