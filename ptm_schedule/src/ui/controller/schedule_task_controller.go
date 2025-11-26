/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/serp/ptm-schedule/src/core/usecase"
)

type ScheduleTaskController struct {
	scheduleTaskUseCase usecase.IScheduleTaskUseCase
}

func NewScheduleTaskController(scheduleTaskUseCase usecase.IScheduleTaskUseCase) *ScheduleTaskController {
	return &ScheduleTaskController{
		scheduleTaskUseCase: scheduleTaskUseCase,
	}
}
