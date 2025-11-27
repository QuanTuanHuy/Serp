/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

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
	"gorm.io/gorm"
)

type IScheduleTaskService interface {
	CreateScheduleTask(ctx context.Context, tx *gorm.DB, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	PushCreateScheduleTaskMessage(ctx context.Context, scheduleTask *entity.ScheduleTaskEntity) error
	UpdateScheduleTask(ctx context.Context, tx *gorm.DB, ID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	DeleteScheduleTask(ctx context.Context, tx *gorm.DB, ID int64) error
	GetScheduleTaskByID(ctx context.Context, ID int64) (*entity.ScheduleTaskEntity, error)
	GetScheduleTaskByTaskID(ctx context.Context, taskID int64) ([]*entity.ScheduleTaskEntity, error)
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
		log.Error(ctx, "Failed to get schedule task by ID: ", err)
		return nil, err
	}
	if scheduleTask == nil {
		return nil, errors.New(constant.ScheduleTaskNotFound)
	}
	return scheduleTask, nil
}

func (s *ScheduleTaskService) GetScheduleTaskByTaskID(ctx context.Context, taskID int64) ([]*entity.ScheduleTaskEntity, error) {
	scheduleTasks, err := s.scheduleTaskPort.GetScheduleTaskByTaskID(ctx, taskID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule task by task ID: ", taskID, err)
		return nil, err
	}
	return scheduleTasks, nil
}

func (s *ScheduleTaskService) CreateScheduleTask(ctx context.Context, tx *gorm.DB, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	scheduleTask, err := s.scheduleTaskPort.CreateScheduleTask(ctx, tx, scheduleTask)
	if err != nil {
		log.Error(ctx, "Failed to create schedule task: ", err)
		return nil, err
	}
	return scheduleTask, nil
}

func (s *ScheduleTaskService) UpdateScheduleTask(ctx context.Context, tx *gorm.DB, ID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	scheduleTask, err := s.scheduleTaskPort.UpdateScheduleTask(ctx, tx, ID, scheduleTask)
	if err != nil {
		log.Error(ctx, "Failed to update schedule task: ", err)
		return nil, err
	}
	return scheduleTask, nil
}

func (s *ScheduleTaskService) DeleteScheduleTask(ctx context.Context, tx *gorm.DB, ID int64) error {
	err := s.scheduleTaskPort.DeleteScheduleTask(ctx, tx, ID)
	if err != nil {
		log.Error(ctx, "Failed to delete schedule task: ", err)
		return err
	}
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
