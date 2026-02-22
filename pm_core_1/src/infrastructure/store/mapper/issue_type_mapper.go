/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type IssueTypeMapper struct{}

func NewIssueTypeMapper() *IssueTypeMapper {
	return &IssueTypeMapper{}
}

func (m *IssueTypeMapper) ToEntity(mdl *model.IssueTypeModel) *entity.IssueTypeEntity {
	if mdl == nil {
		return nil
	}

	return &entity.IssueTypeEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			CreatedBy: mdl.CreatedBy,
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
			UpdatedBy: mdl.UpdatedBy,
		},
		TenantID:       mdl.TenantID,
		TypeKey:        mdl.TypeKey,
		Name:           mdl.Name,
		Description:    mdl.Description,
		IconUrl:        mdl.IconUrl,
		HierarchyLevel: mdl.HierarchyLevel,
		IsSystem:       mdl.IsSystem,
	}
}

func (m *IssueTypeMapper) ToModel(e *entity.IssueTypeEntity) *model.IssueTypeModel {
	if e == nil {
		return nil
	}
	return &model.IssueTypeModel{
		BaseModel: model.BaseModel{
			ID:        e.ID,
			UpdatedBy: e.UpdatedBy,
		},
		TenantID:       e.TenantID,
		TypeKey:        e.TypeKey,
		Name:           e.Name,
		Description:    e.Description,
		IconUrl:        e.IconUrl,
		HierarchyLevel: e.HierarchyLevel,
		IsSystem:       e.IsSystem,
	}
}

func (m *IssueTypeMapper) ToEntities(models []*model.IssueTypeModel) []*entity.IssueTypeEntity {
	if models == nil {
		return nil
	}
	entities := make([]*entity.IssueTypeEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}
	return entities
}
