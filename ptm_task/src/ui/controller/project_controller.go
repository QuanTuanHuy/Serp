/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type ProjectController struct {
	projectUseCase usecase.IProjectUseCase
	taskUseCase    usecase.ITaskUseCase
	mapper         *mapper.ProjectMapper
	taskMapper     *mapper.TaskMapper
}

func (pc *ProjectController) CreateProject(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	tenantID, err := utils.GetTenantIDFromContext(c)
	if err != nil {
		return
	}
	var req request.CreateProjectRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	project, err := pc.projectUseCase.CreateProject(
		c.Request.Context(), userID, pc.mapper.CreateRequestToEntity(&req, userID, tenantID))
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}

	utils.SuccessfulHandle(c, "Project created successfully", pc.mapper.EntityToResponse(project, false))
}

func (pc *ProjectController) GetAllProjects(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var filter request.ProjectFilterRequest
	if !utils.ValidateAndBindQuery(c, &filter) {
		return
	}

	projects, err := pc.projectUseCase.GetProjectsByUserID(
		c.Request.Context(), userID, pc.mapper.FilterMapper(&filter))
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	if len(projects) == 0 {
		utils.SuccessfulHandlePagination(c, "No projects found", projects, 0, filter.Page, filter.PageSize)
		return
	}
	count, err := pc.projectUseCase.CountProjectsByUserID(
		c.Request.Context(), userID, pc.mapper.FilterMapper(&filter))
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}

	utils.SuccessfulHandlePagination(c, "Projects retrieved successfully", pc.mapper.EntitiesToResponses(projects, false), count, filter.Page, filter.PageSize)
}

func (pc *ProjectController) GetProjectByID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	project, err := pc.projectUseCase.GetProjectByID(c.Request.Context(), userID, projectID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Project retrieved successfully", pc.mapper.EntityToResponse(project, false))
}

func (pc *ProjectController) GetTasksByProjectID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	tasks, err := pc.taskUseCase.GetTasksByProjectID(c.Request.Context(), userID, projectID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Tasks retrieved successfully", pc.taskMapper.EntitiesToResponses(tasks))
}

func (pc *ProjectController) UpdateProject(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var req request.UpdateProjectRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	err = pc.projectUseCase.UpdateProject(c.Request.Context(), userID, projectID, &req)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Project updated successfully", nil)
}

func (pc *ProjectController) DeleteProject(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	err = pc.projectUseCase.DeleteProject(c.Request.Context(), userID, projectID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Project deleted successfully", nil)
}

func NewProjectController(projectUseCase usecase.IProjectUseCase, taskUseCase usecase.ITaskUseCase) *ProjectController {
	return &ProjectController{
		projectUseCase: projectUseCase,
		taskUseCase:    taskUseCase,
		mapper:         mapper.NewProjectMapper(),
		taskMapper:     mapper.NewTaskMapper(),
	}
}
