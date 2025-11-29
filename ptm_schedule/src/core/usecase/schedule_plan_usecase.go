/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/service"
)

type ISchedulePlanUseCase interface {
	CreatePlan(ctx context.Context, userID, startDate, endDate int64) (*entity.SchedulePlanEntity, error)
	GetActivePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error)
}

type SchedulePlanUseCase struct {
	schedulePlanService service.ISchedulePlanService
	txService           service.ITransactionService
}

func (s *SchedulePlanUseCase) GetActivePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error) {
	panic("unimplemented")
}

func (s *SchedulePlanUseCase) CreatePlan(ctx context.Context, userID int64, startDate int64, endDate int64) (*entity.SchedulePlanEntity, error) {
	panic("unimplemented")
}

func NewSchedulePlanUseCase(schedulePlanService service.ISchedulePlanService, txService service.ITransactionService) ISchedulePlanUseCase {
	return &SchedulePlanUseCase{
		schedulePlanService: schedulePlanService,
		txService:           txService,
	}
}
