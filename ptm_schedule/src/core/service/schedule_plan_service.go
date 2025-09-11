/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/mapper"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type ISchedulePlanService interface {
	CreateSchedulePlan(ctx context.Context, tx *gorm.DB, userID int64) (*entity.SchedulePlanEntity, error)
	GetSchedulePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error)
	UpdateSchedulePlan(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error)
	UpdateTaskBatch(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity, activeBatch int32, isActiveBatch bool) (*entity.SchedulePlanEntity, error)
}

type SchedulePlanService struct {
	schedulePlanPort port.ISchedulePlanPort
	dbTxPort         port.IDBTransactionPort
}

func (s *SchedulePlanService) CreateSchedulePlan(ctx context.Context, tx *gorm.DB, userID int64) (*entity.SchedulePlanEntity, error) {
	var err error
	existed, err := s.schedulePlanPort.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if existed != nil {
		return existed, nil
	}
	schedulePlan := mapper.CreateSchedulePlanMapper(userID)
	schedulePlan, err = s.schedulePlanPort.CreateSchedulePlan(ctx, tx, schedulePlan)
	if err != nil {
		log.Error(ctx, "Failed to create schedule plan: ", "error", err)
		return nil, err
	}

	return schedulePlan, nil

}

func (s *SchedulePlanService) GetSchedulePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error) {
	schedulePlan, err := s.schedulePlanPort.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		log.Error(ctx, "Failed to get schedule plan by user ID: ", "error", err)
		return nil, err
	}
	if schedulePlan == nil {
		log.Error(ctx, "No schedule plan found for user ID: ", "userID", userID)
		return nil, errors.New(constant.SchedulePlanNotFound)
	}
	return schedulePlan, nil
}

func (s *SchedulePlanService) UpdateTaskBatch(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity, activeBatch int32, isActiveBatch bool) (*entity.SchedulePlanEntity, error) {
	schedulePlan.UpdateTaskBatch(activeBatch, isActiveBatch)
	return s.UpdateSchedulePlan(ctx, tx, schedulePlan)
}

func (s *SchedulePlanService) UpdateSchedulePlan(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error) {
	schedulePlan, err := s.schedulePlanPort.UpdateSchedulePlan(ctx, tx, schedulePlan)
	if err != nil {
		log.Error(ctx, "Failed to update schedule plan: ", "error", err)
		return nil, err
	}
	return schedulePlan, nil
}

func NewSchedulePlanService(schedulePlanPort port.ISchedulePlanPort, dbTxPort port.IDBTransactionPort) ISchedulePlanService {
	return &SchedulePlanService{
		schedulePlanPort: schedulePlanPort,
		dbTxPort:         dbTxPort,
	}
}
