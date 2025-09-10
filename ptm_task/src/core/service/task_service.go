/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"strconv"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/message"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	port2 "github.com/serp/ptm-task/src/core/port/client"
	port "github.com/serp/ptm-task/src/core/port/store"
)

type ITaskService interface {
	CreateTask(ctx context.Context, userID int64, request *request.CreateTaskDTO) (*entity.TaskEntity, error)
	UpdateTask(ctx context.Context, userID, taskID int64, request *request.UpdateTaskDTO) (*entity.TaskEntity, error)
	DeleteTask(ctx context.Context, userID, taskID int64) error
	GetTaskByID(ctx context.Context, taskID int64) (*entity.TaskEntity, error)
	GetTasksByGroupTaskID(ctx context.Context, groupTaskID int64) ([]*entity.TaskEntity, error)
	PushCreateTaskToKafka(ctx context.Context, task *entity.TaskEntity) error
	PushUpdateTaskToKafka(ctx context.Context, task *entity.TaskEntity, updateTask *request.UpdateTaskDTO) error
	PushDeleteTaskToKafka(ctx context.Context, taskID int64) error
}

type TaskService struct {
	taskPort      port.ITaskPort
	groupTaskPort port.IGroupTaskPort
	dbTx          port.IDBTransactionPort
	redisPort     port2.IRedisPort
	kafkaProducer port2.IKafkaProducerPort
}

func (t *TaskService) GetTasksByGroupTaskID(ctx context.Context, groupTaskID int64) ([]*entity.TaskEntity, error) {
	return t.taskPort.GetTasksByGroupTaskID(ctx, groupTaskID)
}

func (t *TaskService) CreateTask(ctx context.Context, userID int64, request *request.CreateTaskDTO) (*entity.TaskEntity, error) {
	task := mapper.ToTaskEntity(request)
	task.UserID = userID

	var err error
	tx := t.dbTx.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic while creating task: ", r)
			t.dbTx.Rollback(tx)
		}
		if err != nil {
			log.Error(ctx, "Failed to create task: ", err)
			t.dbTx.Rollback(tx)
		}
	}()
	task, err = t.taskPort.CreateTask(ctx, tx, task)
	if err != nil {
		return nil, err
	}
	err = t.dbTx.Commit(tx)
	if err != nil {
		log.Error(ctx, "Failed to commit transaction for task creation: ", err)
		return nil, err
	}
	return task, nil
}

func (t *TaskService) UpdateTask(ctx context.Context, userID, taskID int64, request *request.UpdateTaskDTO) (*entity.TaskEntity, error) {
	task, err := t.GetTaskByUserIDAndTaskID(ctx, userID, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get task by user ID and task ID: ", err)
		return nil, err
	}

	task = mapper.UpdateTaskMapper(task, request)
	var updateErr error
	tx := t.dbTx.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic while updating task: ", r)
			t.dbTx.Rollback(tx)
			return
		}
		if updateErr != nil {
			log.Error(ctx, "Failed to update task: ", updateErr)
			t.dbTx.Rollback(tx)
		}
	}()

	task, updateErr = t.taskPort.UpdateTask(ctx, tx, taskID, task)
	if updateErr != nil {
		return nil, err
	}
	updateErr = t.dbTx.Commit(tx)
	if updateErr != nil {
		return nil, updateErr
	}
	return task, nil
}

func (t *TaskService) PushUpdateTaskToKafka(ctx context.Context, task *entity.TaskEntity, updateTask *request.UpdateTaskDTO) error {
	updateTaskMsg := mapper.ToKafkaUpdateTaskMessage(task, updateTask)
	kafkaMessage := message.CreateKafkaMessage(message.UPDATE_TASK, message.ErrorCodeSuccess, message.ErrorMsgSuccess, updateTaskMsg)
	return t.kafkaProducer.SendMessageAsync(ctx, string(enum.TASK_TOPIC), strconv.FormatInt(task.ID, 10), kafkaMessage)
}

func (t *TaskService) PushCreateTaskToKafka(ctx context.Context, task *entity.TaskEntity) error {
	groupTask, err := t.groupTaskPort.GetGroupTaskByID(ctx, task.GroupTaskID)
	if err != nil {
		log.Error(ctx, "Failed to get group task by ID ", task.GroupTaskID, " error: ", err)
		return err
	}
	createTaskMsg := mapper.ToKafkaCreateTaskMessage(task)
	createTaskMsg.ProjectID = groupTask.ProjectID

	kafkaMessage := message.CreateKafkaMessage(message.CREATE_TASK, message.ErrorCodeSuccess, message.ErrorMsgSuccess, createTaskMsg)
	return t.kafkaProducer.SendMessageAsync(ctx, string(enum.TASK_TOPIC), strconv.FormatInt(task.ID, 10), kafkaMessage)
}

func (t *TaskService) GetTaskByID(ctx context.Context, taskID int64) (*entity.TaskEntity, error) {
	task, err := t.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get task by ID ", taskID, " error: ", err)
		return nil, err
	}
	if task == nil {
		return nil, errors.New(constant.TaskNotFound)
	}
	return task, nil
}

func (t *TaskService) GetTaskByUserIDAndTaskID(ctx context.Context, userID int64, taskID int64) (*entity.TaskEntity, error) {
	task, err := t.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if task == nil {
		return nil, errors.New(constant.TaskNotFound)
	}
	if task.UserID != userID {
		return nil, errors.New(constant.GetTaskForbidden)
	}
	return task, nil
}

func (t *TaskService) DeleteTask(ctx context.Context, userID int64, taskID int64) error {
	_, err := t.GetTaskByUserIDAndTaskID(ctx, userID, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get task by user ID and task ID for deletion: ", err)
		return err
	}

	var deleteErr error
	tx := t.dbTx.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic while deleting task: ", r)
			t.dbTx.Rollback(tx)
			return
		}
		if deleteErr != nil {
			log.Error(ctx, "Failed to delete task: ", deleteErr)
			t.dbTx.Rollback(tx)
		}
	}()
	deleteErr = t.taskPort.DeleteTask(ctx, tx, taskID)
	if deleteErr != nil {
		return deleteErr
	}
	deleteErr = t.dbTx.Commit(tx)
	if deleteErr != nil {
		return deleteErr
	}
	return nil
}

func (t *TaskService) PushDeleteTaskToKafka(ctx context.Context, taskID int64) error {
	deleteTaskMsg := &message.KafkaDeleteTaskMessage{
		TaskID: taskID,
	}
	kafkaMessage := message.CreateKafkaMessage(message.DELETE_TASK, message.ErrorCodeSuccess, message.ErrorMsgSuccess, deleteTaskMsg)
	return t.kafkaProducer.SendMessageAsync(ctx, string(enum.TASK_TOPIC), strconv.FormatInt(taskID, 10), kafkaMessage)
}

func NewTaskService(
	taskPort port.ITaskPort,
	dbTx port.IDBTransactionPort,
	redisPort port2.IRedisPort,
	kafkaProducer port2.IKafkaProducerPort,
	groupTaskPort port.IGroupTaskPort) ITaskService {
	return &TaskService{
		taskPort:      taskPort,
		groupTaskPort: groupTaskPort,
		dbTx:          dbTx,
		redisPort:     redisPort,
		kafkaProducer: kafkaProducer,
	}
}
