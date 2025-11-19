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

type SupplierController struct {
	supplierService service.ISupplierService
}

func (s *SupplierController) CreateSupplier(ctx *gin.Context) {
	var req request.CreateSupplierRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.supplierService.CreateSupplier(ctx.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *SupplierController) UpdateSupplier(ctx *gin.Context) {
	supplierId := ctx.Param("supplierId")
	if supplierId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.UpdateSupplierRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.supplierService.UpdateSupplier(ctx.Request.Context(), supplierId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *SupplierController) DeleteSupplier(ctx *gin.Context) {
	supplierId := ctx.Param("supplierId")
	if supplierId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.supplierService.DeleteSupplier(ctx.Request.Context(), supplierId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *SupplierController) GetSupplier(ctx *gin.Context) {
	supplierId := ctx.Param("supplierId")
	if supplierId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.supplierService.GetSupplier(ctx.Request.Context(), supplierId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *SupplierController) GetSuppliers(ctx *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	params := &request.GetSupplierParams{
		Page:          &page,
		Size:          &size,
		SortBy:        sortBy,
		SortDirection: sortDirection,
		Query:         query,
		StatusId:      statusId,
	}

	res, err := s.supplierService.GetSuppliers(ctx.Request.Context(), params)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewSupplierController(supplierService service.ISupplierService) *SupplierController {
	return &SupplierController{
		supplierService: supplierService,
	}
}
