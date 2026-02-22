package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type PrioritySchemeMapper struct{}

func NewPrioritySchemeMapper() *PrioritySchemeMapper {
	return &PrioritySchemeMapper{}
}

func (m *PrioritySchemeMapper) ToEntity(mdl *model.PrioritySchemeModel) *entity.PrioritySchemeEntity {
	if mdl == nil {
		return nil
	}
	return &entity.PrioritySchemeEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			CreatedBy: mdl.CreatedBy,
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
			UpdatedBy: mdl.UpdatedBy,
		},
		TenantID:          mdl.TenantID,
		Name:              mdl.Name,
		Description:       mdl.Description,
		DefaultPriorityID: mdl.DefaultPriorityID,
	}
}

func (m *PrioritySchemeMapper) ToModel(e *entity.PrioritySchemeEntity) *model.PrioritySchemeModel {
	if e == nil {
		return nil
	}
	return &model.PrioritySchemeModel{
		BaseModel: model.BaseModel{
			ID:        e.ID,
			CreatedBy: e.CreatedBy,
			UpdatedBy: e.UpdatedBy,
		},
		TenantID:          e.TenantID,
		Name:              e.Name,
		Description:       e.Description,
		DefaultPriorityID: e.DefaultPriorityID,
	}
}

func (m *PrioritySchemeMapper) ToEntities(models []*model.PrioritySchemeModel) []*entity.PrioritySchemeEntity {
	if models == nil {
		return nil
	}
	entities := make([]*entity.PrioritySchemeEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}
	return entities
}
