/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
)

func ToCommentEntity(createComment *request.CreateCommentDTO) *entity.CommentEntity {
	return &entity.CommentEntity{
		Content: createComment.Content,
		TaskID:  createComment.TaskID,
	}
}

func UpdateCommentMapper(updateComment *request.UpdateCommentDTO, comment *entity.CommentEntity) *entity.CommentEntity {
	comment.Content = updateComment.Content
	comment.ActiveStatus = enum.ActiveStatus(updateComment.ActiveStatus)
	return comment
}
