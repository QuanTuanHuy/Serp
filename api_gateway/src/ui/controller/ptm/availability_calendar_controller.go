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

type AvailabilityCalendarController struct {
	availabilityCalendarService service.IAvailabilityCalendarService
}

func NewAvailabilityCalendarController(availabilityCalendarService service.IAvailabilityCalendarService) *AvailabilityCalendarController {
	return &AvailabilityCalendarController{
		availabilityCalendarService: availabilityCalendarService,
	}
}

func (a *AvailabilityCalendarController) GetAvailability(c *gin.Context) {
	result, err := a.availabilityCalendarService.GetAvailability(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (a *AvailabilityCalendarController) SetAvailability(c *gin.Context) {
	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	result, err := a.availabilityCalendarService.SetAvailability(c.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (a *AvailabilityCalendarController) ReplaceAvailability(c *gin.Context) {
	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	result, err := a.availabilityCalendarService.ReplaceAvailability(c.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}
