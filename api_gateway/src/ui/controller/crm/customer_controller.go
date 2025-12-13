/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	"github.com/serp/api-gateway/src/core/service/crm"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type CustomerController struct {
	customerService crm.ICustomerService
}

func (c *CustomerController) CreateCustomer(ctx *gin.Context) {
	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.customerService.CreateCustomer(ctx.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CustomerController) UpdateCustomer(ctx *gin.Context) {
	customerId, ok := utils.ValidateAndParseID(ctx, "customerId")
	if !ok {
		return
	}

	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.customerService.UpdateCustomer(ctx.Request.Context(), customerId, payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CustomerController) GetCustomerByID(ctx *gin.Context) {
	customerId, ok := utils.ValidateAndParseID(ctx, "customerId")
	if !ok {
		return
	}

	res, err := c.customerService.GetCustomerByID(ctx.Request.Context(), customerId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CustomerController) GetCustomers(ctx *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	res, err := c.customerService.GetCustomers(ctx.Request.Context(), page, size)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CustomerController) FilterCustomers(ctx *gin.Context) {
	payload := map[string]any{}
	if ctx.Request.ContentLength != 0 {
		if err := ctx.ShouldBindJSON(&payload); err != nil {
			utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
			return
		}
	}

	res, err := c.customerService.FilterCustomers(ctx.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CustomerController) DeleteCustomer(ctx *gin.Context) {
	customerId, ok := utils.ValidateAndParseID(ctx, "customerId")
	if !ok {
		return
	}

	res, err := c.customerService.DeleteCustomer(ctx.Request.Context(), customerId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewCustomerController(customerService crm.ICustomerService) *CustomerController {
	return &CustomerController{customerService: customerService}
}
