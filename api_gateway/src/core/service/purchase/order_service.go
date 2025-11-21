/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/purchase"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/purchase"
)

type IOrderService interface {
	CreateOrder(ctx context.Context, req *request.CreateOrderRequest) (*response.BaseResponse, error)
	UpdateOrder(ctx context.Context, orderId string, req *request.UpdateOrderRequest) (*response.BaseResponse, error)
	DeleteOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	GetOrders(ctx context.Context, params *request.GetOrderParams) (*response.BaseResponse, error)

	AddProductToOrder(ctx context.Context, orderId string, req *request.AddOrderItemRequest) (*response.BaseResponse, error)
	DeleteProductFromOrder(ctx context.Context, orderId string, orderItemId string) (*response.BaseResponse, error)
	UpdateProductInOrder(ctx context.Context, orderId string, orderItemId string, req *request.UpdateOrderItemRequest) (*response.BaseResponse, error)

	ApproveOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	CancelOrder(ctx context.Context, orderId string, req *request.CancelOrderRequest) (*response.BaseResponse, error)
	MarkOrderAsReady(ctx context.Context, orderId string) (*response.BaseResponse, error)
}

type OrderService struct {
	orderClient port.IOrderClientPort
}

func (o *OrderService) CreateOrder(ctx context.Context, req *request.CreateOrderRequest) (*response.BaseResponse, error) {
	res, err := o.orderClient.CreateOrder(ctx, req)
	if err != nil {
		log.Error(ctx, "OrderService: CreateOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) UpdateOrder(ctx context.Context, orderId string, req *request.UpdateOrderRequest) (*response.BaseResponse, error) {
	res, err := o.orderClient.UpdateOrder(ctx, orderId, req)
	if err != nil {
		log.Error(ctx, "OrderService: UpdateOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) DeleteOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	res, err := o.orderClient.DeleteOrder(ctx, orderId)
	if err != nil {
		log.Error(ctx, "OrderService: DeleteOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	res, err := o.orderClient.GetOrder(ctx, orderId)
	if err != nil {
		log.Error(ctx, "OrderService: GetOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) GetOrders(ctx context.Context, params *request.GetOrderParams) (*response.BaseResponse, error) {
	res, err := o.orderClient.GetOrders(ctx, params)
	if err != nil {
		log.Error(ctx, "OrderService: GetOrders error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) AddProductToOrder(ctx context.Context, orderId string, req *request.AddOrderItemRequest) (*response.BaseResponse, error) {
	res, err := o.orderClient.AddProductToOrder(ctx, orderId, req)
	if err != nil {
		log.Error(ctx, "OrderService: AddProductToOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) DeleteProductFromOrder(ctx context.Context, orderId string, orderItemId string) (*response.BaseResponse, error) {
	res, err := o.orderClient.DeleteProductFromOrder(ctx, orderId, orderItemId)
	if err != nil {
		log.Error(ctx, "OrderService: DeleteProductFromOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) UpdateProductInOrder(ctx context.Context, orderId string, orderItemId string, req *request.UpdateOrderItemRequest) (*response.BaseResponse, error) {
	res, err := o.orderClient.UpdateProductInOrder(ctx, orderId, orderItemId, req)
	if err != nil {
		log.Error(ctx, "OrderService: UpdateProductInOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) ApproveOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	res, err := o.orderClient.ApproveOrder(ctx, orderId)
	if err != nil {
		log.Error(ctx, "OrderService: ApproveOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) CancelOrder(ctx context.Context, orderId string, req *request.CancelOrderRequest) (*response.BaseResponse, error) {
	res, err := o.orderClient.CancelOrder(ctx, orderId, req)
	if err != nil {
		log.Error(ctx, "OrderService: CancelOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) MarkOrderAsReady(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	res, err := o.orderClient.MarkOrderAsReady(ctx, orderId)
	if err != nil {
		log.Error(ctx, "OrderService: MarkOrderAsReady error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewOrderService(orderClient port.IOrderClientPort) IOrderService {
	return &OrderService{
		orderClient: orderClient,
	}
}
