/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	request "github.com/serp/api-gateway/src/core/domain/dto/request/purchase"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type ICategoryClientPort interface {
	CreateCategory(ctx context.Context, req *request.CreateCategoryRequest) (*response.BaseResponse, error)
	UpdateCategory(ctx context.Context, categoryId string, req *request.UpdateCategoryRequest) (*response.BaseResponse, error)
	DeleteCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error)
	GetCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error)
	GetCategories(ctx context.Context, params *request.GetCategoryParams) (*response.BaseResponse, error)
}
