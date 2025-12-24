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

type SchedulePlanController struct {
	schedulePlanService service.ISchedulePlanService
}

func NewSchedulePlanController(schedulePlanService service.ISchedulePlanService) *SchedulePlanController {
	return &SchedulePlanController{
		schedulePlanService: schedulePlanService,
	}
}

func (s *SchedulePlanController) GetOrCreateActivePlan(c *gin.Context) {
	result, err := s.schedulePlanService.GetOrCreateActivePlan(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) GetActivePlan(c *gin.Context) {
	result, err := s.schedulePlanService.GetActivePlan(c.Request.Context())
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) GetActivePlanDetail(c *gin.Context) {
	fromDateMs := utils.ParseInt64Query(c, "fromDateMs")
	toDateMs := utils.ParseInt64Query(c, "toDateMs")
	if fromDateMs == nil || toDateMs == nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "fromDateMs and toDateMs are required and must be numbers")
		return
	}

	result, err := s.schedulePlanService.GetActivePlanDetail(c.Request.Context(), *fromDateMs, *toDateMs)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) GetPlanHistory(c *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(c)
	if !valid {
		return
	}

	result, err := s.schedulePlanService.GetPlanHistory(c.Request.Context(), page, pageSize)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) TriggerReschedule(c *gin.Context) {
	var payload map[string]any
	if err := c.ShouldBindJSON(&payload); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	result, err := s.schedulePlanService.TriggerReschedule(c.Request.Context(), payload)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) GetPlanByID(c *gin.Context) {
	planID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	result, err := s.schedulePlanService.GetPlanByID(c.Request.Context(), planID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) GetPlanWithEvents(c *gin.Context) {
	planID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	fromDateMs := utils.ParseInt64Query(c, "fromDateMs")
	toDateMs := utils.ParseInt64Query(c, "toDateMs")
	if fromDateMs == nil || toDateMs == nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest, "fromDateMs and toDateMs are required and must be numbers")
		return
	}

	result, err := s.schedulePlanService.GetPlanWithEvents(c.Request.Context(), planID, *fromDateMs, *toDateMs)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) ApplyProposedPlan(c *gin.Context) {
	planID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	result, err := s.schedulePlanService.ApplyProposedPlan(c.Request.Context(), planID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) RevertToPlan(c *gin.Context) {
	planID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	result, err := s.schedulePlanService.RevertToPlan(c.Request.Context(), planID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}

func (s *SchedulePlanController) DiscardProposedPlan(c *gin.Context) {
	planID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	result, err := s.schedulePlanService.DiscardProposedPlan(c.Request.Context(), planID)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		return
	}
	c.JSON(result.Code, result)
}
