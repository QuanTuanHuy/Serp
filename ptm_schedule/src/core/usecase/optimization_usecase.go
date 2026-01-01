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
	"github.com/serp/ptm-schedule/src/core/domain/dto/optimization"
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/dto/response"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IOptimizationUseCase interface {
	TriggerReschedule(ctx context.Context, userID int64, req *request.TriggerRescheduleRequest) (*response.OptimizationResult, error)
	TriggerDeepOptimize(ctx context.Context, userID int64, req *request.DeepOptimizeRequest) (*response.OptimizationResult, error)
	TriggerFallbackChainOptimize(ctx context.Context, userID int64, req *request.FallbackChainOptimizeRequest) (*response.OptimizationResult, error)
}

type OptimizationUseCase struct {
	schedulePlanService  service.ISchedulePlanService
	scheduleEventService service.IScheduleEventService
	scheduleTaskService  service.IScheduleTaskService
	rescheduleService    service.IRescheduleStrategyService
	txService            service.ITransactionService
}

type OptimizationExecutor func(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*service.RescheduleResult, error)

type OptimizationParams struct {
	Algorithm        enum.Algorithm
	BatchStrategy    enum.RescheduleStrategy
	CheckByAlgorithm bool
	Executor         OptimizationExecutor
}

func (s *OptimizationUseCase) TriggerReschedule(ctx context.Context, userID int64, req *request.TriggerRescheduleRequest) (*response.OptimizationResult, error) {
	return s.executeOptimization(ctx, userID, OptimizationParams{
		Algorithm:        enum.HybridAlgorithm,
		BatchStrategy:    req.Strategy,
		CheckByAlgorithm: false,
		Executor: func(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*service.RescheduleResult, error) {
			return s.rescheduleService.Execute(ctx, planID, batch)
		},
	})
}

func (s *OptimizationUseCase) TriggerDeepOptimize(ctx context.Context, userID int64, req *request.DeepOptimizeRequest) (*response.OptimizationResult, error) {
	algorithm := mapStrategyToAlgorithm(req.Strategy)

	return s.executeOptimization(ctx, userID, OptimizationParams{
		Algorithm:        algorithm,
		BatchStrategy:    enum.StrategyOptimalReplan,
		CheckByAlgorithm: true,
		Executor: func(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*service.RescheduleResult, error) {
			return s.rescheduleService.RunDeepOptimize(ctx, planID, batch, req.Strategy, req.MaxTimeSec)
		},
	})
}

func (s *OptimizationUseCase) TriggerFallbackChainOptimize(ctx context.Context, userID int64, req *request.FallbackChainOptimizeRequest) (*response.OptimizationResult, error) {
	return s.executeOptimization(ctx, userID, OptimizationParams{
		Algorithm:        enum.FallbackChainAlgorithm,
		BatchStrategy:    enum.StrategyOptimalReplan,
		CheckByAlgorithm: true,
		Executor: func(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*service.RescheduleResult, error) {
			return s.rescheduleService.RunFallbackChainOptimize(ctx, planID, batch, req.MaxTimeSec)
		},
	})
}

func (s *OptimizationUseCase) executeOptimization(
	ctx context.Context,
	userID int64,
	params OptimizationParams,
) (*response.OptimizationResult, error) {
	activePlan, err := s.schedulePlanService.GetActivePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}

	if err := s.checkExistingProposedPlan(ctx, userID, params); err != nil {
		return nil, err
	}

	startTime := time.Now()

	proposedPlan, err := s.createProposedPlanWithClone(ctx, activePlan, params.Algorithm)
	if err != nil {
		return &response.OptimizationResult{
			Success:      false,
			ErrorMessage: err.Error(),
		}, nil
	}

	if err = s.schedulePlanService.StartOptimization(ctx, nil, proposedPlan, params.Algorithm); err != nil {
		return &response.OptimizationResult{
			Success:      false,
			ErrorMessage: err.Error(),
		}, nil
	}

	batch := &entity.RescheduleBatch{
		UserID:   userID,
		PlanID:   proposedPlan.ID,
		Strategy: params.BatchStrategy,
	}

	rescheduleResult, err := params.Executor(ctx, proposedPlan.ID, batch)
	durationMs := time.Since(startTime).Milliseconds()

	if err != nil {
		s.schedulePlanService.FailOptimization(ctx, nil, proposedPlan, err.Error())
		return &response.OptimizationResult{
			Success:      false,
			DurationMs:   durationMs,
			ErrorMessage: err.Error(),
		}, nil
	}

	return s.completeOptimization(ctx, userID, proposedPlan, rescheduleResult, durationMs)
}

func (s *OptimizationUseCase) checkExistingProposedPlan(ctx context.Context, userID int64, params OptimizationParams) error {
	var existingProposed *entity.SchedulePlanEntity
	var err error

	if params.CheckByAlgorithm {
		existingProposed, err = s.schedulePlanService.GetProposedPlanByAlgorithm(ctx, userID, params.Algorithm)
	} else {
		existingProposed, err = s.schedulePlanService.GetLatestProposedPlanByUserID(ctx, userID)
	}

	if err != nil {
		return err
	}
	if existingProposed != nil {
		return fmt.Errorf(constant.ProposedPlanAlreadyExists)
	}
	return nil
}

func (s *OptimizationUseCase) createProposedPlanWithClone(
	ctx context.Context,
	activePlan *entity.SchedulePlanEntity,
	algorithm enum.Algorithm,
) (*entity.SchedulePlanEntity, error) {
	var proposedPlan *entity.SchedulePlanEntity

	_, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		newPlan := activePlan.CreateNextVersion()
		newPlan.AlgorithmUsed = algorithm

		created, err := s.schedulePlanService.CreatePlan(ctx, tx, newPlan)
		if err != nil {
			return nil, err
		}
		proposedPlan = created

		taskIDMapping, err := s.scheduleTaskService.CloneTasksForPlan(ctx, tx, activePlan.ID, proposedPlan.ID)
		if err != nil {
			return nil, fmt.Errorf("%s: %w", constant.PlanTaskCloneFailed, err)
		}

		events, err := s.scheduleEventService.ListEventsByPlanAndDateRange(
			ctx, activePlan.ID, activePlan.StartDateMs,
			time.Now().AddDate(0, 6, 0).UnixMilli(),
		)
		if err != nil {
			return nil, err
		}

		if len(events) > 0 {
			if _, err := s.scheduleEventService.CloneEventsForPlan(ctx, tx, proposedPlan.ID, events, taskIDMapping); err != nil {
				return nil, fmt.Errorf("%s: %w", constant.PlanEventCloneFailed, err)
			}
		}

		return nil, nil
	})

	return proposedPlan, err
}

func (s *OptimizationUseCase) completeOptimization(
	ctx context.Context,
	userID int64,
	proposedPlan *entity.SchedulePlanEntity,
	result *service.RescheduleResult,
	durationMs int64,
) (*response.OptimizationResult, error) {
	score := 0.0
	if result.Success {
		score = 1.0
	}

	if err := s.schedulePlanService.CompleteOptimization(ctx, nil, proposedPlan, score, durationMs); err != nil {
		log.Warn(ctx, "Failed to complete optimization: ", err)
	}

	proposedDetail, err := s.getPlanWithEvents(
		ctx, userID, proposedPlan.ID,
		proposedPlan.StartDateMs,
		time.Now().AddDate(0, 1, 0).UnixMilli(),
	)
	if err != nil {
		log.Warn(ctx, "Failed to get proposed plan detail: ", err)
	}

	return &response.OptimizationResult{
		Success:          result.Success,
		DurationMs:       int64(result.DurationMs),
		TasksScheduled:   len(result.UpdatedEventIDs),
		TasksUnscheduled: 0,
		ProposedPlan:     proposedDetail,
	}, nil
}

func (s *OptimizationUseCase) getPlanWithEvents(ctx context.Context, userID, planID int64, fromDateMs, toDateMs int64) (*response.PlanDetailResponse, error) {
	plan, err := s.schedulePlanService.GetPlanByID(ctx, planID)
	if err != nil {
		return nil, err
	}
	if plan.UserID != userID {
		return nil, fmt.Errorf(constant.ForbiddenAccess)
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

func (s *OptimizationUseCase) calculatePlanStats(tasks []*entity.ScheduleTaskEntity, events []*entity.ScheduleEventEntity) *response.PlanStats {
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

func mapStrategyToAlgorithm(strategy optimization.StrategyType) enum.Algorithm {
	switch strategy {
	case optimization.StrategyCpSat:
		return enum.CPSATAlgorithm
	case optimization.StrategyMilp:
		return enum.MILPAlgorithm
	case optimization.StrategyLocalSearch:
		return enum.LocalSearchAlgorithm
	case optimization.StrategyHeuristic:
		return enum.HeuristicAlgorithm
	case optimization.StrategyAuto:
		return enum.HybridAlgorithm
	default:
		return enum.HybridAlgorithm
	}
}

func NewOptimizationUseCase(
	schedulePlanService service.ISchedulePlanService,
	scheduleEventService service.IScheduleEventService,
	scheduleTaskService service.IScheduleTaskService,
	rescheduleService service.IRescheduleStrategyService,
	txService service.ITransactionService,
) IOptimizationUseCase {
	return &OptimizationUseCase{
		schedulePlanService:  schedulePlanService,
		scheduleEventService: scheduleEventService,
		scheduleTaskService:  scheduleTaskService,
		rescheduleService:    rescheduleService,
		txService:            txService,
	}
}
