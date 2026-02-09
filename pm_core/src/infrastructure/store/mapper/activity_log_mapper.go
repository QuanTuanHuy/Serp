/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type ActivityLogMapper struct{}

func NewActivityLogMapper() *ActivityLogMapper {
	return &ActivityLogMapper{}
}

func (m *ActivityLogMapper) ToEntity(mdl *model.ActivityLogModel) *entity.ActivityLogEntity {
	if mdl == nil {
		return nil
	}

	return &entity.ActivityLogEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		ProjectID:  mdl.ProjectID,
		WorkItemID: mdl.WorkItemID,
		UserID:     mdl.UserID,
		EntityType: mdl.EntityType,
		EntityID:   mdl.EntityID,
		Action:     mdl.Action,
		Field:      mdl.Field,
		OldValue:   mdl.OldValue,
		NewValue:   mdl.NewValue,
	}
}

func (m *ActivityLogMapper) ToModel(e *entity.ActivityLogEntity) *model.ActivityLogModel {
	if e == nil {
		return nil
	}

	return &model.ActivityLogModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		ProjectID:  e.ProjectID,
		WorkItemID: e.WorkItemID,
		UserID:     e.UserID,
		EntityType: e.EntityType,
		EntityID:   e.EntityID,
		Action:     e.Action,
		Field:      e.Field,
		OldValue:   e.OldValue,
		NewValue:   e.NewValue,
	}
}

func (m *ActivityLogMapper) ToEntities(models []*model.ActivityLogModel) []*entity.ActivityLogEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.ActivityLogEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
