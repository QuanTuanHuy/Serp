/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type WorkItemDependencyMapper struct{}

func NewWorkItemDependencyMapper() *WorkItemDependencyMapper {
	return &WorkItemDependencyMapper{}
}

func (m *WorkItemDependencyMapper) ToEntity(mdl *model.WorkItemDependencyModel) *entity.WorkItemDependencyEntity {
	if mdl == nil {
		return nil
	}

	return &entity.WorkItemDependencyEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		WorkItemID:      mdl.WorkItemID,
		DependsOnItemID: mdl.DependsOnItemID,
		DependencyType:  mdl.DependencyType,
	}
}

func (m *WorkItemDependencyMapper) ToModel(e *entity.WorkItemDependencyEntity) *model.WorkItemDependencyModel {
	if e == nil {
		return nil
	}

	return &model.WorkItemDependencyModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		WorkItemID:      e.WorkItemID,
		DependsOnItemID: e.DependsOnItemID,
		DependencyType:  e.DependencyType,
	}
}

func (m *WorkItemDependencyMapper) ToEntities(models []*model.WorkItemDependencyModel) []*entity.WorkItemDependencyEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.WorkItemDependencyEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
