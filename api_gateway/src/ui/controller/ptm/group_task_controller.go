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

type GroupTaskController struct {
	groupTaskService service.IGroupTaskService
}

func (g *GroupTaskController) CreateGroupTask(c *gin.Context) {
	var req request.CreateGroupTaskRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := g.groupTaskService.CreateGroupTask(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func (g *GroupTaskController) GetGroupTaskByID(c *gin.Context) {
	groupTaskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	res, err := g.groupTaskService.GetGroupTaskByID(c.Request.Context(), groupTaskID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(res.Code, res)
}

func NewGroupTaskController(groupTaskService service.IGroupTaskService) *GroupTaskController {
	return &GroupTaskController{
		groupTaskService: groupTaskService,
	}
}
