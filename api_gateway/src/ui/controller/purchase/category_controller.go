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

type CategoryController struct {
	categoryService service.ICategoryService
}

func (c *CategoryController) CreateCategory(ctx *gin.Context) {
	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.categoryService.CreateCategory(ctx.Request.Context(), req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CategoryController) UpdateCategory(ctx *gin.Context) {
	categoryId := ctx.Param("categoryId")
	if categoryId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req map[string]interface{}
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.categoryService.UpdateCategory(ctx.Request.Context(), categoryId, req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CategoryController) DeleteCategory(ctx *gin.Context) {
	categoryId := ctx.Param("categoryId")
	if categoryId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.categoryService.DeleteCategory(ctx.Request.Context(), categoryId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CategoryController) GetCategory(ctx *gin.Context) {
	categoryId := ctx.Param("categoryId")
	if categoryId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.categoryService.GetCategory(ctx.Request.Context(), categoryId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CategoryController) GetCategories(ctx *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	params := &request.GetCategoryParams{
		Page:          &page,
		Size:          &size,
		SortBy:        sortBy,
		SortDirection: sortDirection,
		Query:         query,
		StatusId:      statusId,
	}

	res, err := c.categoryService.GetCategories(ctx.Request.Context(), params)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralInternalServerError)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewCategoryController(categoryService service.ICategoryService) *CategoryController {
	return &CategoryController{
		categoryService: categoryService,
	}
}
