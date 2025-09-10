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

type TaskController struct {
	taskService service.ITaskService
}

func (t *TaskController) CreateTask(c *gin.Context) {
	var req request.CreateTaskRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := t.taskService.CreateTask(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (t *TaskController) GetTaskByID(c *gin.Context) {
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := t.taskService.GetTaskByID(c.Request.Context(), taskID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (t *TaskController) UpdateTask(c *gin.Context) {
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var req request.UpdateTaskRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := t.taskService.UpdateTask(c.Request.Context(), taskID, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (t *TaskController) DeleteTask(c *gin.Context) {
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := t.taskService.DeleteTask(c.Request.Context(), taskID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (t *TaskController) GetCommentsByTaskID(c *gin.Context) {
	taskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := t.taskService.GetCommentsByTaskID(c.Request.Context(), taskID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewTaskController(taskService service.ITaskService) *TaskController {
	return &TaskController{
		taskService: taskService,
	}
}
