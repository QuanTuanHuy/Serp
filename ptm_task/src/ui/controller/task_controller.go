package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type TaskController struct {
	taskUseCase usecase.ITaskUseCase
	mapper      *mapper.TaskMapper
}

func (t *TaskController) CreateTask(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	tenantID, err := utils.GetTenantIDFromContext(c)
	if err != nil {
		return
	}

	var req request.CreateTaskRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	task, err := t.taskUseCase.CreateTask(c.Request.Context(), userID, tenantID, &req)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Task created successfully", t.mapper.EntityToResponse(task))
}

func (t *TaskController) GetTaskByID(c *gin.Context) {
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	task, err := t.taskUseCase.GetTaskByID(c.Request.Context(), userID, taskID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Task retrieved successfully", t.mapper.EntityToResponse(task))
}

func (t *TaskController) UpdateTask(c *gin.Context) {
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var req request.UpdateTaskRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	task, err := t.taskUseCase.UpdateTask(c.Request.Context(), userID, taskID, &req)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Task updated successfully", t.mapper.EntityToResponse(task))
}

func (t *TaskController) DeleteTask(c *gin.Context) {
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	err = t.taskUseCase.DeleteTask(c.Request.Context(), userID, taskID)
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandle(c, "Task deleted successfully", nil)
}

func (t *TaskController) GetTasksByUserID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var filter request.TaskFilterRequest
	if !utils.ValidateAndBindQuery(c, &filter) {
		return
	}
	tasks, err := t.taskUseCase.GetTasksByUserID(c.Request.Context(), userID, t.mapper.FilterMapper(&filter))
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	if len(tasks) == 0 {
		utils.SuccessfulHandlePagination(c, "No tasks found", t.mapper.EntitiesToResponses(tasks), 0, filter.Page, filter.PageSize)
		return
	}
	count, err := t.taskUseCase.CountTasksByUserID(c.Request.Context(), userID, t.mapper.FilterMapper(&filter))
	if err != nil {
		utils.ErrorHandle(c, err)
		return
	}
	utils.SuccessfulHandlePagination(c, "Tasks retrieved successfully", t.mapper.EntitiesToResponses(tasks), count, filter.Page, filter.PageSize)
}

func NewTaskController(taskUseCase usecase.ITaskUseCase) *TaskController {
	return &TaskController{
		taskUseCase: taskUseCase,
		mapper:      mapper.NewTaskMapper(),
	}
}
