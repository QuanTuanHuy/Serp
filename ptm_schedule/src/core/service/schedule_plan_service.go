/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type ISchedulePlanService interface {
	CreatePlan(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error)
	UpdatePlan(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error)
	UpdatePlanWithOptimisticLock(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity, expectedUpdatedAt int64) (*entity.SchedulePlanEntity, error)
	ApplyPlan(ctx context.Context, tx *gorm.DB, userID, planID int64) (*entity.SchedulePlanEntity, error)
	DiscardPlan(ctx context.Context, tx *gorm.DB, userID, planID int64) (*entity.SchedulePlanEntity, error)
	RevertToPlan(ctx context.Context, tx *gorm.DB, userID int64, targetPlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error)

	GetPlanByID(ctx context.Context, ID int64) (*entity.SchedulePlanEntity, error)
	GetOrCreateActivePlan(ctx context.Context, tx *gorm.DB, userID, tenantID int64) (*entity.SchedulePlanEntity, error)
	GetActivePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error)
	GetPlansByUserID(ctx context.Context, userID int64, filter *port.PlanFilter) ([]*entity.SchedulePlanEntity, error)
	GetLatestProposedPlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error)
	CountPlansByUserID(ctx context.Context, userID int64, filter *port.PlanFilter) (int64, error)

	StartOptimization(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity, algo enum.Algorithm) error
	CompleteOptimization(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity, score float64, durationMs int64) error
	FailOptimization(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity, reason string) error
}

type SchedulePlanService struct {
	schedulePlanPort port.ISchedulePlanPort
	dbTxPort         port.IDBTransactionPort
}

func (s *SchedulePlanService) GetPlanByID(ctx context.Context, ID int64) (*entity.SchedulePlanEntity, error) {
	plan, err := s.schedulePlanPort.GetPlanByID(ctx, ID)
	if err != nil {
		return nil, err
	}
	if plan == nil {
		return nil, fmt.Errorf(constant.SchedulePlanNotFound)
	}
	return plan, nil
}

func (s *SchedulePlanService) GetOrCreateActivePlan(ctx context.Context, tx *gorm.DB, userID, tenantID int64) (*entity.SchedulePlanEntity, error) {
	plan, err := s.schedulePlanPort.GetActivePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if plan != nil {
		return plan, nil
	}

	newPlan := entity.NewRollingPlan(userID, tenantID, constant.DefaultSchedulePlanDurationDays)
	newPlan.Status = enum.PlanActive

	created, err := s.schedulePlanPort.CreatePlan(ctx, tx, newPlan)
	if err != nil {
		return nil, err
	}
	return created, nil
}

func (s *SchedulePlanService) ApplyPlan(ctx context.Context, tx *gorm.DB, userID int64, planID int64) (*entity.SchedulePlanEntity, error) {
	candidatePlan, err := s.GetPlanByID(ctx, planID)
	if err != nil {
		return nil, err
	}
	if candidatePlan.UserID != userID {
		return nil, fmt.Errorf(constant.ForbiddenAccess)
	}

	currentActivePlan, err := s.schedulePlanPort.GetActivePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if currentActivePlan != nil && currentActivePlan.ID == candidatePlan.ID {
		return candidatePlan, nil
	}

	if currentActivePlan != nil {
		if err := currentActivePlan.TransitionTo(enum.PlanArchived); err != nil {
			return nil, fmt.Errorf("failed to archive current plan: %w", err)
		}
		if _, err := s.schedulePlanPort.UpdatePlan(ctx, tx, currentActivePlan); err != nil {
			return nil, err
		}
	}

	if err := candidatePlan.TransitionTo(enum.PlanActive); err != nil {
		return nil, fmt.Errorf("failed to activate new plan: %w", err)
	}
	if currentActivePlan != nil {
		candidatePlan.ParentPlanID = &currentActivePlan.ID
	}

	updatedPlan, err := s.schedulePlanPort.UpdatePlan(ctx, tx, candidatePlan)
	if err != nil {
		return nil, err
	}

	return updatedPlan, nil

}

func (s *SchedulePlanService) CreatePlan(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error) {
	plan, err := s.schedulePlanPort.CreatePlan(ctx, tx, plan)
	if err != nil {
		log.Error(ctx, "failed to create plan ", err)
		return nil, err
	}
	return plan, nil
}

func (s *SchedulePlanService) DiscardPlan(ctx context.Context, tx *gorm.DB, userID int64, planID int64) (*entity.SchedulePlanEntity, error) {
	plan, err := s.GetPlanByID(ctx, planID)
	if err != nil {
		return nil, err
	}
	if plan.UserID != userID {
		return nil, fmt.Errorf(constant.ForbiddenAccess)
	}

	if err := plan.TransitionTo(enum.PlanDiscarded); err != nil {
		return nil, err
	}

	updatedPlan, err := s.schedulePlanPort.UpdatePlan(ctx, tx, plan)
	if err != nil {
		return nil, err
	}

	return updatedPlan, nil
}

func (s *SchedulePlanService) GetActivePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error) {
	plan, err := s.schedulePlanPort.GetActivePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if plan == nil {
		return nil, fmt.Errorf(constant.SchedulePlanNotFound)
	}
	return plan, nil
}

func (s *SchedulePlanService) GetLatestProposedPlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error) {
	filter := port.NewPlanFilter()
	filter.Statuses = []string{string(enum.PlanProposed)}
	filter.Limit = 1

	plans, err := s.schedulePlanPort.ListPlansByUserID(ctx, userID, filter)
	if err != nil {
		return nil, err
	}
	if len(plans) == 0 {
		return nil, nil
	}
	return plans[0], nil
}

func (s *SchedulePlanService) GetPlansByUserID(ctx context.Context, userID int64, filter *port.PlanFilter) ([]*entity.SchedulePlanEntity, error) {
	return s.schedulePlanPort.ListPlansByUserID(ctx, userID, filter)
}

func (s *SchedulePlanService) RevertToPlan(ctx context.Context, tx *gorm.DB, userID int64, targetPlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error) {
	if targetPlan.UserID != userID {
		return nil, fmt.Errorf(constant.ForbiddenAccess)
	}
	if targetPlan.Status != enum.PlanArchived {
		return nil, fmt.Errorf(constant.PlanNotArchived)
	}

	newPlanVersion := targetPlan.CreateNextVersion()
	newPlanVersion.Status = enum.PlanActive

	createdPlan, err := s.schedulePlanPort.CreatePlan(ctx, tx, newPlanVersion)
	if err != nil {
		return nil, err
	}

	currentActive, _ := s.schedulePlanPort.GetActivePlanByUserID(ctx, userID)
	if currentActive != nil {
		currentActive.TransitionTo(enum.PlanArchived)
		s.schedulePlanPort.UpdatePlan(ctx, tx, currentActive)
	}

	// Lưu ý: Cần logic copy ScheduleEvents từ targetPlan sang createdPlan (làm ở tầng UseCase)

	return createdPlan, nil
}

func (s *SchedulePlanService) UpdatePlan(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error) {
	return s.schedulePlanPort.UpdatePlan(ctx, tx, schedulePlan)
}

func (s *SchedulePlanService) UpdatePlanWithOptimisticLock(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity, expectedUpdatedAt int64) (*entity.SchedulePlanEntity, error) {
	// Check if plan has been modified by another process
	currentPlan, err := s.schedulePlanPort.GetPlanByID(ctx, plan.ID)
	if err != nil {
		return nil, err
	}
	if currentPlan == nil {
		return nil, fmt.Errorf(constant.SchedulePlanNotFound)
	}
	if currentPlan.UpdatedAt != expectedUpdatedAt {
		return nil, fmt.Errorf(constant.OptimisticLockConflict)
	}

	return s.schedulePlanPort.UpdatePlan(ctx, tx, plan)
}

func (s *SchedulePlanService) CountPlansByUserID(ctx context.Context, userID int64, filter *port.PlanFilter) (int64, error) {
	plans, err := s.schedulePlanPort.ListPlansByUserID(ctx, userID, filter)
	if err != nil {
		return 0, err
	}
	return int64(len(plans)), nil
}

// Optimization lifecycle methods

func (s *SchedulePlanService) StartOptimization(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity, algo enum.Algorithm) error {
	if err := plan.StartOptimization(algo); err != nil {
		return err
	}
	_, err := s.schedulePlanPort.UpdatePlan(ctx, tx, plan)
	return err
}

func (s *SchedulePlanService) CompleteOptimization(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity, score float64, durationMs int64) error {
	if err := plan.CompleteOptimization(score, durationMs); err != nil {
		return err
	}
	_, err := s.schedulePlanPort.UpdatePlan(ctx, tx, plan)
	return err
}

func (s *SchedulePlanService) FailOptimization(ctx context.Context, tx *gorm.DB, plan *entity.SchedulePlanEntity, reason string) error {
	plan.FailOptimization(reason)
	_, err := s.schedulePlanPort.UpdatePlan(ctx, tx, plan)
	return err
}

func NewSchedulePlanService(schedulePlanPort port.ISchedulePlanPort, dbTxPort port.IDBTransactionPort) ISchedulePlanService {
	return &SchedulePlanService{
		schedulePlanPort: schedulePlanPort,
		dbTxPort:         dbTxPort,
	}
}
