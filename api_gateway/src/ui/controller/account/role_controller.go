/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	service "github.com/serp/api-gateway/src/core/service/account"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type RoleController struct {
	roleService service.IRoleService
}

func (r *RoleController) CreateRole(c *gin.Context) {
	var req request.CreateRoleDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := r.roleService.CreateRole(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (r *RoleController) GetAllRoles(c *gin.Context) {
	res, err := r.roleService.GetAllRoles(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (r *RoleController) AddPermissionsToRole(c *gin.Context) {
	roleIdStr := c.Param("roleId")
	roleId, err := strconv.ParseInt(roleIdStr, 10, 64)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	var req request.AddPermissionToRoleDto
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := r.roleService.AddPermissionsToRole(c.Request.Context(), roleId, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewRoleController(roleService service.IRoleService) *RoleController {
	return &RoleController{
		roleService: roleService,
	}
}
