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

type ScheduleEventController struct {
	scheduleEventService service.IScheduleEventService
}

func NewScheduleEventController(scheduleEventService service.IScheduleEventService) *ScheduleEventController {
	return &ScheduleEventController{
		scheduleEventService: scheduleEventService,
	}
}

func (s *ScheduleEventController) ListEvents(c *gin.Context) {
	planID, valid := utils.ValidateAndParseQueryID(c, "planId")
	if !valid {
		return
	}

	fromDateMs := utils.ParseInt64Query(c, "fromDateMs")
	toDateMs := utils.ParseInt64Query(c, "toDateMs")
	if fromDateMs == nil || toDateMs == nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "fromDateMs and toDateMs are required and must be numbers")
		return
	}

	result, err := s.scheduleEventService.ListEvents(c.Request.Context(), planID, *fromDateMs, *toDateMs)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *ScheduleEventController) SaveEvents(c *gin.Context) {
	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	result, err := s.scheduleEventService.SaveEvents(c.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *ScheduleEventController) ManuallyMoveEvent(c *gin.Context) {
	eventID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	result, err := s.scheduleEventService.ManuallyMoveEvent(c.Request.Context(), eventID, payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *ScheduleEventController) CompleteEvent(c *gin.Context) {
	eventID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	result, err := s.scheduleEventService.CompleteEvent(c.Request.Context(), eventID, payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *ScheduleEventController) SplitEvent(c *gin.Context) {
	eventID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	result, err := s.scheduleEventService.SplitEvent(c.Request.Context(), eventID, payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}
