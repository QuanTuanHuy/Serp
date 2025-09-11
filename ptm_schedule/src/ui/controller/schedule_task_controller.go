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

type ScheduleTaskController struct {
	scheduleTaskUseCase usecase.IScheduleTaskUseCase
}

func (s *ScheduleTaskController) GetListTaskByUserID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	tasks, err := s.scheduleTaskUseCase.GetListTaskByUserID(c.Request.Context(), userID)
	if err != nil {
		if err.Error() == constant.SchedulePlanNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.SchedulePlanNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, tasks)
}

func (s *ScheduleTaskController) GetBatchTasks(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	taskMap, err := s.scheduleTaskUseCase.GetBatchTasks(c.Request.Context(), userID)
	if err != nil {
		if err.Error() == constant.SchedulePlanNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.SchedulePlanNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, taskMap)
}

func (s *ScheduleTaskController) ChooseTaskBatch(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	var request request.ChooseTaskBatchRequest
	if !utils.ValidateAndBindJSON(c, &request) {
		return
	}

	tasks, err := s.scheduleTaskUseCase.ChooseTaskBatch(c.Request.Context(), userID, request.BatchNumber)
	if err != nil {
		if err.Error() == constant.SchedulePlanNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.SchedulePlanNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, tasks)
}

func (s *ScheduleTaskController) GetScheduleTaskDetail(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		return
	}
	var request request.GetScheduleTaskRequest
	if !utils.ValidateAndBindQuery(c, &request) {
		return
	}
	if request.TaskID == 0 && request.ScheduleTaskID == 0 {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralBadRequest,
			"either taskId or scheduleTaskId must be provided")
		return
	}
	scheduleTask, err := s.scheduleTaskUseCase.GetScheduleTaskDetail(
		c.Request.Context(), userID, request.TaskID, request.ScheduleTaskID)
	if err != nil {
		if err.Error() == constant.SchedulePlanNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.SchedulePlanNotFound)
		} else if err.Error() == constant.ScheduleTaskNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.ScheduleTaskNotFound)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, scheduleTask)
}

func NewScheduleTaskController(scheduleTaskUseCase usecase.IScheduleTaskUseCase) *ScheduleTaskController {
	return &ScheduleTaskController{
		scheduleTaskUseCase: scheduleTaskUseCase,
	}
}
