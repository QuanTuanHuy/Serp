/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type BoardMapper struct{}

func NewBoardMapper() *BoardMapper {
	return &BoardMapper{}
}

func (m *BoardMapper) ToEntity(mdl *model.BoardModel) *entity.BoardEntity {
	if mdl == nil {
		return nil
	}

	return &entity.BoardEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		ProjectID:    mdl.ProjectID,
		Name:         mdl.Name,
		ActiveStatus: mdl.ActiveStatus,
	}
}

func (m *BoardMapper) ToModel(e *entity.BoardEntity) *model.BoardModel {
	if e == nil {
		return nil
	}

	return &model.BoardModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		ProjectID:    e.ProjectID,
		Name:         e.Name,
		ActiveStatus: e.ActiveStatus,
	}
}

func (m *BoardMapper) ToEntities(models []*model.BoardModel) []*entity.BoardEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.BoardEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
