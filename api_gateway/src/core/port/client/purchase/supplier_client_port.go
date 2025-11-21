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

type ISupplierClientPort interface {
	CreateSupplier(ctx context.Context, req *request.CreateSupplierRequest) (*response.BaseResponse, error)
	UpdateSupplier(ctx context.Context, supplierId string, req *request.UpdateSupplierRequest) (*response.BaseResponse, error)
	DeleteSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error)
	GetSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error)
	GetSuppliers(ctx context.Context, params *request.GetSupplierParams) (*response.BaseResponse, error)
}
