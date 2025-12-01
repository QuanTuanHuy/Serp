/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"fmt"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type IScheduleTaskService interface {
	CreateSnapshot(ctx context.Context, tx *gorm.DB, planID int64, event *message.TaskCreatedEvent) (*entity.ScheduleTaskEntity, error)
	SyncSnapshot(ctx context.Context, tx *gorm.DB, planID int64, event *message.TaskUpdatedEvent) (enum.ChangeType, error)
	DeleteSnapshot(ctx context.Context, tx *gorm.DB, planID, taskID int64) error

	GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error)
	GetByPlanIDAndTaskID(ctx context.Context, planID, taskID int64) (*entity.ScheduleTaskEntity, error)
}

type ScheduleTaskService struct {
	scheduleTaskPort port.IScheduleTaskPort
}

func NewScheduleTaskService(scheduleTaskPort port.IScheduleTaskPort) IScheduleTaskService {
	return &ScheduleTaskService{
		scheduleTaskPort: scheduleTaskPort,
	}
}

func (s *ScheduleTaskService) CreateSnapshot(ctx context.Context, tx *gorm.DB, planID int64, event *message.TaskCreatedEvent) (*entity.ScheduleTaskEntity, error) {
	nowMs := time.Now().UnixMilli()

	priority := enum.Priority(event.Priority)
	if !priority.IsValid() {
		priority = enum.PriorityMedium
	}

	durationMin := 60
	if event.EstimatedDurationMin != nil {
		durationMin = *event.EstimatedDurationMin
	}

	newTask := &entity.ScheduleTaskEntity{
		UserID:         event.UserID,
		TenantID:       event.TenantID,
		SchedulePlanID: planID,
		TaskID:         event.TaskID,
		Title:          event.Title,
		DurationMin:    durationMin,
		Priority:       priority,
		Category:       event.Category,
		IsDeepWork:     event.IsDeepWork,

		EarliestStartMs:  event.EarliestStartMs,
		DeadlineMs:       event.DeadlineMs,
		PreferredStartMs: event.PreferredStartDateMs,

		AllowSplit:          true,
		MinSplitDurationMin: 30,
		MaxSplitCount:       4,

		ScheduleStatus: enum.ScheduleTaskPending,
	}

	newTask.TaskSnapshotHash = newTask.CalculateSnapshotHash()
	newTask.RecalculatePriorityScore(nowMs)

	created, err := s.scheduleTaskPort.CreateScheduleTask(ctx, tx, newTask)
	if err != nil {
		log.Error(ctx, "Failed to create schedule task snapshot: ", err)
		return nil, err
	}

	log.Info(ctx, "Created schedule task snapshot. PlanID: ", planID, ", TaskID: ", event.TaskID)
	return created, nil
}

func (s *ScheduleTaskService) SyncSnapshot(ctx context.Context, tx *gorm.DB, planID int64, event *message.TaskUpdatedEvent) (enum.ChangeType, error) {
	currentTask, err := s.scheduleTaskPort.GetByPlanIDAndTaskID(ctx, planID, event.TaskID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule task by plan and task ID: ", err)
		return enum.ChangeNone, err
	}
	if currentTask == nil {
		log.Warn(ctx, "Schedule task not found for sync. PlanID: ", planID, ", TaskID: ", event.TaskID)
		return enum.ChangeNone, nil
	}

	hasMetadataChange := s.applyMetadataChanges(currentTask, event)
	hasConstraintChange := s.applyConstraintChanges(currentTask, event)

	newHash := currentTask.CalculateSnapshotHash()
	hasSnapshotChange := newHash != currentTask.TaskSnapshotHash

	if hasSnapshotChange {
		currentTask.TaskSnapshotHash = newHash
		currentTask.UpdatedAt = time.Now().UnixMilli()
		if hasConstraintChange {
			currentTask.ScheduleStatus = enum.ScheduleTaskPending
			currentTask.RecalculatePriorityScore(time.Now().UnixMilli())
		}
		_, err = s.scheduleTaskPort.UpdateScheduleTask(ctx, tx, currentTask.ID, currentTask)
		if err != nil {
			log.Error(ctx, "Failed to update schedule task snapshot: ", err)
			return enum.ChangeNone, err
		}
	}

	if !hasMetadataChange && !hasConstraintChange {
		return enum.ChangeNone, nil
	}

	if hasConstraintChange {
		log.Info(ctx, "Schedule task constraint changed. PlanID: ", planID, ", TaskID: ", event.TaskID)
		return enum.ChangeConstraint, nil
	}

	log.Info(ctx, "Schedule task metadata updated. PlanID: ", planID, ", TaskID: ", event.TaskID)
	return enum.ChangeMetadata, nil
}

func (s *ScheduleTaskService) applyMetadataChanges(task *entity.ScheduleTaskEntity, event *message.TaskUpdatedEvent) bool {
	changed := false

	if event.Title != nil && *event.Title != task.Title {
		task.Title = *event.Title
		changed = true
	}
	if event.Category != nil {
		task.Category = event.Category
		changed = true
	}
	if event.IsDeepWork != nil && *event.IsDeepWork != task.IsDeepWork {
		task.IsDeepWork = *event.IsDeepWork
		changed = true
	}

	return changed
}

func (s *ScheduleTaskService) applyConstraintChanges(task *entity.ScheduleTaskEntity, event *message.TaskUpdatedEvent) bool {
	changed := false

	if event.EstimatedDurationMin != nil && *event.EstimatedDurationMin != task.DurationMin {
		task.DurationMin = *event.EstimatedDurationMin
		changed = true
	}
	if event.Priority != nil {
		newPriority := enum.Priority(*event.Priority)
		if newPriority.IsValid() && newPriority != task.Priority {
			task.Priority = newPriority
			changed = true
		}
	}
	if event.DeadlineMs != nil {
		task.DeadlineMs = event.DeadlineMs
		changed = true
	}
	if event.EarliestStartMs != nil {
		task.EarliestStartMs = event.EarliestStartMs
		changed = true
	}
	if event.PreferredStartDateMs != nil {
		task.PreferredStartMs = event.PreferredStartDateMs
		changed = true
	}

	return changed
}

func (s *ScheduleTaskService) DeleteSnapshot(ctx context.Context, tx *gorm.DB, planID, taskID int64) error {
	currentTask, err := s.scheduleTaskPort.GetByPlanIDAndTaskID(ctx, planID, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule task for deletion: ", err)
		return err
	}
	if currentTask == nil {
		log.Warn(ctx, "Schedule task not found for deletion. PlanID: ", planID, ", TaskID: ", taskID)
		return nil
	}

	if err := s.scheduleTaskPort.DeleteScheduleTask(ctx, tx, currentTask.ID); err != nil {
		log.Error(ctx, "Failed to delete schedule task: ", err)
		return err
	}

	log.Info(ctx, "Deleted schedule task snapshot. PlanID: ", planID, ", TaskID: ", taskID)
	return nil
}

func (s *ScheduleTaskService) GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error) {
	scheduleTasks, err := s.scheduleTaskPort.GetBySchedulePlanID(ctx, schedulePlanID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule tasks by schedule plan ID: ", err)
		return nil, err
	}
	return scheduleTasks, nil
}

func (s *ScheduleTaskService) GetByPlanIDAndTaskID(ctx context.Context, planID, taskID int64) (*entity.ScheduleTaskEntity, error) {
	scheduleTask, err := s.scheduleTaskPort.GetByPlanIDAndTaskID(ctx, planID, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule task by plan ID and task ID: ", err)
		return nil, err
	}
	if scheduleTask == nil {
		return nil, fmt.Errorf(constant.ScheduleTaskNotFound)
	}
	return scheduleTask, nil
}
