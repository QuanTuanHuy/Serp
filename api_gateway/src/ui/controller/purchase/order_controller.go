/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/purchase"
	service "github.com/serp/api-gateway/src/core/service/purchase"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type OrderController struct {
	orderService service.IOrderService
}

func (o *OrderController) CreateOrder(ctx *gin.Context) {
	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.CreateOrder(ctx.Request.Context(), req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) UpdateOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.UpdateOrder(ctx.Request.Context(), orderId, req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) DeleteOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.DeleteOrder(ctx.Request.Context(), orderId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) GetOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.GetOrder(ctx.Request.Context(), orderId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) GetOrders(ctx *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")
	saleChannelId := utils.ParseStringQuery(ctx, "saleChannelId")
	fromSupplierId := utils.ParseStringQuery(ctx, "fromSupplierId")
	orderDateAfter := utils.ParseStringQuery(ctx, "orderDateAfter")
	orderDateBefore := utils.ParseStringQuery(ctx, "orderDateBefore")
	deliveryBefore := utils.ParseStringQuery(ctx, "deliveryBefore")
	deliveryAfter := utils.ParseStringQuery(ctx, "deliveryAfter")

	params := &request.GetOrderParams{
		Page:            &page,
		Size:            &size,
		SortBy:          sortBy,
		SortDirection:   sortDirection,
		Query:           query,
		StatusId:        statusId,
		SaleChannelId:   saleChannelId,
		FromSupplierId:  fromSupplierId,
		OrderDateAfter:  orderDateAfter,
		OrderDateBefore: orderDateBefore,
		DeliveryBefore:  deliveryBefore,
		DeliveryAfter:   deliveryAfter,
	}

	res, err := o.orderService.GetOrders(ctx.Request.Context(), params)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) AddProductToOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.AddProductToOrder(ctx.Request.Context(), orderId, req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) DeleteProductFromOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	orderItemId := ctx.Param("orderItemId")
	if orderId == "" || orderItemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.DeleteProductFromOrder(ctx.Request.Context(), orderId, orderItemId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) UpdateProductInOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	orderItemId := ctx.Param("orderItemId")
	if orderId == "" || orderItemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.UpdateProductInOrder(ctx.Request.Context(), orderId, orderItemId, req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) ApproveOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.ApproveOrder(ctx.Request.Context(), orderId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) CancelOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.CancelOrder(ctx.Request.Context(), orderId, req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) MarkOrderAsReady(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.MarkOrderAsReady(ctx.Request.Context(), orderId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewOrderController(orderService service.IOrderService) *OrderController {
	return &OrderController{
		orderService: orderService,
	}
}
