package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type IssueTypeSchemeMapper struct{}

func NewIssueTypeSchemeMapper() *IssueTypeSchemeMapper {
	return &IssueTypeSchemeMapper{}
}

func (m *IssueTypeSchemeMapper) ToEntity(mdl *model.IssueTypeSchemeModel) *entity.IssueTypeSchemeEntity {
	if mdl == nil {
		return nil
	}
	return &entity.IssueTypeSchemeEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			CreatedBy: mdl.CreatedBy,
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
			UpdatedBy: mdl.UpdatedBy,
		},
		TenantID:           mdl.TenantID,
		Name:               mdl.Name,
		Description:        mdl.Description,
		DefaultIssueTypeID: mdl.DefaultIssueTypeID,
	}
}

func (m *IssueTypeSchemeMapper) ToModel(e *entity.IssueTypeSchemeEntity) *model.IssueTypeSchemeModel {
	if e == nil {
		return nil
	}
	return &model.IssueTypeSchemeModel{
		BaseModel: model.BaseModel{
			ID:        e.ID,
			CreatedBy: e.CreatedBy,
			UpdatedBy: e.UpdatedBy,
		},
		TenantID:           e.TenantID,
		Name:               e.Name,
		Description:        e.Description,
		DefaultIssueTypeID: e.DefaultIssueTypeID,
	}
}

func (m *IssueTypeSchemeMapper) ToEntities(models []*model.IssueTypeSchemeModel) []*entity.IssueTypeSchemeEntity {
	if models == nil {
		return nil
	}
	entities := make([]*entity.IssueTypeSchemeEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}
	return entities
}
