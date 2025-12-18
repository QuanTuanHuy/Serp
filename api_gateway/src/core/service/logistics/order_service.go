/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/logistics"
)

type IOrderService interface {
	GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	GetOrders(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter string) (*response.BaseResponse, error)
}

type OrderService struct {
	orderClient port.IOrderClientPort
}

func (o *OrderService) GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	res, err := o.orderClient.GetOrder(ctx, orderId)
	if err != nil {
		log.Error(ctx, "OrderService: GetOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) GetOrders(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter string) (*response.BaseResponse, error) {
	res, err := o.orderClient.GetOrders(ctx, page, size, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter)
	if err != nil {
		log.Error(ctx, "OrderService: GetOrders error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewOrderService(orderClient port.IOrderClientPort) IOrderService {
	return &OrderService{
		orderClient: orderClient,
	}
}
