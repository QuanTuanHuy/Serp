/*
Author: QuanTuanHuy
Description: Part of Serp Project - Local scheduling using HybridScheduler
*/

package service

import (
	"context"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/algorithm"
	"github.com/serp/ptm-schedule/src/core/domain/dto/optimization"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	clientPort "github.com/serp/ptm-schedule/src/core/port/client"
	storePort "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

const (
	DefaultScheduleRangeDays = 14
)

type RescheduleResult struct {
	Success         bool
	UpdatedEventIDs []int64
	Strategy        enum.RescheduleStrategy
	DurationMs      int
	Error           error
}

type IRescheduleStrategyService interface {
	Execute(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error)
	RunRipple(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error)
	RunInsertion(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error)
	RunFullReplan(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error)
	RunOptimalReplan(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error)

	RunDeepOptimize(ctx context.Context, planID int64, batch *entity.RescheduleBatch, strategy optimization.StrategyType, maxTimeSec *int) (*RescheduleResult, error)

	RunFallbackChainOptimize(ctx context.Context, planID int64, batch *entity.RescheduleBatch, maxTimeSec *int) (*RescheduleResult, error)
}

type RescheduleStrategyService struct {
	taskPort           storePort.IScheduleTaskPort
	eventPort          storePort.IScheduleEventPort
	windowService      IScheduleWindowService
	optimizationClient clientPort.IOptimizationClient
	scheduler          *algorithm.HybridScheduler
	mapper             *algorithm.AlgorithmMapper
}

func NewRescheduleStrategyService(
	taskPort storePort.IScheduleTaskPort,
	eventPort storePort.IScheduleEventPort,
	windowService IScheduleWindowService,
	optimizationClient clientPort.IOptimizationClient,
) IRescheduleStrategyService {
	return &RescheduleStrategyService{
		taskPort:           taskPort,
		eventPort:          eventPort,
		windowService:      windowService,
		optimizationClient: optimizationClient,
		scheduler:          algorithm.NewHybridScheduler(),
		mapper:             algorithm.NewAlgorithmMapper(),
	}
}

func (s *RescheduleStrategyService) Execute(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error) {
	switch batch.Strategy {
	case enum.StrategyRipple:
		return s.RunRipple(ctx, planID, batch)
	case enum.StrategyInsertion:
		return s.RunInsertion(ctx, planID, batch)
	case enum.StrategyFullReplan:
		return s.RunFullReplan(ctx, planID, batch)
	case enum.StrategyOptimalReplan:
		return s.RunOptimalReplan(ctx, planID, batch)
	default:
		return s.RunRipple(ctx, planID, batch)
	}
}

// RunRipple uses full HybridScheduler (Greedy Insertion + Ripple Effect for critical tasks)
func (s *RescheduleStrategyService) RunRipple(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error) {
	return s.runSchedule(ctx, planID, batch, enum.StrategyRipple, true)
}

// RunInsertion uses only Greedy Insertion (no ripple effect)
func (s *RescheduleStrategyService) RunInsertion(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error) {
	return s.runSchedule(ctx, planID, batch, enum.StrategyInsertion, false)
}

// RunFullReplan re-schedules all tasks from scratch
func (s *RescheduleStrategyService) RunFullReplan(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error) {
	return s.runFullReplan(ctx, planID, batch)
}

// RunOptimalReplan uses ptm_optimization service for deep optimization
func (s *RescheduleStrategyService) RunOptimalReplan(ctx context.Context, planID int64, batch *entity.RescheduleBatch) (*RescheduleResult, error) {
	return s.runOptimalReplan(ctx, planID, batch)
}

func (s *RescheduleStrategyService) runSchedule(
	ctx context.Context,
	planID int64,
	batch *entity.RescheduleBatch,
	strategy enum.RescheduleStrategy,
	useRipple bool,
) (*RescheduleResult, error) {
	startTime := time.Now()

	input, taskMap, err := s.loadScheduleData(ctx, planID, batch.UserID)
	if err != nil {
		return nil, err
	}

	if len(input.Tasks) == 0 {
		return &RescheduleResult{
			Success:         true,
			UpdatedEventIDs: []int64{},
			Strategy:        strategy,
			DurationMs:      int(time.Since(startTime).Milliseconds()),
		}, nil
	}

	var output *algorithm.ScheduleOutput

	if useRipple || strategy == enum.StrategyRipple {
		output = s.scheduler.Schedule(input)
	} else {
		affectedTaskIDs := batch.AffectedScheduleTaskIDs()
		if len(affectedTaskIDs) == 0 {
			for _, t := range input.Tasks {
				affectedTaskIDs = append(affectedTaskIDs, t.ScheduleTaskID)
			}
		}
		output = s.scheduler.ScheduleIncremental(input, affectedTaskIDs)
	}

	updatedIDs, err := s.applyChanges(ctx, planID, output, taskMap, input.ExistingEvents)
	if err != nil {
		return nil, err
	}

	log.Infof("Schedule completed: plan=%d, tasks=%d, scheduled=%d, unscheduled=%d",
		planID, output.Metrics.TotalTasks, output.Metrics.ScheduledTasks, output.Metrics.UnscheduledTasks)

	return &RescheduleResult{
		Success:         len(output.UnscheduledTasks) == 0,
		UpdatedEventIDs: updatedIDs,
		Strategy:        strategy,
		DurationMs:      int(time.Since(startTime).Milliseconds()),
	}, nil
}

func (s *RescheduleStrategyService) runFullReplan(
	ctx context.Context,
	planID int64,
	batch *entity.RescheduleBatch,
) (*RescheduleResult, error) {
	startTime := time.Now()

	input, taskMap, err := s.loadScheduleData(ctx, planID, batch.UserID)
	if err != nil {
		return nil, err
	}

	input.ExistingEvents = filterPinnedOrCompleted(input.ExistingEvents)

	output := s.scheduler.Schedule(input)

	updatedIDs, err := s.applyChanges(ctx, planID, output, taskMap, nil)
	if err != nil {
		return nil, err
	}

	return &RescheduleResult{
		Success:         len(output.UnscheduledTasks) == 0,
		UpdatedEventIDs: updatedIDs,
		Strategy:        enum.StrategyFullReplan,
		DurationMs:      int(time.Since(startTime).Milliseconds()),
	}, nil
}

func (s *RescheduleStrategyService) loadScheduleData(
	ctx context.Context,
	planID int64,
	userID int64,
) (*algorithm.ScheduleInput, map[int64]*entity.ScheduleTaskEntity, error) {
	tasks, err := s.taskPort.GetBySchedulePlanID(ctx, planID)
	if err != nil {
		return nil, nil, err
	}

	now := time.Now()
	fromMs := now.UnixMilli()
	toMs := now.AddDate(0, 0, DefaultScheduleRangeDays).UnixMilli()

	// Filter tasks using full schedulability criteria with date range
	activeTasks := make([]*entity.ScheduleTaskEntity, 0, len(tasks))
	for _, t := range tasks {
		if t.IsSchedulableForPlan(fromMs, toMs) {
			activeTasks = append(activeTasks, t)
		}
	}

	windows, usingDefaults, err := s.windowService.GetOrCreateWindowsWithInfo(ctx, userID, fromMs, toMs)
	if err != nil {
		return nil, nil, err
	}

	if usingDefaults {
		log.Infof("User %d has no availability configured, using defaults for scheduling", userID)
	}

	events, err := s.eventPort.ListEventsByPlanAndDateRange(ctx, planID, fromMs, toMs)
	if err != nil {
		return nil, nil, err
	}

	// Filter out events of completed tasks - keep them in schedule but don't reschedule
	activeEvents := make([]*entity.ScheduleEventEntity, 0, len(events))
	completedTaskIDs := make(map[int64]bool)
	for _, t := range tasks {
		if t.IsCompleted() {
			completedTaskIDs[t.ID] = true
		}
	}
	for _, e := range events {
		if !completedTaskIDs[e.ScheduleTaskID] {
			activeEvents = append(activeEvents, e)
		}
	}

	taskMap := make(map[int64]*entity.ScheduleTaskEntity)
	for _, t := range activeTasks {
		taskMap[t.ID] = t
	}

	input := s.mapper.BuildScheduleInput(activeTasks, windows, activeEvents)
	return input, taskMap, nil
}

func (s *RescheduleStrategyService) applyChanges(
	ctx context.Context,
	planID int64,
	output *algorithm.ScheduleOutput,
	taskMap map[int64]*entity.ScheduleTaskEntity,
	existingEvents []*algorithm.Assignment,
) ([]int64, error) {
	existingEntities := make([]*entity.ScheduleEventEntity, 0, len(existingEvents))
	for _, a := range existingEvents {
		if a.EventID != nil {
			existingEntities = append(existingEntities, &entity.ScheduleEventEntity{
				BaseEntity: entity.BaseEntity{ID: *a.EventID},
				DateMs:     a.DateMs,
				StartMin:   a.StartMin,
				EndMin:     a.EndMin,
				PartIndex:  a.PartIndex,
				TotalParts: a.TotalParts,
				IsPinned:   a.IsPinned,
			})
		}
	}

	changes := s.mapper.DiffScheduleOutput(output, existingEntities, planID, taskMap)

	var updatedIDs []int64
	var tx *gorm.DB

	// Create new events
	if len(changes.ToCreate) > 0 {
		if err := s.eventPort.CreateBatch(ctx, tx, changes.ToCreate); err != nil {
			return nil, err
		}
		for _, e := range changes.ToCreate {
			updatedIDs = append(updatedIDs, e.ID)
		}
	}

	// Update existing events
	if len(changes.ToUpdate) > 0 {
		if err := s.eventPort.UpdateBatch(ctx, tx, changes.ToUpdate); err != nil {
			return nil, err
		}
		for _, e := range changes.ToUpdate {
			updatedIDs = append(updatedIDs, e.ID)
		}
	}

	// Delete removed events
	for _, id := range changes.ToDelete {
		if err := s.eventPort.DeleteByID(ctx, tx, id); err != nil {
			return nil, err
		}
	}

	// Update task status: mark scheduled tasks as SCHEDULED
	if len(changes.TasksToMarkScheduled) > 0 {
		if err := s.taskPort.UpdateScheduleStatusBatch(ctx, tx, changes.TasksToMarkScheduled, enum.ScheduleTaskScheduled, nil); err != nil {
			log.Warnf("Failed to update scheduled task status: %v", err)
			// Don't fail the whole operation, just log warning
		}
	}

	// Update task status: mark unscheduled tasks as UNSCHEDULABLE with reason
	for scheduleTaskID, reason := range changes.TasksToMarkUnscheduled {
		reasonPtr := &reason
		if err := s.taskPort.UpdateScheduleStatusBatch(ctx, tx, []int64{scheduleTaskID}, enum.ScheduleTaskUnschedulable, reasonPtr); err != nil {
			log.Warnf("Failed to update unscheduled task status for task %d: %v", scheduleTaskID, err)
			// Don't fail the whole operation, just log warning
		}
	}

	return updatedIDs, nil
}

func filterPinnedOrCompleted(events []*algorithm.Assignment) []*algorithm.Assignment {
	result := make([]*algorithm.Assignment, 0)
	for _, e := range events {
		if e.IsPinned || e.IsCompleted() {
			result = append(result, e)
		}
	}
	return result
}

// runOptimalReplan uses external ptm_optimization service for deep optimization
func (s *RescheduleStrategyService) runOptimalReplan(
	ctx context.Context,
	planID int64,
	batch *entity.RescheduleBatch,
) (*RescheduleResult, error) {
	startTime := time.Now()

	// Check if optimization client is available
	if s.optimizationClient == nil {
		log.Warn(ctx, "Optimization client not configured, falling back to local scheduler")
		return s.runFullReplan(ctx, planID, batch)
	}

	// Get schedule date range
	now := time.Now()
	fromMs := now.UnixMilli()
	toMs := now.AddDate(0, 0, DefaultScheduleRangeDays).UnixMilli()

	// Load and filter tasks
	tasks, err := s.taskPort.GetBySchedulePlanID(ctx, planID)
	if err != nil {
		return nil, err
	}

	activeTasks := make([]*entity.ScheduleTaskEntity, 0, len(tasks))
	for _, t := range tasks {
		if t.IsSchedulableForPlan(fromMs, toMs) {
			activeTasks = append(activeTasks, t)
		}
	}

	if len(activeTasks) == 0 {
		return &RescheduleResult{
			Success:         true,
			UpdatedEventIDs: []int64{},
			Strategy:        enum.StrategyOptimalReplan,
			DurationMs:      int(time.Since(startTime).Milliseconds()),
		}, nil
	}

	windows, _, err := s.windowService.GetOrCreateWindowsWithInfo(ctx, batch.UserID, fromMs, toMs)
	if err != nil {
		return nil, err
	}

	// Build optimization request
	optRequest := s.mapper.BuildOptimizationRequest(activeTasks, windows)

	// Call optimization service with fallback
	log.Infof("Calling ptm_optimization for plan %d with %d tasks and %d windows",
		planID, len(optRequest.Tasks), len(optRequest.Windows))

	optResult, err := s.optimizationClient.OptimizeWithFallback(ctx, optRequest)
	if err != nil {
		log.Warnf("Optimization service failed, falling back to local scheduler: %v", err)
		return s.runFullReplan(ctx, planID, batch)
	}

	// Build task map: ScheduleTaskID -> ScheduleTaskEntity
	// We send ScheduleTaskID as taskId to ptm_optimization, so response uses same ID
	taskMap := make(map[int64]*entity.ScheduleTaskEntity)
	for _, t := range activeTasks {
		taskMap[t.ID] = t
	}

	// Convert optimization result to local format
	output := s.mapper.OptimizationResultToScheduleOutput(optResult, taskMap)

	// Get existing events
	events, err := s.eventPort.ListEventsByPlanAndDateRange(ctx, planID, fromMs, toMs)
	if err != nil {
		return nil, err
	}

	// Convert to entity slice for comparison
	existingEntities := make([]*entity.ScheduleEventEntity, 0, len(events))
	for _, e := range events {
		existingEntities = append(existingEntities, e)
	}

	// Apply changes (taskMap uses ScheduleTaskID as key)
	changes := s.mapper.DiffScheduleOutput(output, existingEntities, planID, taskMap)

	var updatedIDs []int64
	var tx *gorm.DB

	// Create new events
	if len(changes.ToCreate) > 0 {
		if err := s.eventPort.CreateBatch(ctx, tx, changes.ToCreate); err != nil {
			return nil, err
		}
		for _, e := range changes.ToCreate {
			updatedIDs = append(updatedIDs, e.ID)
		}
	}

	// Update existing events
	if len(changes.ToUpdate) > 0 {
		if err := s.eventPort.UpdateBatch(ctx, tx, changes.ToUpdate); err != nil {
			return nil, err
		}
		for _, e := range changes.ToUpdate {
			updatedIDs = append(updatedIDs, e.ID)
		}
	}

	// Delete removed events
	for _, id := range changes.ToDelete {
		if err := s.eventPort.DeleteByID(ctx, tx, id); err != nil {
			return nil, err
		}
	}

	// Update task status: mark scheduled tasks as SCHEDULED
	if len(changes.TasksToMarkScheduled) > 0 {
		if err := s.taskPort.UpdateScheduleStatusBatch(ctx, tx, changes.TasksToMarkScheduled, enum.ScheduleTaskScheduled, nil); err != nil {
			log.Warnf("Failed to update scheduled task status: %v", err)
		}
	}

	// Update task status: mark unscheduled tasks as UNSCHEDULABLE with reason
	for scheduleTaskID, reason := range changes.TasksToMarkUnscheduled {
		reasonPtr := &reason
		if err := s.taskPort.UpdateScheduleStatusBatch(ctx, tx, []int64{scheduleTaskID}, enum.ScheduleTaskUnschedulable, reasonPtr); err != nil {
			log.Warnf("Failed to update unscheduled task status for task %d: %v", scheduleTaskID, err)
		}
	}

	durationMs := int(time.Since(startTime).Milliseconds())

	log.Infof("Optimal replan completed: plan=%d, tasks=%d, scheduled=%d, unscheduled=%d, duration=%dms",
		planID, len(activeTasks), output.Metrics.ScheduledTasks, output.Metrics.UnscheduledTasks, durationMs)

	return &RescheduleResult{
		Success:         len(output.UnscheduledTasks) == 0,
		UpdatedEventIDs: updatedIDs,
		Strategy:        enum.StrategyOptimalReplan,
		DurationMs:      durationMs,
	}, nil
}

// RunDeepOptimize uses ptm_optimization with a specific algorithm strategy (no fallback)
func (s *RescheduleStrategyService) RunDeepOptimize(
	ctx context.Context,
	planID int64,
	batch *entity.RescheduleBatch,
	strategy optimization.StrategyType,
	maxTimeSec *int,
) (*RescheduleResult, error) {
	startTime := time.Now()

	if s.optimizationClient == nil {
		log.Warn(ctx, "Optimization client not configured, falling back to local scheduler")
		return s.runFullReplan(ctx, planID, batch)
	}

	now := time.Now()
	fromMs := now.UnixMilli()
	toMs := now.AddDate(0, 0, DefaultScheduleRangeDays).UnixMilli()

	tasks, err := s.taskPort.GetBySchedulePlanID(ctx, planID)
	if err != nil {
		return nil, err
	}

	activeTasks := make([]*entity.ScheduleTaskEntity, 0, len(tasks))
	for _, t := range tasks {
		if t.IsSchedulableForPlan(fromMs, toMs) {
			activeTasks = append(activeTasks, t)
		}
	}

	if len(activeTasks) == 0 {
		return &RescheduleResult{
			Success:         true,
			UpdatedEventIDs: []int64{},
			Strategy:        enum.StrategyOptimalReplan,
			DurationMs:      int(time.Since(startTime).Milliseconds()),
		}, nil
	}

	windows, _, err := s.windowService.GetOrCreateWindowsWithInfo(ctx, batch.UserID, fromMs, toMs)
	if err != nil {
		return nil, err
	}

	optRequest := s.mapper.BuildOptimizationRequest(activeTasks, windows)

	if maxTimeSec != nil {
		optRequest.Params.MaxTimeSec = maxTimeSec
	}

	log.Infof("Calling ptm_optimization (strategy=%s) for plan %d with %d tasks and %d windows",
		strategy, planID, len(optRequest.Tasks), len(optRequest.Windows))

	optResult, err := s.optimizationClient.Optimize(ctx, optRequest, strategy)
	if err != nil {
		return &RescheduleResult{
			Success:         false,
			UpdatedEventIDs: []int64{},
			Strategy:        enum.StrategyOptimalReplan,
			DurationMs:      int(time.Since(startTime).Milliseconds()),
			Error:           err,
		}, err
	}

	return s.applyOptimizationResult(ctx, planID, activeTasks, optResult, startTime)
}

// RunFallbackChainOptimize uses ptm_optimization with fallback chain
func (s *RescheduleStrategyService) RunFallbackChainOptimize(
	ctx context.Context,
	planID int64,
	batch *entity.RescheduleBatch,
	maxTimeSec *int,
) (*RescheduleResult, error) {
	startTime := time.Now()

	if s.optimizationClient == nil {
		log.Warn(ctx, "Optimization client not configured, falling back to local scheduler")
		return s.runFullReplan(ctx, planID, batch)
	}

	now := time.Now()
	fromMs := now.UnixMilli()
	toMs := now.AddDate(0, 0, DefaultScheduleRangeDays).UnixMilli()

	tasks, err := s.taskPort.GetBySchedulePlanID(ctx, planID)
	if err != nil {
		return nil, err
	}

	activeTasks := make([]*entity.ScheduleTaskEntity, 0, len(tasks))
	for _, t := range tasks {
		if t.IsSchedulableForPlan(fromMs, toMs) {
			activeTasks = append(activeTasks, t)
		}
	}

	if len(activeTasks) == 0 {
		return &RescheduleResult{
			Success:         true,
			UpdatedEventIDs: []int64{},
			Strategy:        enum.StrategyOptimalReplan,
			DurationMs:      int(time.Since(startTime).Milliseconds()),
		}, nil
	}

	windows, _, err := s.windowService.GetOrCreateWindowsWithInfo(ctx, batch.UserID, fromMs, toMs)
	if err != nil {
		return nil, err
	}

	optRequest := s.mapper.BuildOptimizationRequest(activeTasks, windows)

	if maxTimeSec != nil {
		optRequest.Params.MaxTimeSec = maxTimeSec
	}

	log.Infof("Calling ptm_optimization (fallback chain) for plan %d with %d tasks and %d windows",
		planID, len(optRequest.Tasks), len(optRequest.Windows))

	optResult, err := s.optimizationClient.OptimizeWithFallback(ctx, optRequest)
	if err != nil {
		log.Warnf("Fallback chain optimization failed, falling back to local scheduler: %v", err)
		return s.runFullReplan(ctx, planID, batch)
	}

	return s.applyOptimizationResult(ctx, planID, activeTasks, optResult, startTime)
}

// applyOptimizationResult applies the optimization result to the schedule
func (s *RescheduleStrategyService) applyOptimizationResult(
	ctx context.Context,
	planID int64,
	activeTasks []*entity.ScheduleTaskEntity,
	optResult *optimization.PlanResult,
	startTime time.Time,
) (*RescheduleResult, error) {
	taskMap := make(map[int64]*entity.ScheduleTaskEntity)
	for _, t := range activeTasks {
		taskMap[t.ID] = t
	}

	output := s.mapper.OptimizationResultToScheduleOutput(optResult, taskMap)

	// Delete only PLANNED events from today onwards
	now := time.Now()
	fromDateMs := now.UnixMilli()
	var tx *gorm.DB
	if err := s.eventPort.DeletePlannedEventsFromDate(ctx, tx, planID, fromDateMs); err != nil {
		return nil, err
	}

	var updatedIDs []int64
	newEvents := s.mapper.ScheduleOutputToEvents(output, planID, taskMap)

	if len(newEvents) > 0 {
		if err := s.eventPort.CreateBatch(ctx, tx, newEvents); err != nil {
			return nil, err
		}
		for _, e := range newEvents {
			updatedIDs = append(updatedIDs, e.ID)
		}
	}

	// Update task status based on optimization result
	scheduledTaskIDs := make([]int64, 0)
	for _, assignment := range output.Assignments {
		scheduledTaskIDs = append(scheduledTaskIDs, assignment.ScheduleTaskID)
	}

	if len(scheduledTaskIDs) > 0 {
		if err := s.taskPort.UpdateScheduleStatusBatch(ctx, tx, scheduledTaskIDs, enum.ScheduleTaskScheduled, nil); err != nil {
			log.Warnf("Failed to update scheduled task status: %v", err)
		}
	}

	for _, unscheduled := range output.UnscheduledTasks {
		reason := unscheduled.Reason
		if err := s.taskPort.UpdateScheduleStatusBatch(ctx, tx, []int64{unscheduled.ScheduleTaskID}, enum.ScheduleTaskUnschedulable, &reason); err != nil {
			log.Warnf("Failed to update unscheduled task status for task %d: %v", unscheduled.ScheduleTaskID, err)
		}
	}

	durationMs := int(time.Since(startTime).Milliseconds())

	log.Infof("Optimization completed: plan=%d, tasks=%d, scheduled=%d, unscheduled=%d, duration=%dms",
		planID, len(activeTasks), output.Metrics.ScheduledTasks, output.Metrics.UnscheduledTasks, durationMs)

	return &RescheduleResult{
		Success:         len(output.UnscheduledTasks) == 0,
		UpdatedEventIDs: updatedIDs,
		Strategy:        enum.StrategyOptimalReplan,
		DurationMs:      durationMs,
	}, nil
}
