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

	entities, err := s.eventUseCase.ListEvents(c, planID, fromDateMs, toDateMs)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, mapper.ToScheduleEventResponses(entities))
}

func (s *ScheduleEventController) SaveEvents(c *gin.Context) {
	var req request.SaveEventsRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	entities := mapper.ToScheduleEventEntities(req.SchedulePlanID, req.Events)
	if err := s.eventUseCase.SaveEvents(c, req.SchedulePlanID, entities); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "Events saved successfully")
}

func (s *ScheduleEventController) UpdateEventStatus(c *gin.Context) {
	eventID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var req request.UpdateEventStatusRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}
	if err := s.eventUseCase.UpdateEventStatus(c, eventID, req.Status, req.ActualStartMin, req.ActualEndMin); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "Event status updated successfully")
}

func NewScheduleEventController(eventUseCase usecase.IScheduleEventUseCase) *ScheduleEventController {
	return &ScheduleEventController{
		eventUseCase: eventUseCase,
	}
}
