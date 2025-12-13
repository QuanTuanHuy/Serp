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

type ContactController struct {
	contactService crm.IContactService
}

func (c *ContactController) CreateContact(ctx *gin.Context) {
	customerId, ok := utils.ValidateAndParseID(ctx, "customerId")
	if !ok {
		return
	}

	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	payload["customerId"] = customerId

	res, err := c.contactService.CreateContact(ctx.Request.Context(), customerId, payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *ContactController) UpdateContact(ctx *gin.Context) {
	customerId, ok := utils.ValidateAndParseID(ctx, "customerId")
	if !ok {
		return
	}

	contactId, ok := utils.ValidateAndParseID(ctx, "contactId")
	if !ok {
		return
	}

	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	payload["customerId"] = customerId
	payload["id"] = contactId

	res, err := c.contactService.UpdateContact(ctx.Request.Context(), customerId, contactId, payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *ContactController) GetContactByID(ctx *gin.Context) {
	customerId, ok := utils.ValidateAndParseID(ctx, "customerId")
	if !ok {
		return
	}

	contactId, ok := utils.ValidateAndParseID(ctx, "contactId")
	if !ok {
		return
	}

	res, err := c.contactService.GetContactByID(ctx.Request.Context(), customerId, contactId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *ContactController) GetContactsByCustomer(ctx *gin.Context) {
	customerId, ok := utils.ValidateAndParseID(ctx, "customerId")
	if !ok {
		return
	}

	res, err := c.contactService.GetContactsByCustomer(ctx.Request.Context(), customerId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *ContactController) GetContacts(ctx *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	res, err := c.contactService.GetContacts(ctx.Request.Context(), page, size)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *ContactController) DeleteContact(ctx *gin.Context) {
	customerId, ok := utils.ValidateAndParseID(ctx, "customerId")
	if !ok {
		return
	}

	contactId, ok := utils.ValidateAndParseID(ctx, "contactId")
	if !ok {
		return
	}

	res, err := c.contactService.DeleteContact(ctx.Request.Context(), customerId, contactId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewContactController(contactService crm.IContactService) *ContactController {
	return &ContactController{contactService: contactService}
}
