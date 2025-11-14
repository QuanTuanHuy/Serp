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

type DepartmentController struct {
	deptService service.IDepartmentService
}

func (d *DepartmentController) CreateDepartment(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	var body map[string]any
	if err := c.ShouldBindJSON(&body); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}
	res, err := d.deptService.CreateDepartment(c.Request.Context(), orgId, body)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) GetDepartments(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}

	q := map[string]string{}
	if v := c.Query("page"); v != "" {
		q["page"] = v
	}
	if v := c.Query("pageSize"); v != "" {
		q["pageSize"] = v
	}
	if v := c.Query("sortBy"); v != "" {
		q["sortBy"] = v
	}
	if v := c.Query("sortDir"); v != "" {
		q["sortDir"] = v
	}
	if v := c.Query("search"); v != "" {
		q["search"] = v
	}
	if v := c.Query("parentDepartmentId"); v != "" {
		q["parentDepartmentId"] = v
	}
	if v := c.Query("isActive"); v != "" {
		q["isActive"] = v
	}
	if v := c.Query("managerId"); v != "" {
		q["managerId"] = v
	}

	res, err := d.deptService.GetDepartments(c.Request.Context(), orgId, q)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) GetDepartmentById(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	departmentId, ok := utils.ValidateAndParseID(c, "departmentId")
	if !ok {
		return
	}
	res, err := d.deptService.GetDepartmentById(c.Request.Context(), orgId, departmentId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) UpdateDepartment(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	departmentId, ok := utils.ValidateAndParseID(c, "departmentId")
	if !ok {
		return
	}
	var body map[string]any
	if err := c.ShouldBindJSON(&body); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}
	res, err := d.deptService.UpdateDepartment(c.Request.Context(), orgId, departmentId, body)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) DeleteDepartment(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	departmentId, ok := utils.ValidateAndParseID(c, "departmentId")
	if !ok {
		return
	}
	res, err := d.deptService.DeleteDepartment(c.Request.Context(), orgId, departmentId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) GetDepartmentTree(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	departmentId, ok := utils.ValidateAndParseID(c, "departmentId")
	if !ok {
		return
	}
	res, err := d.deptService.GetDepartmentTree(c.Request.Context(), orgId, departmentId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) AssignUserToDepartment(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	departmentId, ok := utils.ValidateAndParseID(c, "departmentId")
	if !ok {
		return
	}
	var body map[string]interface{}
	if err := c.ShouldBindJSON(&body); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}
	res, err := d.deptService.AssignUserToDepartment(c.Request.Context(), orgId, departmentId, body)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) BulkAssignUsersToDepartment(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	departmentId, ok := utils.ValidateAndParseID(c, "departmentId")
	if !ok {
		return
	}
	var body map[string]any
	if err := c.ShouldBindJSON(&body); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}
	res, err := d.deptService.BulkAssignUsersToDepartment(c.Request.Context(), orgId, departmentId, body)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) RemoveUserFromDepartment(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	departmentId, ok := utils.ValidateAndParseID(c, "departmentId")
	if !ok {
		return
	}
	userId, ok := utils.ValidateAndParseID(c, "userId")
	if !ok {
		return
	}
	res, err := d.deptService.RemoveUserFromDepartment(c.Request.Context(), orgId, departmentId, userId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (d *DepartmentController) GetDepartmentMembers(c *gin.Context) {
	orgId, ok := utils.ValidateAndParseID(c, "organizationId")
	if !ok {
		return
	}
	departmentId, ok := utils.ValidateAndParseID(c, "departmentId")
	if !ok {
		return
	}
	res, err := d.deptService.GetDepartmentMembers(c.Request.Context(), orgId, departmentId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewDepartmentController(deptService service.IDepartmentService) *DepartmentController {
	return &DepartmentController{deptService: deptService}
}
