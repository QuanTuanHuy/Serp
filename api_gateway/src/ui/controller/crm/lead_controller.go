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

type LeadController struct {
	leadService crm.ILeadService
}

func (l *LeadController) CreateLead(ctx *gin.Context) {
	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := l.leadService.CreateLead(ctx.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (l *LeadController) UpdateLead(ctx *gin.Context) {
	leadId, ok := utils.ValidateAndParseID(ctx, "leadId")
	if !ok {
		return
	}

	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := l.leadService.UpdateLead(ctx.Request.Context(), leadId, payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (l *LeadController) GetLeadByID(ctx *gin.Context) {
	leadId, ok := utils.ValidateAndParseID(ctx, "leadId")
	if !ok {
		return
	}

	res, err := l.leadService.GetLeadByID(ctx.Request.Context(), leadId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (l *LeadController) GetLeads(ctx *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	res, err := l.leadService.GetLeads(ctx.Request.Context(), page, size)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (l *LeadController) FilterLeads(ctx *gin.Context) {
	payload := map[string]any{}
	if ctx.Request.ContentLength != 0 {
		if err := ctx.ShouldBindJSON(&payload); err != nil {
			utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
			return
		}
	}

	res, err := l.leadService.FilterLeads(ctx.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (l *LeadController) QualifyLead(ctx *gin.Context) {
	leadId, ok := utils.ValidateAndParseID(ctx, "leadId")
	if !ok {
		return
	}

	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	payload["leadId"] = leadId

	res, err := l.leadService.QualifyLead(ctx.Request.Context(), leadId, payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (l *LeadController) ConvertLead(ctx *gin.Context) {
	leadId, ok := utils.ValidateAndParseID(ctx, "leadId")
	if !ok {
		return
	}

	var payload map[string]any
	if err := ctx.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	payload["leadId"] = leadId

	res, err := l.leadService.ConvertLead(ctx.Request.Context(), leadId, payload)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (l *LeadController) DeleteLead(ctx *gin.Context) {
	leadId, ok := utils.ValidateAndParseID(ctx, "leadId")
	if !ok {
		return
	}

	res, err := l.leadService.DeleteLead(ctx.Request.Context(), leadId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewLeadController(leadService crm.ILeadService) *LeadController {
	return &LeadController{leadService: leadService}
}
