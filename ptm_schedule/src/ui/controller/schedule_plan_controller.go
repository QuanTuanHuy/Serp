/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/usecase"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

type SchedulePlanController struct {
	schedulePlanUseCase usecase.ISchedulePlanUseCase
	optimizationUseCase usecase.IOptimizationUseCase
}

func NewSchedulePlanController(
	schedulePlanUseCase usecase.ISchedulePlanUseCase,
	optimizationUseCase usecase.IOptimizationUseCase,
) *SchedulePlanController {
	return &SchedulePlanController{
		schedulePlanUseCase: schedulePlanUseCase,
		optimizationUseCase: optimizationUseCase,
	}
}

func (ctrl *SchedulePlanController) GetActivePlan(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
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

func (ctrl *SchedulePlanController) GetActivePlanDetail(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
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

func (ctrl *SchedulePlanController) GetOrCreateActivePlan(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
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

func (ctrl *SchedulePlanController) GetPlanByID(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
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

func (ctrl *SchedulePlanController) GetPlanWithEvents(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
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

func (ctrl *SchedulePlanController) ApplyProposedPlan(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
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

func (ctrl *SchedulePlanController) DiscardProposedPlan(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	planID, ok := utils.ValidateAndParseID(c, "id")
	if !ok {
		return
	}

	err = ctrl.schedulePlanUseCase.DiscardProposedPlan(c, userID, planID)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, nil)
}

func (ctrl *SchedulePlanController) RevertToPlan(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
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

func (ctrl *SchedulePlanController) TriggerReschedule(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	var req request.TriggerRescheduleRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	result, err := ctrl.optimizationUseCase.TriggerReschedule(c, userID, &req)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, result)
}

func (ctrl *SchedulePlanController) GetPlanHistory(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
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

func (ctrl *SchedulePlanController) DeepOptimize(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	var req request.DeepOptimizeRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	result, err := ctrl.optimizationUseCase.TriggerDeepOptimize(c, userID, &req)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, result)
}

func (ctrl *SchedulePlanController) FallbackChainOptimize(c *gin.Context) {
	userID, err := utils.GetUserIDFromContext(c)
	if err != nil {
		return
	}

	var req request.FallbackChainOptimizeRequest
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	result, err := ctrl.optimizationUseCase.TriggerFallbackChainOptimize(c, userID, &req)
	if err != nil {
		if !utils.HandleBusinessError(c, err) {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
		}
		return
	}

	utils.SuccessfulHandle(c, result)
}
