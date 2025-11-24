/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	port "github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type ICommentService interface {
	CreateComment(ctx context.Context, tx *gorm.DB, request *request.CreateCommentDTO) (*entity.CommentEntity, error)
	UpdateComment(ctx context.Context, tx *gorm.DB, commentID int64, comment *entity.CommentEntity) (*entity.CommentEntity, error)
	DeleteComment(ctx context.Context, tx *gorm.DB, commentID int64) error
	GetCommentByID(ctx context.Context, commentID int64) (*entity.CommentEntity, error)
	GetCommentsByTaskID(ctx context.Context, taskID int64) ([]*entity.CommentEntity, error)
}

type CommentService struct {
	commentPort port.ICommentPort
}

func (c *CommentService) GetCommentsByTaskID(ctx context.Context, taskID int64) ([]*entity.CommentEntity, error) {
	comments, err := c.commentPort.GetCommentsByTaskID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get comments for task ID ", taskID, " error: ", err)
		return nil, err
	}
	return comments, nil
}

func (c *CommentService) CreateComment(ctx context.Context, tx *gorm.DB, request *request.CreateCommentDTO) (*entity.CommentEntity, error) {
	comment := mapper.ToCommentEntity(request)
	comment.ActiveStatus = enum.Active

	comment, err := c.commentPort.CreateComment(ctx, tx, comment)
	if err != nil {
		log.Error(ctx, "Failed to create comment: ", err)
		return nil, err
	}
	return comment, nil
}

func (c *CommentService) DeleteComment(ctx context.Context, tx *gorm.DB, commentID int64) error {
	err := c.commentPort.DeleteComment(ctx, tx, commentID)
	if err != nil {
		log.Error(ctx, "Failed to delete comment ID ", commentID, " error: ", err)
		return err
	}
	return nil

}

func (c *CommentService) GetCommentByID(ctx context.Context, commentID int64) (*entity.CommentEntity, error) {
	comment, err := c.commentPort.GetCommentByID(ctx, commentID)
	if err != nil {
		log.Error(ctx, "Failed to get comment by ID ", commentID, " error: ", err)
		return nil, err
	}
	if comment == nil {
		log.Error(ctx, "Comment not found for ID ", commentID)
		return nil, errors.New(constant.CommentNotFound)
	}
	return comment, nil
}

func (c *CommentService) UpdateComment(ctx context.Context, tx *gorm.DB, commentID int64, comment *entity.CommentEntity) (*entity.CommentEntity, error) {
	comment, err := c.commentPort.UpdateComment(ctx, tx, commentID, comment)
	if err != nil {
		log.Error(ctx, "Failed to update comment ID ", commentID, " error: ", err)
		return nil, err
	}
	return comment, nil
}

func NewCommentService(commentPort port.ICommentPort) ICommentService {
	return &CommentService{
		commentPort: commentPort,
	}
}
