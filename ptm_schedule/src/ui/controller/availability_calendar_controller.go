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

type AvailabilityCalendarController struct {
	availabilityUseCase usecase.IAvailabilityCalendarUseCase
}

func (a *AvailabilityCalendarController) GetAvailability(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	entities, err := a.availabilityUseCase.GetAvailabilityByUser(c, userID)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, mapper.ToAvailabilityCalendarResponses(entities))
}

func (a *AvailabilityCalendarController) SetAvailability(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var req request.SetAvailabilityRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	entities := mapper.ToAvailabilityCalendarEntities(userID, req.Items)
	if err := a.availabilityUseCase.SetAvailabilityForUser(c, userID, entities); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "Availability set successfully")
}

func (a *AvailabilityCalendarController) ReplaceAvailability(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}
	var req request.ReplaceAvailabilityRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	entities := mapper.ToAvailabilityCalendarEntities(userID, req.Items)
	if err := a.availabilityUseCase.ReplaceAvailabilityForUser(c, userID, entities); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "Availability replaced successfully")
}

func NewAvailabilityCalendarController(availabilityUseCase usecase.IAvailabilityCalendarUseCase) *AvailabilityCalendarController {
	return &AvailabilityCalendarController{
		availabilityUseCase: availabilityUseCase,
	}
}
