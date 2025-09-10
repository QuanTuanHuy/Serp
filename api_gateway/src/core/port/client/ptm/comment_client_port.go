/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type ICommentClientPort interface {
	CreateComment(ctx context.Context, req *request.CreateCommentRequest) (*response.BaseResponse, error)
	GetCommentByID(ctx context.Context, commentID int64) (*response.BaseResponse, error)
	UpdateComment(ctx context.Context, commentID int64, req *request.UpdateCommentRequest) (*response.BaseResponse, error)
	DeleteComment(ctx context.Context, commentID int64) (*response.BaseResponse, error)
}
