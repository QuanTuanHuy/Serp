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

type FacilityController struct {
	facilityService service.IFacilityService
}

func (f *FacilityController) CreateFacility(ctx *gin.Context) {
	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := f.facilityService.CreateFacility(ctx.Request.Context(), req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (f *FacilityController) UpdateFacility(ctx *gin.Context) {
	facilityId := ctx.Param("facilityId")
	if facilityId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := f.facilityService.UpdateFacility(ctx.Request.Context(), facilityId, req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (f *FacilityController) DeleteFacility(ctx *gin.Context) {
	facilityId := ctx.Param("facilityId")
	if facilityId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := f.facilityService.DeleteFacility(ctx.Request.Context(), facilityId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (f *FacilityController) GetFacility(ctx *gin.Context) {
	facilityId := ctx.Param("facilityId")
	if facilityId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := f.facilityService.GetFacility(ctx.Request.Context(), facilityId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (f *FacilityController) GetFacilities(ctx *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	params := &request.GetFacilityParams{
		Page:          &page,
		Size:          &size,
		SortBy:        sortBy,
		SortDirection: sortDirection,
		Query:         query,
		StatusId:      statusId,
	}

	res, err := f.facilityService.GetFacilities(ctx.Request.Context(), params)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewFacilityController(facilityService service.IFacilityService) *FacilityController {
	return &FacilityController{
		facilityService: facilityService,
	}
}
