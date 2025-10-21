/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	service "github.com/serp/api-gateway/src/core/service/account"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type ModuleAccessController struct {
	moduleAccessService service.IModuleAccessService
}

func (m *ModuleAccessController) CanOrganizationAccessModule(c *gin.Context) {
	organizationId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}

	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	res, err := m.moduleAccessService.CanOrganizationAccessModule(c.Request.Context(), organizationId, moduleId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleAccessController) GetAccessibleModulesForOrganization(c *gin.Context) {
	organizationId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}

	res, err := m.moduleAccessService.GetAccessibleModulesForOrganization(c.Request.Context(), organizationId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleAccessController) AssignUserToModule(c *gin.Context) {
	organizationId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}

	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	var req request.AssignUserToModuleRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	// Set moduleId from path parameter
	req.ModuleId = moduleId

	res, err := m.moduleAccessService.AssignUserToModule(c.Request.Context(), organizationId, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleAccessController) BulkAssignUsersToModule(c *gin.Context) {
	organizationId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}

	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	var req request.BulkAssignUsersRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	// Set organizationId and moduleId from path parameters
	req.OrganizationId = organizationId
	req.ModuleId = moduleId

	res, err := m.moduleAccessService.BulkAssignUsersToModule(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleAccessController) RevokeUserAccessToModule(c *gin.Context) {
	organizationId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}

	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	userId, ok := utils.ValidateAndParseID(c, "userId")
	if !ok {
		return
	}

	res, err := m.moduleAccessService.RevokeUserAccessToModule(c.Request.Context(), organizationId, moduleId, userId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleAccessController) GetUsersWithAccessToModule(c *gin.Context) {
	organizationId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}

	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	res, err := m.moduleAccessService.GetUsersWithAccessToModule(c.Request.Context(), organizationId, moduleId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleAccessController) GetModulesAccessibleByUser(c *gin.Context) {
	organizationId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}

	res, err := m.moduleAccessService.GetModulesAccessibleByUser(c.Request.Context(), organizationId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewModuleAccessController(moduleAccessService service.IModuleAccessService) *ModuleAccessController {
	return &ModuleAccessController{
		moduleAccessService: moduleAccessService,
	}
}
