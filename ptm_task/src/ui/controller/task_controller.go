package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type TaskController struct {
	taskUseCase    usecase.ITaskUseCase
	commentUseCase usecase.ICommentUseCase
}

func (tc *TaskController) CreateTask(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	var request request.CreateTaskDTO

	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	task, err := tc.taskUseCase.CreateTask(c, userID, &request)
	if err != nil {
		if err.Error() == constant.GroupTaskNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.GroupTaskNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, task)
}

func (tc *TaskController) UpdateTask(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var request request.UpdateTaskDTO
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	task, err := tc.taskUseCase.UpdateTask(c, userID, taskID, &request)
	if err != nil {
		if err.Error() == constant.TaskNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TaskNotFound)
		} else if err.Error() == constant.UpdateTaskForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.UpdateTaskForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, task)
}

func (tc *TaskController) DeleteTask(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	err := tc.taskUseCase.DeleteTask(c, userID, taskID)
	if err != nil {
		if err.Error() == constant.TaskNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TaskNotFound)
		} else if err.Error() == constant.GetTaskForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.DeleteTaskForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, nil)
}

func (tc *TaskController) GetTaskByID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	task, err := tc.taskUseCase.GetTaskByID(c, userID, taskID)
	if err != nil {
		if err.Error() == constant.TaskNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TaskNotFound)
		} else if err.Error() == constant.GetTaskForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.GetTaskForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, task)
}

func (tc *TaskController) GetCommentsByTaskID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	comments, err := tc.commentUseCase.GetCommentsByTaskID(c, userID, taskID)
	if err != nil {
		if err.Error() == constant.GetCommentForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.GetCommentForbidden)
		} else if err.Error() == constant.TaskNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.TaskNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, comments)
}

func NewTaskController(taskUseCase usecase.ITaskUseCase, commentUseCase usecase.ICommentUseCase) *TaskController {
	return &TaskController{
		taskUseCase:    taskUseCase,
		commentUseCase: commentUseCase,
	}
}
