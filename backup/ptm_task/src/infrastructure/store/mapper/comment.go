/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
)

func ToCommentModel(comment *entity.CommentEntity) *model.CommentModel {
	if comment == nil {
		return nil
	}

	return &model.CommentModel{
		BaseModel: model.BaseModel{
			ID: comment.ID,
		},
		Content:      comment.Content,
		TaskID:       comment.TaskID,
		ActiveStatus: string(comment.ActiveStatus),
	}
}

func ToCommentEntity(commentModel *model.CommentModel) *entity.CommentEntity {
	if commentModel == nil {
		return nil
	}

	return &entity.CommentEntity{
		BaseEntity: entity.BaseEntity{
			ID:        commentModel.ID,
			CreatedAt: commentModel.CreatedAt.UnixMilli(),
			UpdatedAt: commentModel.UpdatedAt.UnixMilli(),
		},
		Content:      commentModel.Content,
		TaskID:       commentModel.TaskID,
		ActiveStatus: enum.ActiveStatus(commentModel.ActiveStatus),
	}
}

func ToCommentEntities(commentModels []*model.CommentModel) []*entity.CommentEntity {
	if commentModels == nil {
		return nil
	}

	comments := make([]*entity.CommentEntity, len(commentModels))
	for i, commentModel := range commentModels {
		comments[i] = ToCommentEntity(commentModel)
	}
	return comments
}
