/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type ProjectController struct {
	projectUseCase usecase.IProjectUseCase
}

func (pc *ProjectController) CreateProject(c *gin.Context) {
	var req request.CreateProjectRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	utils.SuccessfulHandle(c, "mock success", nil)
}

func NewProjectController(projectUseCase usecase.IProjectUseCase) *ProjectController {
	return &ProjectController{
		projectUseCase: projectUseCase,
	}
}
