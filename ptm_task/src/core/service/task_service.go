/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"strconv"
	"time"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/message"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	client "github.com/serp/ptm-task/src/core/port/client"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/kernel/utils"
	"gorm.io/gorm"
)

type ITaskService interface {
	ValidateTaskData(task *entity.TaskEntity) error
	ValidateTaskOwnership(userID int64, task *entity.TaskEntity) error
	ValidateTaskStatus(currentStatus, newStatus string) error
	ValidateTaskDeadline(task *entity.TaskEntity, projectDeadline *int64) error

	CalculatePriorityScore(task *entity.TaskEntity, currentTimeMs int64) float64
	CheckIfOverdue(task *entity.TaskEntity, currentTimeMs int64) bool
	CheckIfCanBeScheduled(task *entity.TaskEntity) bool
	CheckIfHasIncompleteSubtasks(ctx context.Context, taskID int64) (bool, error)

	CreateTask(ctx context.Context, tx *gorm.DB, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error)
	UpdateTask(ctx context.Context, tx *gorm.DB, userID int64, oldTask, newTask *entity.TaskEntity) (*entity.TaskEntity, error)
	DeleteTask(ctx context.Context, tx *gorm.DB, userID int64, taskID int64) error
	DeleteTaskRecursively(ctx context.Context, tx *gorm.DB, userID int64, taskID int64) ([]*entity.TaskEntity, error)

	GetTaskByID(ctx context.Context, taskID int64) (*entity.TaskEntity, error)
	GetTaskTreeByTaskID(ctx context.Context, taskID int64) (*entity.TaskEntity, error)
	GetTaskByUserIDAndID(ctx context.Context, userID, taskID int64) (*entity.TaskEntity, error)
	GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error)
	CountTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) (int64, error)
	GetTaskByProjectID(ctx context.Context, projectID int64) ([]*entity.TaskEntity, error)
	GetTaskByParentID(ctx context.Context, parentTaskID int64) ([]*entity.TaskEntity, error)
	GetOverdueTasks(ctx context.Context, userID int64, currentTimeMs int64) ([]*entity.TaskEntity, error)
	GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)
	GetDependentTasks(ctx context.Context, taskID int64) ([]*entity.TaskEntity, error)

	PushTaskCreatedEvent(ctx context.Context, task *entity.TaskEntity) error
	PushTaskUpdatedEvent(ctx context.Context, task *entity.TaskEntity, req *request.UpdateTaskRequest) error
	PushTaskDeletedEvent(ctx context.Context, taskID, userID int64) error
	PushBulkTaskDeletedEvent(ctx context.Context, taskIDs []int64, userID int64) error
}

type taskService struct {
	taskPort      store.ITaskPort
	kafkaProducer client.IKafkaProducerPort
	mapper        *mapper.TaskMapper
}

func NewTaskService(taskPort store.ITaskPort, kafkaProducer client.IKafkaProducerPort) ITaskService {
	return &taskService{
		taskPort:      taskPort,
		kafkaProducer: kafkaProducer,
		mapper:        mapper.NewTaskMapper(),
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

func (s *taskService) ValidateTaskDeadline(task *entity.TaskEntity, projectDeadline *int64) error {
	if task.DeadlineMs != nil && projectDeadline != nil {
		if *task.DeadlineMs > *projectDeadline {
			return errors.New("task deadline cannot be after project deadline")
		}
	}
	return nil
}

func (s *taskService) CheckIfHasIncompleteSubtasks(ctx context.Context, taskID int64) (bool, error) {
	subtasks, err := s.taskPort.GetTasksByParentID(ctx, taskID)
	if err != nil {
		return false, err
	}
	for _, subtask := range subtasks {
		if !subtask.IsCompleted() {
			return true, nil
		}
	}
	return false, nil
}

func (s *taskService) GetDependentTasks(ctx context.Context, taskID int64) ([]*entity.TaskEntity, error) {
	// Implement later
	return []*entity.TaskEntity{}, nil
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
	if task.Priority == "" {
		task.Priority = string(enum.PriorityMedium)
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
	task, err := s.taskPort.CreateTask(ctx, tx, task)
	if err != nil {
		return nil, err
	}
	return task, nil
}

func (s *taskService) UpdateTask(ctx context.Context, tx *gorm.DB, userID int64, oldTask, newTask *entity.TaskEntity) (*entity.TaskEntity, error) {
	if err := s.ValidateTaskOwnership(userID, newTask); err != nil {
		return nil, err
	}
	if err := s.ValidateTaskData(newTask); err != nil {
		return nil, err
	}

	if oldTask.Status != newTask.Status {
		if err := s.ValidateTaskStatus(oldTask.Status, newTask.Status); err != nil {
			return nil, err
		}

		if newTask.IsCompleted() {
			hasIncomplete, err := s.CheckIfHasIncompleteSubtasks(ctx, newTask.ID)
			if err != nil {
				return nil, err
			}
			if hasIncomplete {
				return nil, errors.New(constant.CannotCompleteTaskWithSubtasks)
			}
		}
	}

	now := time.Now().UnixMilli()
	newTask.UpdatedAt = now
	score := s.CalculatePriorityScore(newTask, now)
	newTask.PriorityScore = &score

	if !oldTask.IsCompleted() && newTask.IsCompleted() {
		newTask.CompletedAt = &now
	}
	if oldTask.IsCompleted() && !newTask.IsCompleted() {
		newTask.CompletedAt = nil
	}

	return s.taskPort.UpdateTask(ctx, tx, newTask)
}

func (s *taskService) DeleteTask(ctx context.Context, tx *gorm.DB, userID int64, taskID int64) error {
	task, err := s.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	if err := s.ValidateTaskOwnership(userID, task); err != nil {
		return err
	}
	return s.taskPort.SoftDeleteTask(ctx, tx, taskID)
}

func (s *taskService) DeleteTaskRecursively(ctx context.Context, tx *gorm.DB, userID int64, taskID int64) ([]*entity.TaskEntity, error) {
	tasks, err := s.taskPort.GetTasksByRootID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	var deletedTasks []*entity.TaskEntity
	for _, task := range tasks {
		if err := s.ValidateTaskOwnership(userID, task); err != nil {
			return nil, err
		}
		if err := s.taskPort.SoftDeleteTask(ctx, tx, task.ID); err != nil {
			return nil, err
		}
		deletedTasks = append(deletedTasks, task)
	}
	return deletedTasks, nil
}

func (s *taskService) GetTaskByID(ctx context.Context, taskID int64) (*entity.TaskEntity, error) {
	task, err := s.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if task == nil {
		return nil, errors.New(constant.TaskNotFound)
	}
	return task, nil
}

func (s *taskService) GetTaskTreeByTaskID(ctx context.Context, rootID int64) (*entity.TaskEntity, error) {
	tasks, err := s.taskPort.GetTasksByRootID(ctx, rootID)
	if err != nil {
		return nil, err
	}
	if len(tasks) == 0 {
		return nil, errors.New(constant.TaskNotFound)
	}
	taskMap := make(map[int64]*entity.TaskEntity)
	for _, task := range tasks {
		task.SubTasks = []*entity.TaskEntity{}
		taskMap[task.ID] = task
	}
	var rootTask *entity.TaskEntity
	for _, task := range tasks {
		if task.ParentTaskID != nil {
			parentTask, exists := taskMap[*task.ParentTaskID]
			if exists {
				parentTask.SubTasks = append(parentTask.SubTasks, task)
			}
		}
		if task.ID == rootID {
			rootTask = task
		}
	}
	return rootTask, nil
}

func (s *taskService) GetTaskByUserIDAndID(ctx context.Context, userID, taskID int64) (*entity.TaskEntity, error) {
	task, err := s.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if task == nil || task.UserID != userID {
		return nil, errors.New(constant.TaskNotFound)
	}
	return task, nil
}

func (s *taskService) GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error) {
	return s.taskPort.GetTasksByUserID(ctx, userID, filter)
}

func (s *taskService) CountTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) (int64, error) {
	return s.taskPort.CountTasksByUserID(ctx, userID, filter)
}

func (s *taskService) GetTaskByProjectID(ctx context.Context, projectID int64) ([]*entity.TaskEntity, error) {
	return s.taskPort.GetTasksByProjectID(ctx, projectID)
}

func (s *taskService) GetTaskByParentID(ctx context.Context, parentTaskID int64) ([]*entity.TaskEntity, error) {
	return s.taskPort.GetTasksByParentID(ctx, parentTaskID)
}

func (s *taskService) GetOverdueTasks(ctx context.Context, userID int64, currentTimeMs int64) ([]*entity.TaskEntity, error) {
	return s.taskPort.GetOverdueTasks(ctx, userID, currentTimeMs)
}

func (s *taskService) GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error) {
	return s.taskPort.GetDeepWorkTasks(ctx, userID)
}

func (s *taskService) PushTaskCreatedEvent(ctx context.Context, task *entity.TaskEntity) error {
	event := s.mapper.CreateTaskCreatedEvent(task)
	message := utils.BuildCreatedEvent(ctx, "task", event)
	return s.kafkaProducer.SendMessage(ctx, string(constant.TASK_TOPIC), strconv.FormatInt(task.ID, 10), message)
}

func (s *taskService) PushTaskUpdatedEvent(ctx context.Context, task *entity.TaskEntity, req *request.UpdateTaskRequest) error {
	if task == nil || req == nil {
		return nil
	}
	event := s.mapper.CreateTaskUpdatedEvent(task, req)
	message := utils.BuildUpdatedEvent(ctx, "task", event)
	return s.kafkaProducer.SendMessage(ctx, string(constant.TASK_TOPIC), strconv.FormatInt(task.ID, 10), message)
}

func (s *taskService) PushTaskDeletedEvent(ctx context.Context, taskID, userID int64) error {
	event := &message.TaskDeletedEvent{
		TaskID: taskID,
		UserID: userID,
	}
	message := utils.BuildDeletedEvent(ctx, "task", event)
	return s.kafkaProducer.SendMessage(ctx, string(constant.TASK_TOPIC), strconv.FormatInt(taskID, 10), message)
}

func (s *taskService) PushBulkTaskDeletedEvent(ctx context.Context, taskIDs []int64, userID int64) error {
	event := &message.BulkTaskDeletedEvent{
		TaskIDs: taskIDs,
		UserID:  userID,
	}
	message := utils.BuildBulkDeletedEvent(ctx, "task", event)
	return s.kafkaProducer.SendMessage(ctx, string(constant.TASK_TOPIC), "bulk-"+strconv.FormatInt(userID, 10), message)
}
