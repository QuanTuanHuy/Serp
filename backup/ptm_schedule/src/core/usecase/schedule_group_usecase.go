/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/mapper"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IScheduleGroupUseCase interface {
	CreateScheduleGroup(ctx context.Context, userID int64, req *request.CreateScheduleGroupDto) (*entity.ScheduleGroupEntity, error)
	GetScheduleGroupByID(ctx context.Context, ID, userID int64) (*entity.ScheduleGroupEntity, error)
	GetScheduleGroupsByUserID(ctx context.Context, userID int64) ([]*entity.ScheduleGroupEntity, error)
	DeleteScheduleGroup(ctx context.Context, userID, scheduleGroupID int64) error
}

type ScheduleGroupUseCase struct {
	scheduleGroupService service.IScheduleGroupService
	schedulePlanService  service.ISchedulePlanService
	txService            service.ITransactionService
}

func (s *ScheduleGroupUseCase) GetScheduleGroupsByUserID(ctx context.Context, userID int64) ([]*entity.ScheduleGroupEntity, error) {
	schedulePlan, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	scheduleGroups, err := s.scheduleGroupService.GetScheduleGroupBySPID(ctx, schedulePlan.ID)
	if err != nil {
		return nil, err
	}
	return scheduleGroups, nil
}

func (s *ScheduleGroupUseCase) GetScheduleGroupByID(ctx context.Context, ID, userID int64) (*entity.ScheduleGroupEntity, error) {
	scheduleGroup, err := s.scheduleGroupService.GetScheduleGroupByID(ctx, ID)
	if err != nil {
		return nil, err
	}
	schedulePlan, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if schedulePlan.UserID != userID {
		log.Error(ctx, "Schedule group does not belong to the user's schedule plan")
		return nil, errors.New(constant.ScheduleGroupNotFound)
	}
	return scheduleGroup, nil
}

func (s *ScheduleGroupUseCase) CreateScheduleGroup(ctx context.Context, userID int64, req *request.CreateScheduleGroupDto) (*entity.ScheduleGroupEntity, error) {
	schedulePlan, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}

	scheduleGroup := mapper.ToScheduleGroupEntity(req)
	scheduleGroup.SchedulePlanID = schedulePlan.ID

	result, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return s.scheduleGroupService.CreateScheduleGroup(ctx, tx, scheduleGroup)
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.ScheduleGroupEntity), nil
}

func (s *ScheduleGroupUseCase) DeleteScheduleGroup(ctx context.Context, userID int64, scheduleGroupID int64) error {
	schedulePlan, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return err
	}
	scheduleGroup, err := s.scheduleGroupService.GetScheduleGroupByID(ctx, scheduleGroupID)
	if err != nil {
		return err
	}
	if scheduleGroup.SchedulePlanID != schedulePlan.ID {
		return errors.New(constant.DeleteScheduleGroupForbidden)
	}
	return s.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return s.scheduleGroupService.DeleteScheduleGroup(ctx, tx, scheduleGroupID)
	})
}

func NewScheduleGroupUseCase(scheduleGroupService service.IScheduleGroupService,
	schedulePlanService service.ISchedulePlanService,
	txService service.ITransactionService) IScheduleGroupUseCase {
	return &ScheduleGroupUseCase{
		scheduleGroupService: scheduleGroupService,
		schedulePlanService:  schedulePlanService,
		txService:            txService,
	}
}
