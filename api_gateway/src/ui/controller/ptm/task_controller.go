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

type TaskController struct {
	taskService service.ITaskService
}

func (t *TaskController) CreateTask(c *gin.Context) {
	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := t.taskService.CreateTask(c.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (t *TaskController) GetTasksByUserID(c *gin.Context) {
	params := map[string]string{}
	queryParams := c.Request.URL.Query()
	for key, values := range queryParams {
		if len(values) > 0 {
			params[key] = values[0]
		}
	}
	res, err := t.taskService.GetTasksByUserID(c.Request.Context(), params)
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

	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := t.taskService.UpdateTask(c.Request.Context(), taskID, payload)
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

func NewTaskController(taskService service.ITaskService) *TaskController {
	return &TaskController{
		taskService: taskService,
	}
}
