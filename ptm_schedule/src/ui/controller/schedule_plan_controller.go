/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto"
	"github.com/serp/ptm-schedule/src/core/usecase"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

type SchedulePlanController struct {
	schedulePlanUseCase usecase.ISchedulePlanUseCase
}

func NewSchedulePlanController(schedulePlanUseCase usecase.ISchedulePlanUseCase) *SchedulePlanController {
	return &SchedulePlanController{
		schedulePlanUseCase: schedulePlanUseCase,
	}
}

// GetActivePlan returns the active schedule plan for the authenticated user
// GET /api/v1/schedule-plans/active
func (ctrl *SchedulePlanController) GetActivePlan(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	plan, err := ctrl.schedulePlanUseCase.GetActivePlanByUserID(c, userID)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, plan)
}

// GetActivePlanDetail returns the active plan with events within date range
// GET /api/v1/schedule-plans/active/detail?fromDateMs=xxx&toDateMs=xxx
func (ctrl *SchedulePlanController) GetActivePlanDetail(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	fromDateMs, ok := utils.ValidateAndParseQueryID(c, "fromDateMs")
	if !ok {
		return
	}
	toDateMs, ok := utils.ValidateAndParseQueryID(c, "toDateMs")
	if !ok {
		return
	}

	response, err := ctrl.schedulePlanUseCase.GetActivePlanDetail(c, userID, fromDateMs, toDateMs)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, response)
}

// GetOrCreateActivePlan gets or creates an active plan for the user
// POST /api/v1/schedule-plans/active
func (ctrl *SchedulePlanController) GetOrCreateActivePlan(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}
	tenantID, _ := utils.GetTenantIDFromContext(c)

	plan, err := ctrl.schedulePlanUseCase.GetOrCreateActivePlan(c, userID, tenantID)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, plan)
}

// GetPlanByID returns a specific plan by ID
// GET /api/v1/schedule-plans/:id
func (ctrl *SchedulePlanController) GetPlanByID(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	planID, ok := utils.ValidateAndParseID(c, "id")
	if !ok {
		return
	}

	plan, err := ctrl.schedulePlanUseCase.GetPlanByID(c, userID, planID)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, plan)
}

// GetPlanWithEvents returns a plan with its events within date range
// GET /api/v1/schedule-plans/:id/events?fromDateMs=xxx&toDateMs=xxx
func (ctrl *SchedulePlanController) GetPlanWithEvents(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	planID, ok := utils.ValidateAndParseID(c, "id")
	if !ok {
		return
	}

	fromDateMs, ok := utils.ValidateAndParseQueryID(c, "fromDateMs")
	if !ok {
		return
	}
	toDateMs, ok := utils.ValidateAndParseQueryID(c, "toDateMs")
	if !ok {
		return
	}

	response, err := ctrl.schedulePlanUseCase.GetPlanWithEvents(c, userID, planID, fromDateMs, toDateMs)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, response)
}

// ApplyProposedPlan activates a proposed plan
// POST /api/v1/schedule-plans/:id/apply
func (ctrl *SchedulePlanController) ApplyProposedPlan(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	planID, ok := utils.ValidateAndParseID(c, "id")
	if !ok {
		return
	}

	plan, err := ctrl.schedulePlanUseCase.ApplyProposedPlan(c, userID, planID)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, plan)
}

// DiscardProposedPlan discards a proposed or draft plan
// DELETE /api/v1/schedule-plans/:id
func (ctrl *SchedulePlanController) DiscardProposedPlan(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	planID, ok := utils.ValidateAndParseID(c, "id")
	if !ok {
		return
	}

	err := ctrl.schedulePlanUseCase.DiscardProposedPlan(c, userID, planID)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, nil)
}

// RevertToPlan reverts to an archived plan
// POST /api/v1/schedule-plans/:id/revert
func (ctrl *SchedulePlanController) RevertToPlan(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	planID, ok := utils.ValidateAndParseID(c, "id")
	if !ok {
		return
	}

	plan, err := ctrl.schedulePlanUseCase.RevertToPlan(c, userID, planID)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, plan)
}

// TriggerReschedule triggers schedule optimization
// POST /api/v1/schedule-plans/reschedule
func (ctrl *SchedulePlanController) TriggerReschedule(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	var req dto.TriggerRescheduleRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	result, err := ctrl.schedulePlanUseCase.TriggerReschedule(c, userID, &req)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, result)
}

// GetPlanHistory returns the plan history for the user
// GET /api/v1/schedule-plans/history?page=1&pageSize=10
func (ctrl *SchedulePlanController) GetPlanHistory(c *gin.Context) {
	userID, ok := utils.GetUserIDFromContext(c)
	if !ok {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}

	page, pageSize, ok := utils.ValidatePaginationParams(c)
	if !ok {
		return
	}

	response, err := ctrl.schedulePlanUseCase.GetPlanHistory(c, userID, page, pageSize)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, response)
}
