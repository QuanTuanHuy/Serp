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

type CalendarExceptionController struct {
	exceptionUseCase usecase.ICalendarExceptionUseCase
}

func (e *CalendarExceptionController) ListExceptions(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
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

	entities, err := e.exceptionUseCase.ListExceptions(c, userID, fromDateMs, toDateMs)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, mapper.ToCalendarExceptionResponses(entities))
}

func (e *CalendarExceptionController) SaveExceptions(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	var req request.SaveExceptionsRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	entities := mapper.ToCalendarExceptionEntities(userID, req.Items)
	if err := e.exceptionUseCase.SaveExceptions(c, userID, entities); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "Exceptions saved successfully")
}

func (e *CalendarExceptionController) ReplaceExceptions(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}

	var req request.ReplaceExceptionsRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	entities := mapper.ToCalendarExceptionEntities(userID, req.Items)
	if err := e.exceptionUseCase.ReplaceExceptions(c, userID, req.FromDateMs, req.ToDateMs, entities); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "Exceptions replaced successfully")
}

func (e *CalendarExceptionController) DeleteExceptions(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
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

	if err := e.exceptionUseCase.DeleteExceptions(c, userID, fromDateMs, toDateMs); err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, "Exceptions deleted successfully")
}

func NewCalendarExceptionController(exceptionUseCase usecase.ICalendarExceptionUseCase) *CalendarExceptionController {
	return &CalendarExceptionController{
		exceptionUseCase: exceptionUseCase,
	}
}
