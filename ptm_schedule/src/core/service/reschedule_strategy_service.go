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
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
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
}

type RescheduleStrategyService struct {
	taskPort      storePort.IScheduleTaskPort
	eventPort     storePort.IScheduleEventPort
	windowService IScheduleWindowService
	scheduler     *algorithm.HybridScheduler
	mapper        *algorithm.AlgorithmMapper
}

func NewRescheduleStrategyService(
	taskPort storePort.IScheduleTaskPort,
	eventPort storePort.IScheduleEventPort,
	windowService IScheduleWindowService,
) IRescheduleStrategyService {
	return &RescheduleStrategyService{
		taskPort:      taskPort,
		eventPort:     eventPort,
		windowService: windowService,
		scheduler:     algorithm.NewHybridScheduler(),
		mapper:        algorithm.NewAlgorithmMapper(),
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

	input.ExistingEvents = filterPinnedOnly(input.ExistingEvents)

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

	activeTasks := make([]*entity.ScheduleTaskEntity, 0, len(tasks))
	for _, t := range tasks {
		if !t.IsCompleted() {
			activeTasks = append(activeTasks, t)
		}
	}

	now := time.Now()
	fromMs := now.UnixMilli()
	toMs := now.AddDate(0, 0, DefaultScheduleRangeDays).UnixMilli()

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

	if len(changes.ToCreate) > 0 {
		if err := s.eventPort.CreateBatch(ctx, tx, changes.ToCreate); err != nil {
			return nil, err
		}
		for _, e := range changes.ToCreate {
			updatedIDs = append(updatedIDs, e.ID)
		}
	}

	if len(changes.ToUpdate) > 0 {
		if err := s.eventPort.UpdateBatch(ctx, tx, changes.ToUpdate); err != nil {
			return nil, err
		}
		for _, e := range changes.ToUpdate {
			updatedIDs = append(updatedIDs, e.ID)
		}
	}

	for _, id := range changes.ToDelete {
		if err := s.eventPort.DeleteByID(ctx, tx, id); err != nil {
			return nil, err
		}
	}

	return updatedIDs, nil
}

func filterPinnedOnly(events []*algorithm.Assignment) []*algorithm.Assignment {
	result := make([]*algorithm.Assignment, 0)
	for _, e := range events {
		if e.IsPinned {
			result = append(result, e)
		}
	}
	return result
}
