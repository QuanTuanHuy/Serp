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

type IOrderClientPort interface {
	CreateOrder(ctx context.Context, req *request.CreateOrderRequest) (*response.BaseResponse, error)
	UpdateOrder(ctx context.Context, orderId string, req *request.UpdateOrderRequest) (*response.BaseResponse, error)
	DeleteOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	GetOrders(ctx context.Context, params *request.GetOrderParams) (*response.BaseResponse, error)

	// Order item operations
	AddProductToOrder(ctx context.Context, orderId string, req *request.AddOrderItemRequest) (*response.BaseResponse, error)
	DeleteProductFromOrder(ctx context.Context, orderId string, orderItemId string) (*response.BaseResponse, error)
	UpdateProductInOrder(ctx context.Context, orderId string, orderItemId string, req *request.UpdateOrderItemRequest) (*response.BaseResponse, error)

	// Order state operations
	ApproveOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	CancelOrder(ctx context.Context, orderId string, req *request.CancelOrderRequest) (*response.BaseResponse, error)
	MarkOrderAsReady(ctx context.Context, orderId string) (*response.BaseResponse, error)
}
