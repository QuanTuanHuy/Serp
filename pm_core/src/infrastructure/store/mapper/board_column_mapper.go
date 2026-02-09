/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type BoardColumnMapper struct{}

func NewBoardColumnMapper() *BoardColumnMapper {
	return &BoardColumnMapper{}
}

func (m *BoardColumnMapper) ToEntity(mdl *model.BoardColumnModel) *entity.BoardColumnEntity {
	if mdl == nil {
		return nil
	}

	return &entity.BoardColumnEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		BoardID:       mdl.BoardID,
		Name:          mdl.Name,
		Position:      mdl.Position,
		StatusMapping: mdl.StatusMapping,
		ActiveStatus:  mdl.ActiveStatus,
	}
}

func (m *BoardColumnMapper) ToModel(e *entity.BoardColumnEntity) *model.BoardColumnModel {
	if e == nil {
		return nil
	}

	return &model.BoardColumnModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		BoardID:       e.BoardID,
		Name:          e.Name,
		Position:      e.Position,
		StatusMapping: e.StatusMapping,
		ActiveStatus:  e.ActiveStatus,
	}
}

func (m *BoardColumnMapper) ToEntities(models []*model.BoardColumnModel) []*entity.BoardColumnEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.BoardColumnEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
