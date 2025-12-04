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

type ScheduleEventController struct {
	eventUseCase usecase.IScheduleEventUseCase
}

func (s *ScheduleEventController) ListEvents(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	planID, valid := utils.ValidateAndParseQueryID(c, "planId")
	if !valid {
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

	entities, err := s.eventUseCase.ListEvents(c, userID, planID, fromDateMs, toDateMs)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandleWithMessage(c, mapper.ToScheduleEventResponses(entities), "Events retrieved successfully")
}

func (s *ScheduleEventController) SaveEvents(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var req request.SaveEventsRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	entities := mapper.ToScheduleEventEntities(req.SchedulePlanID, req.Events)
	if err := s.eventUseCase.SaveEvents(c, userID, req.SchedulePlanID, entities); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandleWithMessage(c, nil, "Events saved successfully")
}

func (s *ScheduleEventController) ManuallyMoveEvent(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	eventID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var req request.MoveEventRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	if err := s.eventUseCase.ManuallyMoveEvent(c.Request.Context(), userID, eventID, req.NewDateMs, req.NewStartMin, req.NewEndMin); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}
	utils.SuccessfulHandleWithMessage(c, nil, "Event moved successfully")
}

func (s *ScheduleEventController) CompleteEvent(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	eventID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var req request.CompleteEventRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	if err := s.eventUseCase.CompleteEvent(c.Request.Context(), userID, eventID, req.ActualStartMin, req.ActualEndMin); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}
	utils.SuccessfulHandleWithMessage(c, nil, "Event completed successfully")
}

func (s *ScheduleEventController) SplitEvent(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	eventID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}
	var req request.SplitEventRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	splitResult, err := s.eventUseCase.SplitEvent(c.Request.Context(), userID, eventID, req.SplitPointMin)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}
	utils.SuccessfulHandleWithMessage(c, splitResult, "Event split successfully")
}

func NewScheduleEventController(eventUseCase usecase.IScheduleEventUseCase) *ScheduleEventController {
	return &ScheduleEventController{
		eventUseCase: eventUseCase,
	}
}
