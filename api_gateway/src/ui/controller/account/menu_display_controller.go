/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
"strconv"
"strings"

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
idStr := c.Param("id")
id, err := strconv.ParseInt(idStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
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
idStr := c.Param("id")
id, err := strconv.ParseInt(idStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
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
moduleIdStr := c.Param("moduleId")
moduleId, err := strconv.ParseInt(moduleIdStr, 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}

res, err := m.menuDisplayService.GetMenuDisplaysByModuleId(c.Request.Context(), moduleId)
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

roleIdsStrSlice := strings.Split(roleIdsStr, ",")
roleIds := make([]int64, 0, len(roleIdsStrSlice))
for _, idStr := range roleIdsStrSlice {
id, err := strconv.ParseInt(strings.TrimSpace(idStr), 10, 64)
if err != nil {
utils.AbortErrorHandle(c, constant.GeneralBadRequest)
return
}
roleIds = append(roleIds, id)
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
