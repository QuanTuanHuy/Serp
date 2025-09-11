/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type ISchedulePlanUseCase interface {
	CreateSchedulePlan(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error)
}

type SchedulePlanUseCase struct {
	schedulePlanService service.ISchedulePlanService
	txService           service.ITransactionService
}

func (s *SchedulePlanUseCase) CreateSchedulePlan(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error) {
	schedulePlan, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		schedulePlan, err := s.schedulePlanService.CreateSchedulePlan(ctx, tx, userID)
		if err != nil {
			return nil, err
		}
		return schedulePlan, nil
	})
	if err != nil {
		return nil, err
	}
	return schedulePlan.(*entity.SchedulePlanEntity), nil
}

func NewSchedulePlanUseCase(schedulePlanService service.ISchedulePlanService, txService service.ITransactionService) ISchedulePlanUseCase {
	return &SchedulePlanUseCase{
		schedulePlanService: schedulePlanService,
		txService:           txService,
	}
}
