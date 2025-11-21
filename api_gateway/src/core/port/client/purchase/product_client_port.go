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

type IProductClientPort interface {
	CreateProduct(ctx context.Context, req *request.CreateProductRequest) (*response.BaseResponse, error)
	UpdateProduct(ctx context.Context, productId string, req *request.UpdateProductRequest) (*response.BaseResponse, error)
	DeleteProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProducts(ctx context.Context, params *request.GetProductParams) (*response.BaseResponse, error)
}
