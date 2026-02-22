/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

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

type PriorityAdapter struct {
	db     *gorm.DB
	mapper *mapper.PriorityMapper
}

func NewPriorityAdapter(db *gorm.DB) store.IPriorityStore {
	return &PriorityAdapter{
		db:     db,
		mapper: mapper.NewPriorityMapper(),
	}
}

func (p *PriorityAdapter) CreatePriority(ctx context.Context, tx *gorm.DB, priority *entity.PriorityEntity) (*entity.PriorityEntity, error) {
	db := p.getDB(tx)
	priorityModel := p.mapper.ToModel(priority)
	if err := db.WithContext(ctx).Create(priorityModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create priority: %w", err)
	}
	return p.mapper.ToEntity(priorityModel), nil
}

func (p *PriorityAdapter) ExistsByName(ctx context.Context, tenantID int64, name string) (bool, error) {
	var count int64
	if err := p.db.WithContext(ctx).Model(&model.PriorityModel{}).
		Where("tenant_id = ? AND name = ? AND deleted_at IS NULL", tenantID, name).
		Count(&count).Error; err != nil {
		return false, fmt.Errorf("failed to check priority existence: %w", err)
	}
	return count > 0, nil
}

func (p *PriorityAdapter) GetPriorityByID(ctx context.Context, ID int64, tenantID int64) (*entity.PriorityEntity, error) {
	var priorityModel model.PriorityModel
	if err := p.db.WithContext(ctx).Model(&model.PriorityModel{}).
		Where("id = ? AND tenant_id = ? AND deleted_at IS NULL", ID, tenantID).
		First(&priorityModel).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get priority by id: %w", err)
	}
	return p.mapper.ToEntity(&priorityModel), nil
}

func (p *PriorityAdapter) ListPriorities(ctx context.Context, tenantID int64) ([]*entity.PriorityEntity, error) {
	var priorityModels []*model.PriorityModel
	if err := p.db.WithContext(ctx).Model(&model.PriorityModel{}).
		Where("tenant_id = ? AND deleted_at IS NULL", tenantID).
		Find(&priorityModels).Error; err != nil {
		return nil, fmt.Errorf("failed to list priorities: %w", err)
	}
	return p.mapper.ToEntities(priorityModels), nil
}

func (p *PriorityAdapter) SoftDeletePriority(ctx context.Context, tx *gorm.DB, ID int64, tenantID int64) error {
	db := p.getDB(tx)
	if err := db.WithContext(ctx).
		Model(&model.PriorityModel{}).
		Where("id = ? AND tenant_id = ? AND deleted_at IS NULL", ID, tenantID).
		Update("deleted_at", gorm.Expr("CURRENT_TIMESTAMP")).Error; err != nil {
		return fmt.Errorf("failed to soft delete priority: %w", err)
	}
	return nil
}

func (p *PriorityAdapter) UpdatePriority(ctx context.Context, tx *gorm.DB, priority *entity.PriorityEntity) error {
	db := p.getDB(tx)
	priorityModel := p.mapper.ToModel(priority)
	if err := db.WithContext(ctx).Save(priorityModel).Error; err != nil {
		return fmt.Errorf("failed to update priority: %w", err)
	}
	return nil
}

func (p *PriorityAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return p.db
}
