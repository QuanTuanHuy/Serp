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

type ModuleController struct {
	moduleService service.IModuleService
}

func (m *ModuleController) CreateModule(c *gin.Context) {
	var req request.CreateModuleDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := m.moduleService.CreateModule(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleController) GetModuleById(c *gin.Context) {
	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	res, err := m.moduleService.GetModuleById(c.Request.Context(), moduleId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleController) GetRolesInModule(c *gin.Context) {
	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	res, err := m.moduleService.GetRolesInModule(c.Request.Context(), moduleId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleController) UpdateModule(c *gin.Context) {
	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	var req request.UpdateModuleDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := m.moduleService.UpdateModule(c.Request.Context(), moduleId, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleController) GetAllModules(c *gin.Context) {
	res, err := m.moduleService.GetAllModules(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *ModuleController) GetMyModules(c *gin.Context) {
	res, err := m.moduleService.GetMyModules(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewModuleController(moduleService service.IModuleService) *ModuleController {
	return &ModuleController{
		moduleService: moduleService,
	}
}
