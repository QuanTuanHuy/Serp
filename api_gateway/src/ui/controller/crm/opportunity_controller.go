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

type OpportunityController struct {
	opportunityService crm.IOpportunityService
}

func (o *OpportunityController) CreateOpportunity(ctx *gin.Context) {
	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.opportunityService.CreateOpportunity(ctx.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OpportunityController) UpdateOpportunity(ctx *gin.Context) {
	opportunityId, ok := utils.ValidateAndParseID(ctx, "opportunityId")
	if !ok {
		return
	}

	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.opportunityService.UpdateOpportunity(ctx.Request.Context(), opportunityId, payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OpportunityController) GetOpportunityByID(ctx *gin.Context) {
	opportunityId, ok := utils.ValidateAndParseID(ctx, "opportunityId")
	if !ok {
		return
	}

	res, err := o.opportunityService.GetOpportunityByID(ctx.Request.Context(), opportunityId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OpportunityController) GetOpportunities(ctx *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	res, err := o.opportunityService.GetOpportunities(ctx.Request.Context(), page, size)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OpportunityController) FilterOpportunities(ctx *gin.Context) {
	payload := map[string]any{}
	if ctx.Request.ContentLength != 0 {
		if err := ctx.ShouldBindJSON(&payload); err != nil {
			utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
			return
		}
	}

	res, err := o.opportunityService.FilterOpportunities(ctx.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OpportunityController) ChangeStage(ctx *gin.Context) {
	opportunityId, ok := utils.ValidateAndParseID(ctx, "opportunityId")
	if !ok {
		return
	}

	newStage := ctx.Query("newStage")
	if newStage == "" {
		utils.AbortErrorHandleCustomMessage(ctx, constant.GeneralBadRequest, "newStage query parameter is required")
		return
	}

	res, err := o.opportunityService.ChangeStage(ctx.Request.Context(), opportunityId, newStage)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OpportunityController) CloseAsWon(ctx *gin.Context) {
	opportunityId, ok := utils.ValidateAndParseID(ctx, "opportunityId")
	if !ok {
		return
	}

	res, err := o.opportunityService.CloseAsWon(ctx.Request.Context(), opportunityId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OpportunityController) CloseAsLost(ctx *gin.Context) {
	opportunityId, ok := utils.ValidateAndParseID(ctx, "opportunityId")
	if !ok {
		return
	}

	lostReason := ctx.Query("lostReason")
	if lostReason == "" {
		utils.AbortErrorHandleCustomMessage(ctx, constant.GeneralBadRequest, "lostReason query parameter is required")
		return
	}

	res, err := o.opportunityService.CloseAsLost(ctx.Request.Context(), opportunityId, lostReason)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OpportunityController) DeleteOpportunity(ctx *gin.Context) {
	opportunityId, ok := utils.ValidateAndParseID(ctx, "opportunityId")
	if !ok {
		return
	}

	res, err := o.opportunityService.DeleteOpportunity(ctx.Request.Context(), opportunityId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewOpportunityController(opportunityService crm.IOpportunityService) *OpportunityController {
	return &OpportunityController{opportunityService: opportunityService}
}
