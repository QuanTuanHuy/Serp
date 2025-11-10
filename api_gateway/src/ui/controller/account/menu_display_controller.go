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

type MenuDisplayController struct {
	menuDisplayService service.IMenuDisplayService
}

func (m *MenuDisplayController) CreateMenuDisplay(c *gin.Context) {
	var req request.CreateMenuDisplayDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := m.menuDisplayService.CreateMenuDisplay(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *MenuDisplayController) UpdateMenuDisplay(c *gin.Context) {
	id, ok := utils.ValidateAndParseID(c, "id")
	if !ok {
		return
	}

	var req request.UpdateMenuDisplayDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := m.menuDisplayService.UpdateMenuDisplay(c.Request.Context(), id, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *MenuDisplayController) DeleteMenuDisplay(c *gin.Context) {
	id, ok := utils.ValidateAndParseID(c, "id")
	if !ok {
		return
	}

	res, err := m.menuDisplayService.DeleteMenuDisplay(c.Request.Context(), id)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *MenuDisplayController) GetMenuDisplaysByModuleId(c *gin.Context) {
	moduleId, ok := utils.ValidateAndParseID(c, "moduleId")
	if !ok {
		return
	}

	res, err := m.menuDisplayService.GetMenuDisplaysByModuleId(c.Request.Context(), moduleId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *MenuDisplayController) GetAllMenuDisplays(c *gin.Context) {
	params := request.GetMenuDisplayParams{
		Page:          utils.ParseIntQuery(c, "page"),
		PageSize:      utils.ParseIntQuery(c, "pageSize"),
		SortBy:        utils.ParseStringQuery(c, "sortBy"),
		SortDirection: utils.ParseStringQuery(c, "sortDir"),
		ModuleId:      utils.ParseInt64Query(c, "moduleId"),
		Search:        utils.ParseStringQuery(c, "search"),
	}

	res, err := m.menuDisplayService.GetAllMenuDisplays(c.Request.Context(), &params)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *MenuDisplayController) AssignMenuDisplaysToRole(c *gin.Context) {
	var req request.AssignMenuDisplayToRoleDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := m.menuDisplayService.AssignMenuDisplaysToRole(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *MenuDisplayController) GetMenuDisplaysByModuleIdAndUserId(c *gin.Context) {
	moduleId, ok := utils.ValidateAndParseQueryID(c, "moduleId")
	if !ok {
		return
	}

	res, err := m.menuDisplayService.GetMenuDisplaysByModuleIdAndUserId(c.Request.Context(), moduleId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *MenuDisplayController) UnassignMenuDisplaysFromRole(c *gin.Context) {
	var req request.AssignMenuDisplayToRoleDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := m.menuDisplayService.UnassignMenuDisplaysFromRole(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (m *MenuDisplayController) GetMenuDisplaysByRoleIds(c *gin.Context) {
	roleIdsStr := c.Query("roleIds")
	if roleIdsStr == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	roleIds, ok := utils.ValidateAndParseIDsQuery(c, "roleIds")
	if !ok {
		return
	}

	res, err := m.menuDisplayService.GetMenuDisplaysByRoleIds(c.Request.Context(), roleIds)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewMenuDisplayController(menuDisplayService service.IMenuDisplayService) *MenuDisplayController {
	return &MenuDisplayController{
		menuDisplayService: menuDisplayService,
	}
}
