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

type ScheduleWindowController struct {
	windowUseCase usecase.IScheduleWindowUseCase
}

func (s *ScheduleWindowController) ListAvailabilityWindows(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	fromDateMs, valid := utils.ValidateAndParseQueryID(c, "fromDateMs")
	if !valid {
		return
	}
	toDateMs, valid := utils.ValidateAndParseQueryID(c, "toDateMs")
	if !valid {
		return
	}

	entities, err := s.windowUseCase.ListWindows(c, userID, fromDateMs, toDateMs)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, mapper.ToScheduleWindowResponses(entities))
}

func (s *ScheduleWindowController) MaterializeWindows(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var req request.MaterializeWindowsRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	if err := s.windowUseCase.MaterializeWindows(c, userID, req.FromDateMs, req.ToDateMs); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "Windows materialized successfully")
}

func NewScheduleWindowController(windowUseCase usecase.IScheduleWindowUseCase) *ScheduleWindowController {
	return &ScheduleWindowController{
		windowUseCase: windowUseCase,
	}
}
