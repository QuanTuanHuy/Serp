/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type CommentMapper struct{}

func NewCommentMapper() *CommentMapper {
	return &CommentMapper{}
}

func (m *CommentMapper) ToEntity(mdl *model.CommentModel) *entity.CommentEntity {
	if mdl == nil {
		return nil
	}

	return &entity.CommentEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		WorkItemID:      mdl.WorkItemID,
		AuthorID:        mdl.AuthorID,
		Content:         mdl.Content,
		ParentCommentID: mdl.ParentCommentID,
		IsEdited:        mdl.IsEdited,
		EditedAtMs:      mdl.EditedAtMs,
		ActiveStatus:    mdl.ActiveStatus,
	}
}

func (m *CommentMapper) ToModel(e *entity.CommentEntity) *model.CommentModel {
	if e == nil {
		return nil
	}

	return &model.CommentModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		WorkItemID:      e.WorkItemID,
		AuthorID:        e.AuthorID,
		Content:         e.Content,
		ParentCommentID: e.ParentCommentID,
		IsEdited:        e.IsEdited,
		EditedAtMs:      e.EditedAtMs,
		ActiveStatus:    e.ActiveStatus,
	}
}

func (m *CommentMapper) ToEntities(models []*model.CommentModel) []*entity.CommentEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.CommentEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
