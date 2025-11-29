/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port2 "github.com/serp/ptm-schedule/src/core/port/client"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type IScheduleTaskService interface {
	SyncSnapshot(ctx context.Context, tx *gorm.DB, planID int64, event *message.TaskUpdatedEvent) (enum.ChangeType, error)
	CreateSnapshot(ctx context.Context, tx *gorm.DB, planID int64, event *message.TaskCreatedEvent) error
	DeleteSnapshot(ctx context.Context, tx *gorm.DB, taskID int64) error
}

type ScheduleTaskService struct {
	scheduleTaskPort port.IScheduleTaskPort
	kafkaProducer    port2.IKafkaProducerPort
	dbTxPort         port.IDBTransactionPort
}

func (s *ScheduleTaskService) CreateSnapshot(ctx context.Context, tx *gorm.DB, planID int64, event *message.TaskCreatedEvent) error {
	panic("unimplemented")
}

func (s *ScheduleTaskService) DeleteSnapshot(ctx context.Context, tx *gorm.DB, taskID int64) error {
	panic("unimplemented")
}

func (s *ScheduleTaskService) SyncSnapshot(ctx context.Context, tx *gorm.DB, planID int64, event *message.TaskUpdatedEvent) (enum.ChangeType, error) {
	panic("unimplemented")
}

func (s *ScheduleTaskService) GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error) {
	scheduleTasks, err := s.scheduleTaskPort.GetBySchedulePlanID(ctx, schedulePlanID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule tasks by schedule plan ID: ", err)
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
