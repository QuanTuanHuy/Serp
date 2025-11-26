/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/usecase"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

type ScheduleGroupController struct {
	scheduleGroupUseCase usecase.IScheduleGroupUseCase
}

func NewScheduleGroupController(scheduleGroupUseCase usecase.IScheduleGroupUseCase) *ScheduleGroupController {
	return &ScheduleGroupController{
		scheduleGroupUseCase: scheduleGroupUseCase,
	}
}

func (s *ScheduleGroupController) GetScheduleGroupByID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	ID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	scheduleGroup, err := s.scheduleGroupUseCase.GetScheduleGroupByID(c, ID, userID)
	if err != nil {
		if err.Error() == constant.ScheduleGroupNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.ScheduleGroupNotFound)
		} else {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, scheduleGroup)
}

func (s *ScheduleGroupController) CreateScheduleGroup(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	var request request.CreateScheduleGroupDto
	if valid := utils.ValidateAndBindJSON(c, &request); !valid {
		return
	}

	scheduleGroup, err := s.scheduleGroupUseCase.CreateScheduleGroup(c, userID, &request)
	if err != nil {
		if err.Error() == constant.SchedulePlanNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.SchedulePlanNotFound)
		} else {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, scheduleGroup)
}

func (s *ScheduleGroupController) GetScheduleGroupsByUserID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	scheduleGroups, err := s.scheduleGroupUseCase.GetScheduleGroupsByUserID(c, userID)
	if err != nil {
		if err.Error() == constant.SchedulePlanNotFound {
			utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		} else {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, scheduleGroups)
}

func (s *ScheduleGroupController) DeleteScheduleGroup(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	scheduleGroupID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	err := s.scheduleGroupUseCase.DeleteScheduleGroup(c, userID, scheduleGroupID)
	if err != nil {
		if err.Error() == constant.DeleteScheduleGroupForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.DeleteScheduleGroupForbidden)
		} else if err.Error() == constant.ScheduleGroupNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, constant.ScheduleGroupNotFound)
		} else {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "delete schedule group successfully")
}
