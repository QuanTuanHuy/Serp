/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	service "github.com/serp/api-gateway/src/core/service/ptm"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type ProjectController struct {
	projectService service.IProjectService
}

func (p *ProjectController) GetProjectsByUserID(c *gin.Context) {
	res, err := p.projectService.GetProjectsByUserID(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) CreateProject(c *gin.Context) {
	var req request.CreateProjectRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.projectService.CreateProject(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) UpdateProject(c *gin.Context) {
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var req request.UpdateProjectRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.projectService.UpdateProject(c.Request.Context(), projectID, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) GetProjectByID(c *gin.Context) {
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := p.projectService.GetProjectByID(c.Request.Context(), projectID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) GetProjects(c *gin.Context) {
	var params request.GetProjectsRequest
	if err := c.ShouldBindQuery(&params); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	if params.Page == 0 {
		params.Page = 1
	}
	if params.PageSize == 0 {
		params.PageSize = 10
	}

	res, err := p.projectService.GetProjects(c.Request.Context(), &params)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) GetProjectsByName(c *gin.Context) {
	name := c.Query("name")
	if name == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.projectService.GetProjectsByName(c.Request.Context(), name)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) ArchiveProject(c *gin.Context) {
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := p.projectService.ArchiveProject(c.Request.Context(), projectID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) GetGroupTasksByProjectID(c *gin.Context) {
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := p.projectService.GetGroupTasksByProjectID(c.Request.Context(), projectID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewProjectController(projectService service.IProjectService) *ProjectController {
	return &ProjectController{
		projectService: projectService,
	}
}
