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

type ModuleAccessController struct {
moduleAccessService service.IModuleAccessService
}

func (m *ModuleAccessController) CanOrganizationAccessModule(c *gin.Context) {
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

moduleIdStr := c.Param("moduleId")
moduleId, err := strconv.ParseInt(moduleIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
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
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
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
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

var req request.AssignUserToModuleRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := m.moduleAccessService.AssignUserToModule(c.Request.Context(), organizationId, &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (m *ModuleAccessController) BulkAssignUsersToModule(c *gin.Context) {
var req request.BulkAssignUsersRequest
if err := c.ShouldBindJSON(&req); err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := m.moduleAccessService.BulkAssignUsersToModule(c.Request.Context(), &req)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
return
}
c.JSON(res.Code, res)
}

func (m *ModuleAccessController) RevokeUserAccessToModule(c *gin.Context) {
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

moduleIdStr := c.Param("moduleId")
moduleId, err := strconv.ParseInt(moduleIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

userIdStr := c.Param("userId")
userId, err := strconv.ParseInt(userIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
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
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

moduleIdStr := c.Param("moduleId")
moduleId, err := strconv.ParseInt(moduleIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
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
organizationIdStr := c.Param("organizationId")
organizationId, err := strconv.ParseInt(organizationIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
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
