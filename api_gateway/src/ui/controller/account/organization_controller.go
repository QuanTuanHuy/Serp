/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	service "github.com/serp/api-gateway/src/core/service/account"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type OrganizationController struct {
	organizationService service.IOrganizationService
}

func (o *OrganizationController) GetOrganizations(c *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(c)
	if !valid {
		return
	}

	search := utils.ParseStringQuery(c, "search")
	status := utils.ParseStringQuery(c, "status")
	organizationType := utils.ParseStringQuery(c, "type")

	res, err := o.organizationService.GetOrganizations(c.Request.Context(), search, status, organizationType, &page, &pageSize)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (o *OrganizationController) GetOrganizationById(c *gin.Context) {
	organizationID, err := utils.ParseInt64Param(c, "organizationId")
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := o.organizationService.GetOrganizationById(c.Request.Context(), organizationID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (o *OrganizationController) GetMyOrganization(c *gin.Context) {
	res, err := o.organizationService.GetMyOrganization(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewOrganizationController(organizationService service.IOrganizationService) *OrganizationController {
	return &OrganizationController{
		organizationService: organizationService,
	}
}
