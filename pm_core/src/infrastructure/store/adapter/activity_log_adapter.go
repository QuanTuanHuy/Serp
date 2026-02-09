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

type ActivityLogAdapter struct {
	db     *gorm.DB
	mapper *mapper.ActivityLogMapper
}

func NewActivityLogAdapter(db *gorm.DB) store.IActivityLogPort {
	return &ActivityLogAdapter{
		db:     db,
		mapper: mapper.NewActivityLogMapper(),
	}
}

func (a *ActivityLogAdapter) CreateLog(ctx context.Context, tx *gorm.DB, log *entity.ActivityLogEntity) (*entity.ActivityLogEntity, error) {
	db := a.getDB(tx)
	logModel := a.mapper.ToModel(log)
	if err := db.WithContext(ctx).Create(logModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create activity log: %w", err)
	}
	return a.mapper.ToEntity(logModel), nil
}

func (a *ActivityLogAdapter) GetLogsByProjectID(ctx context.Context, projectID int64) ([]*entity.ActivityLogEntity, error) {
	var models []*model.ActivityLogModel
	if err := a.db.WithContext(ctx).Where("project_id = ?", projectID).
		Order("created_at DESC").Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get activity logs by project: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *ActivityLogAdapter) GetLogsByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.ActivityLogEntity, error) {
	var models []*model.ActivityLogModel
	if err := a.db.WithContext(ctx).Where("work_item_id = ?", workItemID).
		Order("created_at DESC").Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get activity logs by work item: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *ActivityLogAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
