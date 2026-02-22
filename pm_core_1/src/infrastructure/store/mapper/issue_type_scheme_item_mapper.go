package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type IssueTypeSchemeItemMapper struct{}

func NewIssueTypeSchemeItemMapper() *IssueTypeSchemeItemMapper {
	return &IssueTypeSchemeItemMapper{}
}

func (m *IssueTypeSchemeItemMapper) ToEntity(mdl *model.IssueTypeSchemeItemModel) *entity.IssueTypeSchemeItemEntity {
	if mdl == nil {
		return nil
	}
	return &entity.IssueTypeSchemeItemEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			CreatedBy: mdl.CreatedBy,
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
			UpdatedBy: mdl.UpdatedBy,
		},
		TenantID:    mdl.TenantID,
		SchemeID:    mdl.SchemeID,
		IssueTypeID: mdl.IssueTypeID,
		Sequence:    mdl.Sequence,
	}
}

func (m *IssueTypeSchemeItemMapper) ToModel(e *entity.IssueTypeSchemeItemEntity) *model.IssueTypeSchemeItemModel {
	if e == nil {
		return nil
	}
	return &model.IssueTypeSchemeItemModel{
		BaseModel: model.BaseModel{
			ID:        e.ID,
			CreatedBy: e.CreatedBy,
			UpdatedBy: e.UpdatedBy,
		},
		TenantID:    e.TenantID,
		SchemeID:    e.SchemeID,
		IssueTypeID: e.IssueTypeID,
		Sequence:    e.Sequence,
	}
}

func (m *IssueTypeSchemeItemMapper) ToEntities(models []*model.IssueTypeSchemeItemModel) []*entity.IssueTypeSchemeItemEntity {
	if models == nil {
		return nil
	}
	entities := make([]*entity.IssueTypeSchemeItemEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}
	return entities
}

func (m *IssueTypeSchemeItemMapper) ToModels(entities []*entity.IssueTypeSchemeItemEntity) []*model.IssueTypeSchemeItemModel {
	if entities == nil {
		return nil
	}
	models := make([]*model.IssueTypeSchemeItemModel, 0, len(entities))
	for _, e := range entities {
		if mdl := m.ToModel(e); mdl != nil {
			models = append(models, mdl)
		}
	}
	return models
}
