/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package crm

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type ICustomerClientPort interface {
	CreateCustomer(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	UpdateCustomer(ctx context.Context, customerId int64, payload map[string]any) (*response.BaseResponse, error)
	GetCustomerByID(ctx context.Context, customerId int64) (*response.BaseResponse, error)
	GetCustomers(ctx context.Context, page, size int) (*response.BaseResponse, error)
	FilterCustomers(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	DeleteCustomer(ctx context.Context, customerId int64) (*response.BaseResponse, error)
}
