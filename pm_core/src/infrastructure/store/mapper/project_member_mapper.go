/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type ProjectMemberMapper struct{}

func NewProjectMemberMapper() *ProjectMemberMapper {
	return &ProjectMemberMapper{}
}

func (m *ProjectMemberMapper) ToEntity(mdl *model.ProjectMemberModel) *entity.ProjectMemberEntity {
	if mdl == nil {
		return nil
	}

	return &entity.ProjectMemberEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		ProjectID: mdl.ProjectID,
		UserID:    mdl.UserID,
		Role:      mdl.Role,
		JoinedAt:  mdl.JoinedAt.UnixMilli(),
	}
}

func (m *ProjectMemberMapper) ToModel(e *entity.ProjectMemberEntity) *model.ProjectMemberModel {
	if e == nil {
		return nil
	}

	base := NewBaseMapper()
	return &model.ProjectMemberModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		ProjectID: e.ProjectID,
		UserID:    e.UserID,
		Role:      e.Role,
		JoinedAt:  base.UnixMilliToTime(e.JoinedAt),
	}
}

func (m *ProjectMemberMapper) ToEntities(models []*model.ProjectMemberModel) []*entity.ProjectMemberEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.ProjectMemberEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
