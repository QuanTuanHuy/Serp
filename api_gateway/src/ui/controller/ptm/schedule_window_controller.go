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

type ScheduleWindowController struct {
	scheduleWindowService service.IScheduleWindowService
}

func NewScheduleWindowController(scheduleWindowService service.IScheduleWindowService) *ScheduleWindowController {
	return &ScheduleWindowController{
		scheduleWindowService: scheduleWindowService,
	}
}

func (s *ScheduleWindowController) ListAvailabilityWindows(c *gin.Context) {
	fromDateMs := utils.ParseInt64Query(c, "fromDateMs")
	toDateMs := utils.ParseInt64Query(c, "toDateMs")
	if fromDateMs == nil || toDateMs == nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "fromDateMs and toDateMs are required and must be numbers")
		return
	}

	result, err := s.scheduleWindowService.ListAvailabilityWindows(c.Request.Context(), *fromDateMs, *toDateMs)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *ScheduleWindowController) MaterializeWindows(c *gin.Context) {
	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	result, err := s.scheduleWindowService.MaterializeWindows(c.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}
