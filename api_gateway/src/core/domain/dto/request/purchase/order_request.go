/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type OrderItem struct {
	ProductId      string  `json:"productId"`
	OrderItemSeqId int     `json:"orderItemSeqId"`
	Quantity       int     `json:"quantity"`
	Tax            float32 `json:"tax"`
	Discount       int64   `json:"discount"`
}

type CreateOrderRequest struct {
	FromSupplierId     string      `json:"fromSupplierId"`
	DeliveryBeforeDate string      `json:"deliveryBeforeDate"` // LocalDate as string "2025-11-19"
	DeliveryAfterDate  string      `json:"deliveryAfterDate"`
	Note               string      `json:"note"`
	OrderName          string      `json:"orderName"`
	Priority           int         `json:"priority"`
	SaleChannelId      string      `json:"saleChannelId"`
	OrderItems         []OrderItem `json:"orderItems"`
}

type UpdateOrderRequest struct {
	DeliveryBeforeDate string `json:"deliveryBeforeDate"`
	DeliveryAfterDate  string `json:"deliveryAfterDate"`
	Note               string `json:"note"`
	OrderName          string `json:"orderName"`
	Priority           int    `json:"priority"`
	SaleChannelId      string `json:"saleChannelId"`
}

type AddOrderItemRequest struct {
	ProductId      string  `json:"productId"`
	OrderItemSeqId int     `json:"orderItemSeqId"`
	Quantity       int     `json:"quantity"`
	Tax            float32 `json:"tax"`
	Discount       int64   `json:"discount"`
}

type UpdateOrderItemRequest struct {
	OrderItemSeqId int     `json:"orderItemSeqId"`
	Quantity       int     `json:"quantity"`
	Tax            float32 `json:"tax"`
	Discount       int64   `json:"discount"`
}

type CancelOrderRequest struct {
	Note string `json:"note"`
}

type GetOrderParams struct {
	Page            *int    `form:"page"`
	Size            *int    `form:"size"`
	SortBy          *string `form:"sortBy"`
	SortDirection   *string `form:"sortDirection"`
	Query           *string `form:"query"`
	StatusId        *string `form:"statusId"`
	FromSupplierId  *string `form:"fromSupplierId"`
	SaleChannelId   *string `form:"saleChannelId"`
	OrderDateAfter  *string `form:"orderDateAfter"`
	OrderDateBefore *string `form:"orderDateBefore"`
	DeliveryBefore  *string `form:"deliveryBefore"`
	DeliveryAfter   *string `form:"deliveryAfter"`
}
