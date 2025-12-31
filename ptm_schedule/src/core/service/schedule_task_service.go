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
	UpdateConstraints(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, updates *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)

	CloneTasksForPlan(ctx context.Context, tx *gorm.DB, srcPlanID, destPlanID int64) (map[int64]int64, error)
	Update(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, task *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	DeleteByPlanID(ctx context.Context, tx *gorm.DB, planID int64) error

	GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error)
	GetByScheduleTaskID(ctx context.Context, scheduleTaskID int64) (*entity.ScheduleTaskEntity, error)
	GetByScheduleTaskIDs(ctx context.Context, scheduleTaskIDs []int64) ([]*entity.ScheduleTaskEntity, error)
	GetByPlanIDAndTaskID(ctx context.Context, planID, taskID int64) (*entity.ScheduleTaskEntity, error)
	ListScheduleTasks(ctx context.Context, filter *port.ScheduleTaskFilter) ([]*entity.ScheduleTaskEntity, int64, error)
}

type ScheduleTaskService struct {
	scheduleTaskPort  port.IScheduleTaskPort
	scheduleEventPort port.IScheduleEventPort
}

func NewScheduleTaskService(
	scheduleTaskPort port.IScheduleTaskPort,
	scheduleEventPort port.IScheduleEventPort,
) IScheduleTaskService {
	return &ScheduleTaskService{
		scheduleTaskPort:  scheduleTaskPort,
		scheduleEventPort: scheduleEventPort,
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

		ParentTaskID:          event.ParentTaskID,
		HasSubtasks:           event.HasSubtasks,
		TotalSubtaskCount:     event.TotalSubtaskCount,
		CompletedSubtaskCount: event.CompletedSubtaskCount,

		AllowSplit:          true,
		MinSplitDurationMin: 30,
		MaxSplitCount:       4,

		ScheduleStatus: func() enum.ScheduleTaskStatus {
			if event.HasSubtasks {
				return enum.ScheduleTaskExcluded
			}
			return enum.ScheduleTaskPending
		}(),
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

	hasMetadataChange := currentTask.ApplyMedataChanges(event.Title, event.Category)
	hasConstraintChange := currentTask.ApplyConstraintChanges(
		event.EstimatedDurationMin,
		event.Priority,
		event.DeadlineMs,
		event.EarliestStartMs,
		event.PreferredStartDateMs,
		event.IsDeepWork,
	)

	hasSubtaskChange := false
	if event.HasSubtasks != nil && *event.HasSubtasks != currentTask.HasSubtasks {
		hasSubtaskChange = true
		if *event.HasSubtasks {
			currentTask.ScheduleStatus = enum.ScheduleTaskExcluded
			s.scheduleEventPort.DeleteByScheduleTaskID(ctx, tx, currentTask.ID)
			log.Info(ctx, "Task now has subtasks, excluded from scheduling. PlanID: ", planID, ", TaskID: ", event.TaskID)
		} else {
			currentTask.ScheduleStatus = enum.ScheduleTaskPending
			log.Info(ctx, "Task no longer has subtasks, included in scheduling. PlanID: ", planID, ", TaskID: ", event.TaskID)
		}
	}
	currentTask.ApplySubtaskInfoChanges(
		event.ParentTaskID,
		event.HasSubtasks,
		event.TotalSubtaskCount,
		event.CompletedSubtaskCount,
	)

	newHash := currentTask.CalculateSnapshotHash()
	hasSnapshotChange := currentTask.HasChanged(newHash)

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

	if !hasMetadataChange && !hasConstraintChange && !hasSubtaskChange {
		return enum.ChangeNone, nil
	}

	if hasConstraintChange {
		log.Info(ctx, "Schedule task constraint changed. PlanID: ", planID, ", TaskID: ", event.TaskID)
		return enum.ChangeConstraint, nil
	}
	if hasSubtaskChange {
		log.Info(ctx, "Schedule task subtask info changed. PlanID: ", planID, ", TaskID: ", event.TaskID)
		return enum.ChangeStructural, nil
	}

	log.Info(ctx, "Schedule task metadata updated. PlanID: ", planID, ", TaskID: ", event.TaskID)
	return enum.ChangeMetadata, nil
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

	if err := s.scheduleEventPort.DeleteByScheduleTaskID(ctx, tx, currentTask.ID); err != nil {
		log.Error(ctx, "Failed to delete associated events for task: ", currentTask.ID, ", error: ", err)
		return err
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

func (s *ScheduleTaskService) GetByScheduleTaskID(ctx context.Context, scheduleTaskID int64) (*entity.ScheduleTaskEntity, error) {
	scheduleTask, err := s.scheduleTaskPort.GetScheduleTaskByID(ctx, scheduleTaskID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule task by ID: ", err)
		return nil, err
	}
	if scheduleTask == nil {
		return nil, fmt.Errorf(constant.ScheduleTaskNotFound)
	}
	return scheduleTask, nil
}

func (s *ScheduleTaskService) GetByScheduleTaskIDs(ctx context.Context, scheduleTaskIDs []int64) ([]*entity.ScheduleTaskEntity, error) {
	scheduleTasks, err := s.scheduleTaskPort.GetScheduleTasksByIDs(ctx, scheduleTaskIDs)
	if err != nil {
		log.Error(ctx, "Failed to get schedule tasks by IDs: ", err)
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

func (s *ScheduleTaskService) ListScheduleTasks(ctx context.Context, filter *port.ScheduleTaskFilter) ([]*entity.ScheduleTaskEntity, int64, error) {
	scheduleTasks, totalCount, err := s.scheduleTaskPort.ListScheduleTasks(ctx, filter)
	if err != nil {
		log.Error(ctx, "Failed to list schedule tasks: ", err)
		return nil, 0, err
	}
	return scheduleTasks, totalCount, nil
}

// Returns a map of old schedule_task_id -> new schedule_task_id
func (s *ScheduleTaskService) CloneTasksForPlan(ctx context.Context, tx *gorm.DB, srcPlanID, destPlanID int64) (map[int64]int64, error) {
	srcTasks, err := s.scheduleTaskPort.GetBySchedulePlanID(ctx, srcPlanID)
	if err != nil {
		return nil, err
	}

	if len(srcTasks) == 0 {
		return make(map[int64]int64), nil
	}

	clonedTasks := make([]*entity.ScheduleTaskEntity, 0, len(srcTasks))
	for _, t := range srcTasks {
		clone := t.Clone()
		clone.SchedulePlanID = destPlanID
		clonedTasks = append(clonedTasks, clone)
	}

	if err := s.scheduleTaskPort.CreateBatch(ctx, tx, clonedTasks); err != nil {
		return nil, err
	}

	taskIDMapping := make(map[int64]int64) // old schedule_task_id -> new schedule_task_id
	newTasks, err := s.scheduleTaskPort.GetBySchedulePlanID(ctx, destPlanID)
	if err != nil {
		return nil, err
	}

	taskIDToNewID := make(map[int64]int64)
	for _, t := range newTasks {
		taskIDToNewID[t.TaskID] = t.ID
	}

	for _, srcTask := range srcTasks {
		if newID, ok := taskIDToNewID[srcTask.TaskID]; ok {
			taskIDMapping[srcTask.ID] = newID
		}
	}

	log.Info(ctx, "Cloned ", len(clonedTasks), " tasks from plan ", srcPlanID, " to plan ", destPlanID)
	return taskIDMapping, nil
}

func (s *ScheduleTaskService) DeleteByPlanID(ctx context.Context, tx *gorm.DB, planID int64) error {
	return s.scheduleTaskPort.DeleteByPlanID(ctx, tx, planID)
}

func (s *ScheduleTaskService) Update(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, task *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	return s.scheduleTaskPort.UpdateScheduleTask(ctx, tx, scheduleTaskID, task)
}

func (s *ScheduleTaskService) UpdateConstraints(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, updates *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	currentTask, err := s.scheduleTaskPort.GetScheduleTaskByID(ctx, scheduleTaskID)
	if err != nil {
		return nil, err
	}
	if currentTask == nil {
		return nil, fmt.Errorf("schedule task not found: %d", scheduleTaskID)
	}

	hasChange := false
	nowMs := time.Now().UnixMilli()

	// Update scheduling constraints
	if updates.DurationMin > 0 && updates.DurationMin != currentTask.DurationMin {
		currentTask.DurationMin = updates.DurationMin
		hasChange = true
	}
	if updates.Priority.IsValid() && updates.Priority != currentTask.Priority {
		currentTask.Priority = updates.Priority
		hasChange = true
	}
	if updates.DeadlineMs != nil && (currentTask.DeadlineMs == nil || *updates.DeadlineMs != *currentTask.DeadlineMs) {
		currentTask.DeadlineMs = updates.DeadlineMs
		hasChange = true
	}
	if updates.EarliestStartMs != nil && (currentTask.EarliestStartMs == nil || *updates.EarliestStartMs != *currentTask.EarliestStartMs) {
		currentTask.EarliestStartMs = updates.EarliestStartMs
		hasChange = true
	}
	if updates.PreferredStartMs != nil && (currentTask.PreferredStartMs == nil || *updates.PreferredStartMs != *currentTask.PreferredStartMs) {
		currentTask.PreferredStartMs = updates.PreferredStartMs
		hasChange = true
	}
	if updates.IsDeepWork != currentTask.IsDeepWork {
		currentTask.IsDeepWork = updates.IsDeepWork
		hasChange = true
	}

	// Update split settings
	if updates.AllowSplit != currentTask.AllowSplit {
		currentTask.AllowSplit = updates.AllowSplit
		hasChange = true
	}
	if updates.MinSplitDurationMin > 0 && updates.MinSplitDurationMin != currentTask.MinSplitDurationMin {
		currentTask.MinSplitDurationMin = updates.MinSplitDurationMin
		hasChange = true
	}
	if updates.MaxSplitCount > 0 && updates.MaxSplitCount != currentTask.MaxSplitCount {
		currentTask.MaxSplitCount = updates.MaxSplitCount
		hasChange = true
	}

	// Update buffers
	if updates.BufferBeforeMin != currentTask.BufferBeforeMin {
		currentTask.BufferBeforeMin = updates.BufferBeforeMin
		hasChange = true
	}
	if updates.BufferAfterMin != currentTask.BufferAfterMin {
		currentTask.BufferAfterMin = updates.BufferAfterMin
		hasChange = true
	}

	if !hasChange {
		return currentTask, nil
	}

	// Recalculate priority score and mark as pending for rescheduling
	currentTask.RecalculatePriorityScore(nowMs)
	currentTask.ScheduleStatus = enum.ScheduleTaskPending
	currentTask.UpdatedAt = nowMs

	updated, err := s.scheduleTaskPort.UpdateScheduleTask(ctx, tx, scheduleTaskID, currentTask)
	if err != nil {
		log.Error(ctx, "Failed to update schedule task constraints: ", err)
		return nil, err
	}

	log.Info(ctx, "Updated schedule task constraints. ScheduleTaskID: ", scheduleTaskID)
	return updated, nil
}
