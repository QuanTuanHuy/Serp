/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
)

type ICommentService interface {
	CreateComment(ctx context.Context, req *request.CreateCommentRequest) (*response.BaseResponse, error)
	GetCommentByID(ctx context.Context, commentID int64) (*response.BaseResponse, error)
	UpdateComment(ctx context.Context, commentID int64, req *request.UpdateCommentRequest) (*response.BaseResponse, error)
	DeleteComment(ctx context.Context, commentID int64) (*response.BaseResponse, error)
}

type CommentService struct {
	commentClient port.ICommentClientPort
}

func (c *CommentService) CreateComment(ctx context.Context, req *request.CreateCommentRequest) (*response.BaseResponse, error) {
	res, err := c.commentClient.CreateComment(ctx, req)
	if err != nil {
		log.Error(ctx, "Error creating comment: %v", err)
		return nil, err
	}
	return res, nil
}

func (c *CommentService) GetCommentByID(ctx context.Context, commentID int64) (*response.BaseResponse, error) {
	res, err := c.commentClient.GetCommentByID(ctx, commentID)
	if err != nil {
		log.Error(ctx, "Error getting comment by ID: %v", err)
		return nil, err
	}
	return res, nil
}

func (c *CommentService) UpdateComment(ctx context.Context, commentID int64, req *request.UpdateCommentRequest) (*response.BaseResponse, error) {
	res, err := c.commentClient.UpdateComment(ctx, commentID, req)
	if err != nil {
		log.Error(ctx, "Error updating comment: %v", err)
		return nil, err
	}
	return res, nil
}

func (c *CommentService) DeleteComment(ctx context.Context, commentID int64) (*response.BaseResponse, error) {
	res, err := c.commentClient.DeleteComment(ctx, commentID)
	if err != nil {
		log.Error(ctx, "Error deleting comment: %v", err)
		return nil, err
	}
	return res, nil
}

func NewCommentService(commentClient port.ICommentClientPort) ICommentService {
	return &CommentService{
		commentClient: commentClient,
	}
}
