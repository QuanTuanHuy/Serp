/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	port "github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type GroupTaskStoreAdapter struct {
	db *gorm.DB
}

func (g *GroupTaskStoreAdapter) GetGroupTaskByID(ctx context.Context, groupTaskID int64) (*entity.GroupTaskEntity, error) {
	var groupTask model.GroupTaskModel
	if err := g.db.WithContext(ctx).
		Where("id = ? AND active_status = ?", groupTaskID, enum.Active).
		First(&groupTask).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToGroupTaskEntity(&groupTask), nil
}

func (g *GroupTaskStoreAdapter) GetGroupTasksByProjectID(ctx context.Context, projectID int64) ([]*entity.GroupTaskEntity, error) {
	var groupTasks []*model.GroupTaskModel
	if err := g.db.WithContext(ctx).
		Where("project_id = ? AND active_status = ?", projectID, enum.Active).
		Find(&groupTasks).Error; err != nil {
		return nil, err
	}
	return mapper.ToGroupTaskEntityList(groupTasks), nil
}

func (g *GroupTaskStoreAdapter) CreateGroupTask(ctx context.Context, tx *gorm.DB, groupTask *entity.GroupTaskEntity) (*entity.GroupTaskEntity, error) {
	groupTaskModel := mapper.ToGroupTaskModel(groupTask)
	if err := tx.WithContext(ctx).Create(groupTaskModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToGroupTaskEntity(groupTaskModel), nil
}

func (g *GroupTaskStoreAdapter) UpdateGroupTask(ctx context.Context, tx *gorm.DB, groupTaskID int64, groupTask *entity.GroupTaskEntity) (*entity.GroupTaskEntity, error) {
	groupTaskModel := mapper.ToGroupTaskModel(groupTask)
	if err := tx.WithContext(ctx).
		Model(&model.GroupTaskModel{}).
		Where("id = ?", groupTaskID).
		Updates(groupTaskModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToGroupTaskEntity(groupTaskModel), nil
}

func (g *GroupTaskStoreAdapter) GetDefaultGroupTaskByProjectID(ctx context.Context, projectID int64) (*entity.GroupTaskEntity, error) {
	var groupTask model.GroupTaskModel
	if err := g.db.WithContext(ctx).
		Where("project_id = ? AND active_status = ? AND is_default = ?", projectID, enum.Active, true).
		First(&groupTask).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToGroupTaskEntity(&groupTask), nil
}

func NewGroupTaskStoreAdapter(db *gorm.DB) port.IGroupTaskPort {
	return &GroupTaskStoreAdapter{
		db: db,
	}
}
