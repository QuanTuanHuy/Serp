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

type PermissionController struct {
permissionService service.IPermissionService
}

func (p *PermissionController) CreatePermission(c *gin.Context) {
var req request.CreatePermissionDto
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := p.permissionService.CreatePermission(c.Request.Context(), &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (p *PermissionController) GetAllPermissions(c *gin.Context) {
res, err := p.permissionService.GetAllPermissions(c.Request.Context())
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func NewPermissionController(permissionService service.IPermissionService) *PermissionController {
return &PermissionController{
permissionService: permissionService,
}
}
