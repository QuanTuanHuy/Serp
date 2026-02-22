package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type PrioritySchemeItemMapper struct{}

func NewPrioritySchemeItemMapper() *PrioritySchemeItemMapper {
	return &PrioritySchemeItemMapper{}
}

func (m *PrioritySchemeItemMapper) ToEntity(mdl *model.PrioritySchemeItemModel) *entity.PrioritySchemeItemEntity {
	if mdl == nil {
		return nil
	}
	return &entity.PrioritySchemeItemEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			CreatedBy: mdl.CreatedBy,
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
			UpdatedBy: mdl.UpdatedBy,
		},
		TenantID:   mdl.TenantID,
		SchemeID:   mdl.SchemeID,
		PriorityID: mdl.PriorityID,
		Sequence:   mdl.Sequence,
	}
}

func (m *PrioritySchemeItemMapper) ToModel(e *entity.PrioritySchemeItemEntity) *model.PrioritySchemeItemModel {
	if e == nil {
		return nil
	}
	return &model.PrioritySchemeItemModel{
		BaseModel: model.BaseModel{
			ID:        e.ID,
			CreatedBy: e.CreatedBy,
			UpdatedBy: e.UpdatedBy,
		},
		TenantID:   e.TenantID,
		SchemeID:   e.SchemeID,
		PriorityID: e.PriorityID,
		Sequence:   e.Sequence,
	}
}

func (m *PrioritySchemeItemMapper) ToEntities(models []*model.PrioritySchemeItemModel) []*entity.PrioritySchemeItemEntity {
	if models == nil {
		return nil
	}
	entities := make([]*entity.PrioritySchemeItemEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}
	return entities
}

func (m *PrioritySchemeItemMapper) ToModels(entities []*entity.PrioritySchemeItemEntity) []*model.PrioritySchemeItemModel {
	if entities == nil {
		return nil
	}
	models := make([]*model.PrioritySchemeItemModel, 0, len(entities))
	for _, e := range entities {
		if mdl := m.ToModel(e); mdl != nil {
			models = append(models, mdl)
		}
	}
	return models
}
