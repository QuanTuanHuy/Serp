/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/serp/ptm-schedule/src/core/usecase"
)

type SchedulePlanController struct {
	schedulePlanUseCase usecase.ISchedulePlanUseCase
}

func NewSchedulePlanController(schedulePlanUseCase usecase.ISchedulePlanUseCase) *SchedulePlanController {
	return &SchedulePlanController{
		schedulePlanUseCase: schedulePlanUseCase,
	}
}
