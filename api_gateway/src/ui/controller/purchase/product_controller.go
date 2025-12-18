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

type ProductController struct {
	productService service.IProductService
}

func (p *ProductController) CreateProduct(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.productService.CreateProduct(c.Request.Context(), req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProductController) UpdateProduct(c *gin.Context) {
	productId := c.Param("productId")
	if productId == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.productService.UpdateProduct(c.Request.Context(), productId, req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProductController) DeleteProduct(c *gin.Context) {
	productId := c.Param("productId")
	if productId == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.productService.DeleteProduct(c.Request.Context(), productId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProductController) GetProduct(c *gin.Context) {
	productId := c.Param("productId")
	if productId == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.productService.GetProduct(c.Request.Context(), productId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProductController) GetProducts(c *gin.Context) {
	page, size, valid := utils.ValidatePaginationParams(c)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(c, "sortBy")
	sortDirection := utils.ParseStringQuery(c, "sortDirection")
	query := utils.ParseStringQuery(c, "query")
	statusId := utils.ParseStringQuery(c, "statusId")

	params := &request.GetProductParams{
		Page:          &page,
		Size:          &size,
		SortBy:        sortBy,
		SortDirection: sortDirection,
		Query:         query,
		StatusId:      statusId,
	}

	res, err := p.productService.GetProducts(c.Request.Context(), params)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewProductController(productService service.IProductService) *ProductController {
	return &ProductController{
		productService: productService,
	}
}
