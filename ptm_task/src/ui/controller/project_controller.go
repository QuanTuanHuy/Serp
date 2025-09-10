package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type ProjectController struct {
	projectUseCase   usecase.IProjectUseCase
	groupTaskUseCase usecase.IGroupTaskUseCase
}

func (p *ProjectController) CreateProject(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	var request request.CreateProjectDTO

	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	project, err := p.projectUseCase.CreateProject(c, userID, &request)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, project)
}

func (p *ProjectController) UpdateProject(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var request request.UpdateProjectDTO
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	project, err := p.projectUseCase.UpdateProject(c, userID, projectID, &request)
	if err != nil {
		if err.Error() == constant.ProjectNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.ProjectNotFound)
			return
		}
		if err.Error() == constant.UpdateProjectForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.UpdateProjectForbidden)
			return
		}
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}

	utils.SuccessfulHandle(c, project)
}

func (p *ProjectController) GetProjectByID(c *gin.Context) {
	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	project, err := p.projectUseCase.GetProjectByID(c, projectID)
	if err != nil {
		if err.Error() == constant.ProjectNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.ProjectNotFound)
			return
		}
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}

	utils.SuccessfulHandle(c, project)
}

func (p *ProjectController) GetProjectsByUserID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	projects, err := p.projectUseCase.GetProjectsByUserID(c, userID)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}

	utils.SuccessfulHandle(c, projects)
}

func (p *ProjectController) GetProjects(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	var params request.GetProjectParams
	params.UserID = &userID

	page, pageSize, valid := utils.ValidatePaginationParams(c)
	if !valid {
		return
	}
	offset := utils.CalculateOffsetFromPage(page, pageSize)
	if err := c.ShouldBindQuery(&params); err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.InvalidQueryParameters)
		return
	}
	params.Limit = &pageSize
	params.Offset = &offset

	projects, count, err := p.projectUseCase.GetProjects(c, &params)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	dataResponse := utils.MakeDataResponseWithPagination(projects, count)

	utils.SuccessfulHandle(c, dataResponse)
}

func (p *ProjectController) GetProjectsByName(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	searchName := c.Query("name")
	maxDistance := 2
	limit := 10

	if searchName == "" {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.InvalidQueryParameters)
		return
	}

	projects, err := p.projectUseCase.GetProjectsByName(c, userID, searchName, maxDistance, limit)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}

	utils.SuccessfulHandle(c, projects)
}

func (p *ProjectController) GetGroupTasksByProjectID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	groupTasks, err := p.groupTaskUseCase.GetGroupTasksByProjectID(c, userID, projectID)
	if err != nil {
		if err.Error() == constant.ProjectNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.ProjectNotFound)
			return
		}
		if err.Error() == constant.GetGroupTaskForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.GetGroupTaskForbidden)
			return
		}
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, groupTasks)
}

func (p *ProjectController) ArchiveProject(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	projectID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	err := p.projectUseCase.ArchiveProject(c, userID, projectID)
	if err != nil {
		if err.Error() == constant.ProjectNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, err.Error())
			return
		}
		if err.Error() == constant.ArchiveProjectForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, err.Error())
			return
		}
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}

	utils.SuccessfulHandle(c, nil)
}

func NewProjectController(
	projectUseCase usecase.IProjectUseCase,
	groupTaskUseCase usecase.IGroupTaskUseCase) *ProjectController {
	return &ProjectController{
		projectUseCase:   projectUseCase,
		groupTaskUseCase: groupTaskUseCase,
	}
}
