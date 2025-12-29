/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/usecase"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

type ScheduleTaskController struct {
	scheduleTaskUseCase usecase.IScheduleTaskUseCase
}

func (s *ScheduleTaskController) GetTasksByPlanID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	var planIDPtr *int64
	if c.Query("planId") != "" {
		planIDVal, ok := utils.ValidateAndParseID(c, "planId")
		if !ok {
			return
		}
		planIDPtr = &planIDVal
	}

	tasks, err := s.scheduleTaskUseCase.GetScheduleTasksByUserIDAndPlanID(c.Request.Context(), userID, planIDPtr)
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
