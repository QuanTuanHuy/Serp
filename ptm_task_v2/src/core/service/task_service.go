/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"time"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type ITaskService interface {
	// Validation
	ValidateTaskData(task *entity.TaskEntity) error
	ValidateTaskOwnership(userID int64, task *entity.TaskEntity) error
	ValidateTaskStatus(currentStatus, newStatus string) error

	// Business rules
	CalculatePriorityScore(task *entity.TaskEntity, currentTimeMs int64) float64
	CheckIfOverdue(task *entity.TaskEntity, currentTimeMs int64) bool
	CheckIfCanBeScheduled(task *entity.TaskEntity) bool

	// Task operations
	CreateTask(ctx context.Context, tx *gorm.DB, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error)
	UpdateTask(ctx context.Context, tx *gorm.DB, userID int64, task *entity.TaskEntity) error
	UpdateTaskStatus(ctx context.Context, tx *gorm.DB, userID int64, taskID int64, status string) error
	CompleteTask(ctx context.Context, tx *gorm.DB, userID int64, taskID int64, actualDurationMin int, quality int) error
	DeleteTask(ctx context.Context, tx *gorm.DB, userID int64, taskID int64) error

	// Query operations
	GetTaskByID(ctx context.Context, taskID int64) (*entity.TaskEntity, error)
	GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error)
	GetOverdueTasks(ctx context.Context, userID int64, currentTimeMs int64) ([]*entity.TaskEntity, error)
	GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)
}

type taskService struct {
	taskPort store.ITaskPort
}

func NewTaskService(taskPort store.ITaskPort) ITaskService {
	return &taskService{
		taskPort: taskPort,
	}
}

func (s *taskService) ValidateTaskData(task *entity.TaskEntity) error {
	if task.Title == "" {
		return errors.New(constant.TaskTitleRequired)
	}
	if len(task.Title) > 500 {
		return errors.New(constant.TaskTitleTooLong)
	}
	if !enum.TaskPriority(task.Priority).IsValid() {
		return errors.New(constant.InvalidTaskPriority)
	}
	if !enum.TaskStatus(task.Status).IsValid() {
		return errors.New(constant.InvalidTaskStatus)
	}
	if task.DeadlineMs != nil && task.EarliestStartMs != nil {
		if *task.DeadlineMs < *task.EarliestStartMs {
			return errors.New(constant.InvalidDeadline)
		}
	}
	if task.EstimatedDurationMin != nil && *task.EstimatedDurationMin <= 0 {
		return errors.New(constant.InvalidDuration)
	}
	if task.RecurrencePattern != nil && !enum.RecurrencePattern(*task.RecurrencePattern).IsValid() {
		return errors.New(constant.InvalidRecurrencePattern)
	}
	return nil
}

func (s *taskService) ValidateTaskOwnership(userID int64, task *entity.TaskEntity) error {
	if task.UserID != userID {
		return errors.New(constant.UpdateTaskForbidden)
	}
	return nil
}

func (s *taskService) ValidateTaskStatus(currentStatus, newStatus string) error {
	current := enum.TaskStatus(currentStatus)
	if !current.IsValid() {
		return errors.New(constant.InvalidTaskStatus)
	}
	new := enum.TaskStatus(newStatus)
	if !new.IsValid() {
		return errors.New(constant.InvalidTaskStatus)
	}
	if !current.CanTransitionTo(new) {
		return errors.New(constant.InvalidStatusTransition)
	}
	return nil
}

func (s *taskService) CalculatePriorityScore(task *entity.TaskEntity, currentTimeMs int64) float64 {
	return task.GetPriorityScore()
}

func (s *taskService) CheckIfOverdue(task *entity.TaskEntity, currentTimeMs int64) bool {
	return task.IsOverdue(currentTimeMs)
}

func (s *taskService) CheckIfCanBeScheduled(task *entity.TaskEntity) bool {
	return task.CanBeScheduled(time.Now().UnixMilli())
}

func (s *taskService) CreateTask(ctx context.Context, tx *gorm.DB, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	task.UserID = userID
	now := time.Now().UnixMilli()
	task.CreatedAt = now
	task.UpdatedAt = now
	if task.Status == "" {
		task.Status = string(enum.StatusTodo)
	}
	if task.ActiveStatus == "" {
		task.ActiveStatus = string(enum.Active)
	}
	if task.RecurrencePattern == nil {
		none := string(enum.RecurrenceNone)
		task.RecurrencePattern = &none
	}
	score := s.CalculatePriorityScore(task, now)
	task.PriorityScore = &score
	if err := s.ValidateTaskData(task); err != nil {
		return nil, err
	}
	if err := s.taskPort.CreateTask(ctx, tx, task); err != nil {
		return nil, err
	}
	return task, nil
}

func (s *taskService) UpdateTask(ctx context.Context, tx *gorm.DB, userID int64, task *entity.TaskEntity) error {
	if err := s.ValidateTaskOwnership(userID, task); err != nil {
		return err
	}
	if err := s.ValidateTaskData(task); err != nil {
		return err
	}
	now := time.Now().UnixMilli()
	task.UpdatedAt = now
	score := s.CalculatePriorityScore(task, now)
	task.PriorityScore = &score
	return s.taskPort.UpdateTask(ctx, tx, task)
}

func (s *taskService) UpdateTaskStatus(ctx context.Context, tx *gorm.DB, userID int64, taskID int64, status string) error {
	task, err := s.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	if err := s.ValidateTaskOwnership(userID, task); err != nil {
		return err
	}
	if err := s.ValidateTaskStatus(task.Status, status); err != nil {
		return err
	}
	return s.taskPort.UpdateTaskStatus(ctx, tx, taskID, status)
}

func (s *taskService) CompleteTask(ctx context.Context, tx *gorm.DB, userID int64, taskID int64, actualDurationMin int, quality int) error {
	task, err := s.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	if err := s.ValidateTaskOwnership(userID, task); err != nil {
		return err
	}
	if err := s.ValidateTaskStatus(task.Status, string(enum.StatusDone)); err != nil {
		return err
	}
	if quality < 1 || quality > 5 {
		return errors.New(constant.InvalidQuality)
	}
	if err := s.taskPort.UpdateTaskStatus(ctx, tx, taskID, string(enum.StatusDone)); err != nil {
		return err
	}
	if err := s.taskPort.UpdateTaskDuration(ctx, tx, taskID, actualDurationMin); err != nil {
		return err
	}
	return nil
}

func (s *taskService) DeleteTask(ctx context.Context, tx *gorm.DB, userID int64, taskID int64) error {
	task, err := s.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	if err := s.ValidateTaskOwnership(userID, task); err != nil {
		return err
	}
	return s.taskPort.SoftDeleteTask(ctx, tx, taskID)
}

func (s *taskService) GetTaskByID(ctx context.Context, taskID int64) (*entity.TaskEntity, error) {
	return s.taskPort.GetTaskByID(ctx, taskID)
}

func (s *taskService) GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error) {
	return s.taskPort.GetTasksByUserID(ctx, userID, filter)
}

func (s *taskService) GetOverdueTasks(ctx context.Context, userID int64, currentTimeMs int64) ([]*entity.TaskEntity, error) {
	return s.taskPort.GetOverdueTasks(ctx, userID, currentTimeMs)
}

func (s *taskService) GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error) {
	return s.taskPort.GetDeepWorkTasks(ctx, userID)
}
