/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type IScheduleGroupService interface {
	CreateScheduleGroup(ctx context.Context, tx *gorm.DB, scheduleGroup *entity.ScheduleGroupEntity) (*entity.ScheduleGroupEntity, error)
	GetScheduleGroupByID(ctx context.Context, ID int64) (*entity.ScheduleGroupEntity, error)
	UpdateScheduleGroup(ctx context.Context, tx *gorm.DB, ID int64, scheduleGroup *entity.ScheduleGroupEntity) (*entity.ScheduleGroupEntity, error)
	DeleteScheduleGroup(ctx context.Context, tx *gorm.DB, ID int64) error
	GetScheduleGroupBySPID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleGroupEntity, error)
	GetScheduleGroupsToCreateTask(ctx context.Context, date time.Time, limit int32) ([]*entity.ScheduleGroupEntity, error)
}

type ScheduleGroupService struct {
	scheduleGroupPort port.IScheduleGroupPort
	scheduleTaskPort  port.IScheduleTaskPort
	dbTxPort          port.IDBTransactionPort
}

func (s *ScheduleGroupService) CreateScheduleGroup(ctx context.Context, tx *gorm.DB, scheduleGroup *entity.ScheduleGroupEntity) (*entity.ScheduleGroupEntity, error) {
	sg, err := s.scheduleGroupPort.CreateScheduleGroup(ctx, tx, scheduleGroup)
	if err != nil {
		log.Error(ctx, "Error in CreateScheduleGroup: ", err)
		return nil, err
	}
	return sg, nil
}

func (s *ScheduleGroupService) GetScheduleGroupByID(ctx context.Context, ID int64) (*entity.ScheduleGroupEntity, error) {
	sg, err := s.scheduleGroupPort.GetScheduleGroupByID(ctx, ID)
	if err != nil {
		log.Error(ctx, "Error in GetScheduleGroupByID: ", err)
		return nil, err
	}
	if sg == nil {
		log.Error(ctx, "ScheduleGroup not found")
		return nil, errors.New(constant.ScheduleGroupNotFound)
	}
	return sg, nil
}

func (s *ScheduleGroupService) DeleteScheduleGroup(ctx context.Context, tx *gorm.DB, ID int64) error {
	err := s.scheduleTaskPort.DeleteByScheduleGroupID(ctx, tx, ID)
	if err != nil {
		log.Error(ctx, "Error deleting tasks in DeleteScheduleGroup: ", err)
		return err
	}
	err = s.scheduleGroupPort.DeleteScheduleGroup(ctx, tx, ID)
	if err != nil {
		log.Error(ctx, "Error in DeleteScheduleGroup: ", err)
		return err
	}
	return nil
}

func (s *ScheduleGroupService) UpdateScheduleGroup(ctx context.Context, tx *gorm.DB, ID int64, scheduleGroup *entity.ScheduleGroupEntity) (*entity.ScheduleGroupEntity, error) {
	sg, err := s.scheduleGroupPort.UpdateScheduleGroup(ctx, tx, ID, scheduleGroup)
	if err != nil {
		log.Error(ctx, "Error in UpdateScheduleGroup: ", err)
		return nil, err
	}
	return sg, nil
}

func (s *ScheduleGroupService) GetScheduleGroupBySPID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleGroupEntity, error) {
	scheduleGroups, err := s.scheduleGroupPort.GetBySchedulePlanID(ctx, schedulePlanID)
	if err != nil {
		log.Error(ctx, "Error in GetScheduleGroupBySPID: ", err)
		return nil, err
	}
	return scheduleGroups, nil
}

func (s *ScheduleGroupService) GetScheduleGroupsToCreateTask(ctx context.Context, date time.Time, limit int32) ([]*entity.ScheduleGroupEntity, error) {
	scheduleGroups, err := s.scheduleGroupPort.GetScheduleGroupsToCreateTask(ctx, date, limit)
	if err != nil {
		log.Error(ctx, "Error in GetScheduleGroupsToCreateTask: ", err)
		return nil, err
	}
	return scheduleGroups, nil
}

func NewScheduleGroupService(
	scheduleGroupPort port.IScheduleGroupPort,
	dbTxPort port.IDBTransactionPort,
	scheduleTaskPort port.IScheduleTaskPort,
) IScheduleGroupService {
	return &ScheduleGroupService{
		scheduleGroupPort: scheduleGroupPort,
		scheduleTaskPort:  scheduleTaskPort,
		dbTxPort:          dbTxPort,
	}
}
