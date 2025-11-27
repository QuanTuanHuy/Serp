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

type SchedulePlanController struct {
	schedulePlanUseCase usecase.ISchedulePlanUseCase
}

func (s *SchedulePlanController) CreateSchedulePlan(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	schedulePlan, err := s.schedulePlanUseCase.CreateSchedulePlan(c, userID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	utils.SuccessfulHandle(c, schedulePlan)
}

func NewSchedulePlanController(schedulePlanUseCase usecase.ISchedulePlanUseCase) *SchedulePlanController {
	return &SchedulePlanController{
		schedulePlanUseCase: schedulePlanUseCase,
	}
}
