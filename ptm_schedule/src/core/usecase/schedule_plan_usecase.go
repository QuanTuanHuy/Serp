/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"fmt"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/dto/response"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

const (
	MaxArchivedPlans       = 10
	ArchivedRetentionDays  = 30
	DiscardedRetentionDays = 7
)

type ISchedulePlanUseCase interface {
	// Core CRUD
	CreatePlan(ctx context.Context, userID, tenantID int64, req *request.CreateProposedPlanRequest) (*entity.SchedulePlanEntity, error)
	GetActivePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error)
	GetPlanByID(ctx context.Context, userID, planID int64) (*entity.SchedulePlanEntity, error)

	// Plan with events
	GetOrCreateActivePlan(ctx context.Context, userID, tenantID int64) (*entity.SchedulePlanEntity, error)
	GetPlanWithEvents(ctx context.Context, userID, planID int64, fromDateMs, toDateMs int64) (*response.PlanDetailResponse, error)
	GetActivePlanDetail(ctx context.Context, userID int64, fromDateMs, toDateMs int64) (*response.PlanDetailResponse, error)

	// Plan lifecycle
	ApplyProposedPlan(ctx context.Context, userID, planID int64) (*entity.SchedulePlanEntity, error)
	DiscardProposedPlan(ctx context.Context, userID, planID int64) error
	RevertToPlan(ctx context.Context, userID, targetPlanID int64) (*entity.SchedulePlanEntity, error)

	// Optimization
	TriggerReschedule(ctx context.Context, userID int64, req *request.TriggerRescheduleRequest) (*response.OptimizationResult, error)

	// History & Cleanup
	GetPlanHistory(ctx context.Context, userID int64, page, pageSize int) (*response.PlanHistoryResponse, error)
	CleanupOldPlans(ctx context.Context, userID int64) (int, error)
}

type SchedulePlanUseCase struct {
	schedulePlanService  service.ISchedulePlanService
	scheduleEventService service.IScheduleEventService
	scheduleTaskService  service.IScheduleTaskService
	rescheduleService    service.IRescheduleStrategyService
	txService            service.ITransactionService
	scheduleEventPort    port.IScheduleEventPort
	scheduleTaskPort     port.IScheduleTaskPort
}

func (s *SchedulePlanUseCase) GetActivePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error) {
	return s.schedulePlanService.GetActivePlanByUserID(ctx, userID)
}

func (s *SchedulePlanUseCase) GetPlanByID(ctx context.Context, userID, planID int64) (*entity.SchedulePlanEntity, error) {
	plan, err := s.schedulePlanService.GetPlanByID(ctx, planID)
	if err != nil {
		return nil, err
	}
	if plan.UserID != userID {
		return nil, fmt.Errorf(constant.ForbiddenAccess)
	}
	return plan, nil
}

func (s *SchedulePlanUseCase) GetOrCreateActivePlan(ctx context.Context, userID, tenantID int64) (*entity.SchedulePlanEntity, error) {
	result, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return s.schedulePlanService.GetOrCreateActivePlan(ctx, tx, userID, tenantID)
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.SchedulePlanEntity), nil
}

func (s *SchedulePlanUseCase) CreatePlan(ctx context.Context, userID, tenantID int64, req *request.CreateProposedPlanRequest) (*entity.SchedulePlanEntity, error) {
	result, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		newPlan := entity.NewRollingPlan(userID, tenantID, constant.DefaultSchedulePlanDurationDays)
		newPlan.AlgorithmUsed = req.Algorithm

		activePlan, _ := s.schedulePlanService.GetActivePlanByUserID(ctx, userID)
		if activePlan != nil {
			newPlan.ParentPlanID = &activePlan.ID
		}

		createdPlan, err := s.schedulePlanService.CreatePlan(ctx, tx, newPlan)
		if err != nil {
			return nil, err
		}

		return createdPlan, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.SchedulePlanEntity), nil
}

func (s *SchedulePlanUseCase) GetPlanWithEvents(ctx context.Context, userID, planID int64, fromDateMs, toDateMs int64) (*response.PlanDetailResponse, error) {
	plan, err := s.GetPlanByID(ctx, userID, planID)
	if err != nil {
		return nil, err
	}

	if fromDateMs > toDateMs {
		return nil, fmt.Errorf(constant.InvalidDateRange)
	}

	events, err := s.scheduleEventService.ListEventsByPlanAndDateRange(ctx, planID, fromDateMs, toDateMs)
	if err != nil {
		return nil, err
	}

	scheduleTaskIDs := make([]int64, 0, len(events))
	for _, e := range events {
		scheduleTaskIDs = append(scheduleTaskIDs, e.ScheduleTaskID)
	}
	tasks, err := s.scheduleTaskService.GetByScheduleTaskIDs(ctx, scheduleTaskIDs)
	if err != nil {
		return nil, err
	}

	stats := s.calculatePlanStats(tasks, events)

	return &response.PlanDetailResponse{
		Plan:   plan,
		Events: events,
		Tasks:  tasks,
		Stats:  stats,
	}, nil
}

func (s *SchedulePlanUseCase) GetActivePlanDetail(ctx context.Context, userID int64, fromDateMs, toDateMs int64) (*response.PlanDetailResponse, error) {
	plan, err := s.schedulePlanService.GetActivePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}

	return s.GetPlanWithEvents(ctx, userID, plan.ID, fromDateMs, toDateMs)
}

func (s *SchedulePlanUseCase) ApplyProposedPlan(ctx context.Context, userID, planID int64) (*entity.SchedulePlanEntity, error) {
	plan, err := s.schedulePlanService.GetPlanByID(ctx, planID)
	if err != nil {
		return nil, err
	}
	if plan.UserID != userID {
		return nil, fmt.Errorf(constant.ForbiddenAccess)
	}
	if plan.Status != enum.PlanProposed {
		return nil, fmt.Errorf(constant.PlanNotProposed)
	}

	result, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return s.schedulePlanService.ApplyPlan(ctx, tx, userID, planID)
	})
	if err != nil {
		return nil, err
	}

	go func() {
		ctx := context.Background()
		if count, err := s.CleanupOldPlans(ctx, userID); err != nil {
			log.Warn(ctx, "Failed to cleanup old plans: ", err)
		} else if count > 0 {
			log.Info(ctx, "Cleaned up ", count, " old plans")
		}
	}()

	return result.(*entity.SchedulePlanEntity), nil
}

func (s *SchedulePlanUseCase) DiscardProposedPlan(ctx context.Context, userID, planID int64) error {
	_, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return s.schedulePlanService.DiscardPlan(ctx, tx, userID, planID)
	})
	return err
}

func (s *SchedulePlanUseCase) RevertToPlan(ctx context.Context, userID, targetPlanID int64) (*entity.SchedulePlanEntity, error) {
	result, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		targetPlan, err := s.schedulePlanService.GetPlanByID(ctx, targetPlanID)
		if err != nil {
			return nil, err
		}

		// Create new plan from target
		newPlan, err := s.schedulePlanService.RevertToPlan(ctx, tx, userID, targetPlan)
		if err != nil {
			return nil, err
		}

		// Clone events from target plan to new plan
		fromDateMs := targetPlan.StartDateMs
		toDateMs := int64(0)
		if targetPlan.EndDateMs != nil {
			toDateMs = *targetPlan.EndDateMs
		} else {
			toDateMs = time.Now().AddDate(0, 6, 0).UnixMilli()
		}

		events, err := s.scheduleEventService.ListEventsByPlanAndDateRange(ctx, targetPlanID, fromDateMs, toDateMs)
		if err != nil {
			return nil, err
		}

		if len(events) > 0 {
			clonedEvents := make([]*entity.ScheduleEventEntity, 0, len(events))
			for _, e := range events {
				clone := e.Clone()
				clone.ID = 0
				clone.SchedulePlanID = newPlan.ID
				clonedEvents = append(clonedEvents, clone)
			}
			if err := s.scheduleEventService.CreateBatch(ctx, tx, clonedEvents); err != nil {
				return nil, fmt.Errorf("%s: %w", constant.PlanEventCloneFailed, err)
			}
		}

		// Note: ScheduleTasks are not cloned here because they are managed separately
		// and linked to external PTM tasks. The events reference schedule_task_id which
		// should still be valid if tasks haven't been deleted.

		return newPlan, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.SchedulePlanEntity), nil
}

func (s *SchedulePlanUseCase) TriggerReschedule(ctx context.Context, userID int64, req *request.TriggerRescheduleRequest) (*response.OptimizationResult, error) {
	activePlan, err := s.schedulePlanService.GetActivePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}

	// Check if plan is already being optimized
	if activePlan.Status == enum.PlanProcessing {
		return nil, fmt.Errorf(constant.PlanAlreadyProcessing)
	}

	startTime := time.Now()

	// Start optimization in transaction
	_, err = s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return nil, s.schedulePlanService.StartOptimization(ctx, tx, activePlan, enum.HybridAlgorithm)
	})
	if err != nil {
		return &response.OptimizationResult{
			Success:      false,
			ErrorMessage: err.Error(),
		}, nil
	}

	// Create batch with all tasks
	batch := &entity.RescheduleBatch{
		UserID: userID,
	}

	// Run reschedule based on strategy
	var rescheduleResult *service.RescheduleResult
	switch req.Strategy {
	case enum.StrategyRipple:
		rescheduleResult, err = s.rescheduleService.RunRipple(ctx, activePlan.ID, batch)
	case enum.StrategyInsertion:
		rescheduleResult, err = s.rescheduleService.RunInsertion(ctx, activePlan.ID, batch)
	case enum.StrategyFullReplan:
		rescheduleResult, err = s.rescheduleService.RunFullReplan(ctx, activePlan.ID, batch)
	default:
		rescheduleResult, err = s.rescheduleService.RunRipple(ctx, activePlan.ID, batch)
	}

	durationMs := time.Since(startTime).Milliseconds()

	if err != nil {
		// Mark optimization as failed
		s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
			return nil, s.schedulePlanService.FailOptimization(ctx, tx, activePlan, err.Error())
		})
		return &response.OptimizationResult{
			Success:      false,
			DurationMs:   durationMs,
			ErrorMessage: err.Error(),
		}, nil
	}

	// Complete optimization
	_, err = s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		score := 0.0
		if rescheduleResult.Success {
			score = 1.0
		}
		return nil, s.schedulePlanService.CompleteOptimization(ctx, tx, activePlan, score, durationMs)
	})
	if err != nil {
		log.Warn(ctx, "Failed to complete optimization: ", err)
	}

	return &response.OptimizationResult{
		Success:          rescheduleResult.Success,
		DurationMs:       int64(rescheduleResult.DurationMs),
		TasksScheduled:   len(rescheduleResult.UpdatedEventIDs),
		TasksUnscheduled: 0, // TODO: get from reschedule result
	}, nil
}

func (s *SchedulePlanUseCase) GetPlanHistory(ctx context.Context, userID int64, page, pageSize int) (*response.PlanHistoryResponse, error) {
	if page < 1 {
		page = 1
	}
	if pageSize < 1 || pageSize > 50 {
		pageSize = 10
	}

	filter := port.NewPlanFilter()
	filter.Statuses = []string{string(enum.PlanArchived)}
	filter.Offset = (page - 1) * pageSize
	filter.Limit = pageSize

	plans, err := s.schedulePlanService.GetPlansByUserID(ctx, userID, filter)
	if err != nil {
		return nil, err
	}

	totalCount, err := s.schedulePlanService.CountPlansByUserID(ctx, userID, filter)
	if err != nil {
		return nil, err
	}

	return &response.PlanHistoryResponse{
		Plans:      plans,
		TotalCount: int(totalCount),
	}, nil
}

func (s *SchedulePlanUseCase) CleanupOldPlans(ctx context.Context, userID int64) (int, error) {
	deletedCount := 0

	// 1. Delete discarded plans older than DiscardedRetentionDays
	discardedCutoff := time.Now().AddDate(0, 0, -DiscardedRetentionDays).UnixMilli()
	discardedFilter := port.NewPlanFilter()
	discardedFilter.Statuses = []string{string(enum.PlanDiscarded)}

	discardedPlans, err := s.schedulePlanService.GetPlansByUserID(ctx, userID, discardedFilter)
	if err != nil {
		return 0, err
	}

	for _, plan := range discardedPlans {
		if plan.UpdatedAt < discardedCutoff {
			// TODO: implement DeletePlan in service/port
			// s.schedulePlanService.DeletePlan(ctx, plan.ID)
			deletedCount++
		}
	}

	// 2. Keep only MaxArchivedPlans archived plans (newest)
	archivedFilter := port.NewPlanFilter()
	archivedFilter.Statuses = []string{string(enum.PlanArchived)}

	archivedPlans, err := s.schedulePlanService.GetPlansByUserID(ctx, userID, archivedFilter)
	if err != nil {
		return deletedCount, err
	}

	// Plans are sorted by UpdatedAt DESC, delete oldest beyond limit
	if len(archivedPlans) > MaxArchivedPlans {
		for i := MaxArchivedPlans; i < len(archivedPlans); i++ {
			// Check if also older than ArchivedRetentionDays
			retentionCutoff := time.Now().AddDate(0, 0, -ArchivedRetentionDays).UnixMilli()
			if archivedPlans[i].UpdatedAt < retentionCutoff {
				// TODO: implement DeletePlan in service/port
				// s.schedulePlanService.DeletePlan(ctx, archivedPlans[i].ID)
				deletedCount++
			}
		}
	}

	return deletedCount, nil
}

func (s *SchedulePlanUseCase) calculatePlanStats(tasks []*entity.ScheduleTaskEntity, events []*entity.ScheduleEventEntity) *response.PlanStats {
	totalTasks := len(tasks)
	scheduledTaskIDs := make(map[int64]bool)
	totalDurationMin := 0
	scheduledMin := 0

	for _, t := range tasks {
		totalDurationMin += t.DurationMin
	}

	for _, e := range events {
		scheduledTaskIDs[e.ScheduleTaskID] = true
		scheduledMin += e.EndMin - e.StartMin
	}

	scheduledTasks := len(scheduledTaskIDs)
	unscheduledTasks := totalTasks - scheduledTasks

	utilizationPct := 0.0
	if totalDurationMin > 0 {
		utilizationPct = float64(scheduledMin) / float64(totalDurationMin) * 100
	}

	return &response.PlanStats{
		TotalTasks:       totalTasks,
		ScheduledTasks:   scheduledTasks,
		UnscheduledTasks: unscheduledTasks,
		TotalDurationMin: totalDurationMin,
		ScheduledMin:     scheduledMin,
		UtilizationPct:   utilizationPct,
	}
}

func NewSchedulePlanUseCase(
	schedulePlanService service.ISchedulePlanService,
	scheduleEventService service.IScheduleEventService,
	scheduleTaskService service.IScheduleTaskService,
	rescheduleService service.IRescheduleStrategyService,
	txService service.ITransactionService,
	scheduleEventPort port.IScheduleEventPort,
	scheduleTaskPort port.IScheduleTaskPort,
) ISchedulePlanUseCase {
	return &SchedulePlanUseCase{
		schedulePlanService:  schedulePlanService,
		scheduleEventService: scheduleEventService,
		scheduleTaskService:  scheduleTaskService,
		rescheduleService:    rescheduleService,
		txService:            txService,
		scheduleEventPort:    scheduleEventPort,
		scheduleTaskPort:     scheduleTaskPort,
	}
}
