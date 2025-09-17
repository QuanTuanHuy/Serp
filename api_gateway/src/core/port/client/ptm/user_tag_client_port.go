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

type IUserTagClientPort interface {
	CreateUserTag(ctx context.Context, req *request.CreateUserTagRequest) (*response.BaseResponse, error)
	GetUserTags(ctx context.Context) (*response.BaseResponse, error)
	GetUserTagByID(ctx context.Context, tagID int64) (*response.BaseResponse, error)
	UpdateUserTag(ctx context.Context, tagID int64, req *request.UpdateUserTagRequest) (*response.BaseResponse, error)
	DeleteUserTag(ctx context.Context, tagID int64) (*response.BaseResponse, error)
}
