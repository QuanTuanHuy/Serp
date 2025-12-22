/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	service "github.com/serp/api-gateway/src/core/service/ptm"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type ProjectController struct {
	projectService service.IProjectService
}

func (p *ProjectController) CreateProject(c *gin.Context) {
	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.projectService.CreateProject(c.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) GetAllProjects(c *gin.Context) {
	payload := map[string]any{}
	queryParams := c.Request.URL.Query()
	for key, values := range queryParams {
		if len(values) > 0 {
			payload[key] = values[0]
		}
	}

	res, err := p.projectService.GetAllProjects(c.Request.Context(), payload)
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

func (p *ProjectController) GetTasksByProjectID(c *gin.Context) {
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := p.projectService.GetTasksByProjectID(c.Request.Context(), projectID)
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

	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := p.projectService.UpdateProject(c.Request.Context(), projectID, payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (p *ProjectController) DeleteProject(c *gin.Context) {
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := p.projectService.DeleteProject(c.Request.Context(), projectID)
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
