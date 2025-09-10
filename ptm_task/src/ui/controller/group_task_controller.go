/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/usecase"
	"github.com/serp/ptm-task/src/kernel/utils"
)

type GroupTaskController struct {
	groupTaskUseCase usecase.IGroupTaskUseCase
}

func (g *GroupTaskController) CreateGroupTask(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	var request request.CreateGroupTaskDTO
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}
	groupTask, err := g.groupTaskUseCase.CreateGroupTask(c, userID, request.ProjectID, &request)
	if err != nil {
		if err.Error() == constant.ProjectNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.ProjectNotFound)
			return
		}
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, groupTask)
}

func (g *GroupTaskController) GetGroupTaskByID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	groupTaskID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	groupTask, err := g.groupTaskUseCase.GetGroupTaskByID(c, userID, groupTaskID)
	if err != nil {
		if err.Error() == constant.GroupTaskNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.GroupTaskNotFound)
		} else if err.Error() == constant.GetGroupTaskForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.GetGroupTaskForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, groupTask)
}

func NewGroupTaskController(groupTaskUseCase usecase.IGroupTaskUseCase) *GroupTaskController {
	return &GroupTaskController{
		groupTaskUseCase: groupTaskUseCase,
	}
}
