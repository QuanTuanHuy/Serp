package service

import (
	"context"
	"errors"
	"fmt"
	"strconv"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/core/domain/mapper"
	port2 "github.com/serp/ptm-schedule/src/core/port/client"
	port "github.com/serp/ptm-schedule/src/core/port/store"
)

type IScheduleTaskService interface {
	CreateScheduleTask(ctx context.Context, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	PushCreateScheduleTaskMessage(ctx context.Context, scheduleTask *entity.ScheduleTaskEntity) error
	UpdateScheduleTask(ctx context.Context, ID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	DeleteScheduleTask(ctx context.Context, ID int64) error
	GetScheduleTaskByID(ctx context.Context, ID int64) (*entity.ScheduleTaskEntity, error)
	GetScheduleTaskByTaskID(ctx context.Context, taskID int64) (*entity.ScheduleTaskEntity, error)
	GetByTaskBatch(ctx context.Context, schedulePlanID int64, taskBatch int32) ([]*entity.ScheduleTaskEntity, error)
	GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error)
	GetTopNewestTasks(ctx context.Context, schedulePlanID int64, limit int) ([]*entity.ScheduleTaskEntity, error)
}

type ScheduleTaskService struct {
	scheduleTaskPort port.IScheduleTaskPort
	kafkaProducer    port2.IKafkaProducerPort
	dbTxPort         port.IDBTransactionPort
}

func (s *ScheduleTaskService) PushCreateScheduleTaskMessage(ctx context.Context, scheduleTask *entity.ScheduleTaskEntity) error {
	createScheduleTaskMsg := mapper.ToCreateScheduleTaskMessage(scheduleTask)
	kafkaMessage := message.CreateKafkaMessage(message.CREATE_SCHEDULE_TASK, "00", "Successful", createScheduleTaskMsg)
	return s.kafkaProducer.SendMessageAsync(ctx, string(enum.SCHEDULE_TASK_TOPIC), strconv.FormatInt(scheduleTask.TaskID, 10), kafkaMessage)
}

func (s *ScheduleTaskService) GetScheduleTaskByID(ctx context.Context, ID int64) (*entity.ScheduleTaskEntity, error) {
	scheduleTask, err := s.scheduleTaskPort.GetScheduleTaskByID(ctx, ID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule task by ID: ", "error", err)
		return nil, err
	}
	if scheduleTask == nil {
		return nil, errors.New(constant.ScheduleTaskNotFound)
	}
	return scheduleTask, nil
}

func (s *ScheduleTaskService) GetScheduleTaskByTaskID(ctx context.Context, taskID int64) (*entity.ScheduleTaskEntity, error) {
	scheduleTask, err := s.scheduleTaskPort.GetScheduleTaskByTaskID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule task by task ID: ", taskID, "error", err)
		return nil, err
	}
	if scheduleTask == nil {
		return nil, errors.New(constant.ScheduleTaskNotFound)
	}
	return scheduleTask, nil
}

func (s *ScheduleTaskService) CreateScheduleTask(ctx context.Context, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	var err error
	tx := s.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic in CreateScheduleTask ", r)
			s.dbTxPort.Rollback(tx)
			return
		}
		if err != nil {
			log.Error(ctx, "Error in CreateScheduleTask: ", err)
			s.dbTxPort.Rollback(tx)
		}
	}()

	scheduleTask, err = s.scheduleTaskPort.CreateScheduleTask(ctx, tx, scheduleTask)
	if err != nil {
		log.Error(ctx, "Failed to create schedule task: ", "error", err)
		return nil, err
	}
	err = s.PushCreateScheduleTaskMessage(ctx, scheduleTask)
	if err != nil {
		log.Error(ctx, "Failed to push create schedule task message: ", err)
		return nil, err
	}
	err = tx.Commit().Error
	if err != nil {
		return nil, err
	}
	return scheduleTask, nil
}

func (s *ScheduleTaskService) UpdateScheduleTask(ctx context.Context, ID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	var err error
	tx := s.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic in UpdateScheduleTask ", "error ", r)
			s.dbTxPort.Rollback(tx)
			return
		}
		if err != nil {
			log.Error(ctx, "Error in UpdateScheduleTask: ", "error", err)
			s.dbTxPort.Rollback(tx)
		}
	}()
	scheduleTask, err = s.scheduleTaskPort.UpdateScheduleTask(ctx, tx, ID, scheduleTask)
	if err != nil {
		log.Error(ctx, "Failed to update schedule task: ", "error", err)
		return nil, err
	}
	err = tx.Commit().Error
	if err != nil {
		return nil, err
	}
	return scheduleTask, nil
}

func (s *ScheduleTaskService) DeleteScheduleTask(ctx context.Context, ID int64) error {
	var err error
	tx := s.dbTxPort.StartTransaction()
	defer func() {
		if r := recover(); r != nil {
			log.Error(ctx, "Recovered from panic in DeleteScheduleTask ", "error ", r)
			s.dbTxPort.Rollback(tx)
			return
		}
		if err != nil {
			log.Error(ctx, "Error in DeleteScheduleTask: ", "error", err)
			s.dbTxPort.Rollback(tx)
		}
	}()
	err = s.scheduleTaskPort.DeleteScheduleTask(ctx, tx, ID)
	if err != nil {
		log.Error(ctx, "Failed to delete schedule task: ", "error", err)
		return err
	}
	err = tx.Commit().Error
	if err != nil {
		return err
	}
	return nil
}

func (s *ScheduleTaskService) GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error) {
	scheduleTasks, err := s.scheduleTaskPort.GetBySchedulePlanID(ctx, schedulePlanID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule tasks by schedule plan ID: ", "error", err)
		return nil, err
	}
	return scheduleTasks, nil
}

func (s *ScheduleTaskService) GetByTaskBatch(ctx context.Context, schedulePlanID int64, taskBatch int32) ([]*entity.ScheduleTaskEntity, error) {
	scheduleTasks, err := s.scheduleTaskPort.GetByTaskBatch(ctx, schedulePlanID, taskBatch)
	if err != nil {
		log.Error(ctx, "Failed to get schedule tasks by task batch: ", "error", err)
		return nil, err
	}
	return scheduleTasks, nil
}

func (s *ScheduleTaskService) GetTopNewestTasks(ctx context.Context, schedulePlanID int64, limit int) ([]*entity.ScheduleTaskEntity, error) {
	scheduleTasks, err := s.scheduleTaskPort.GetTopNewestTasks(ctx, schedulePlanID, limit)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Failed to get top newest schedule tasks for schedule plan ID %d: %v", schedulePlanID, err))
		return nil, err
	}
	return scheduleTasks, nil
}

func NewScheduleTaskService(
	scheduleTaskPort port.IScheduleTaskPort,
	kafkaProducer port2.IKafkaProducerPort,
	dbTxPort port.IDBTransactionPort,
) IScheduleTaskService {
	return &ScheduleTaskService{
		scheduleTaskPort: scheduleTaskPort,
		kafkaProducer:    kafkaProducer,
		dbTxPort:         dbTxPort,
	}
}
