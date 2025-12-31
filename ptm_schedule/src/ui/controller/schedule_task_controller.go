/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/mapper"
	"github.com/serp/ptm-schedule/src/core/usecase"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

type ScheduleTaskController struct {
	scheduleTaskUseCase usecase.IScheduleTaskUseCase
}

func (s *ScheduleTaskController) ListScheduleTasks(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var req request.TaskFilterRequest
	if !utils.ValidateAndBindQuery(c, &req) {
		return
	}
	filter := mapper.ToTaskFilter(&req, userID)
	tasks, _, err := s.scheduleTaskUseCase.ListScheduleTasks(c.Request.Context(), userID, filter)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}
	utils.SuccessfulHandle(c, tasks)
}

func NewScheduleTaskController(scheduleTaskUseCase usecase.IScheduleTaskUseCase) *ScheduleTaskController {
	return &ScheduleTaskController{
		scheduleTaskUseCase: scheduleTaskUseCase,
	}
}
