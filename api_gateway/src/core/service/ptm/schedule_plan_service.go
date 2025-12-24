/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
)

type ISchedulePlanService interface {
	GetOrCreateActivePlan(ctx context.Context) (*response.BaseResponse, error)
	GetActivePlan(ctx context.Context) (*response.BaseResponse, error)
	GetActivePlanDetail(ctx context.Context, fromDateMs, toDateMs int64) (*response.BaseResponse, error)
	GetPlanHistory(ctx context.Context, page, pageSize int) (*response.BaseResponse, error)
	TriggerReschedule(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	GetPlanByID(ctx context.Context, planID int64) (*response.BaseResponse, error)
	GetPlanWithEvents(ctx context.Context, planID int64, fromDateMs, toDateMs int64) (*response.BaseResponse, error)
	ApplyProposedPlan(ctx context.Context, planID int64) (*response.BaseResponse, error)
	RevertToPlan(ctx context.Context, planID int64) (*response.BaseResponse, error)
	DiscardProposedPlan(ctx context.Context, planID int64) (*response.BaseResponse, error)
}

type SchedulePlanService struct {
	schedulePlanClient port.ISchedulePlanClientPort
}

func (s *SchedulePlanService) GetOrCreateActivePlan(ctx context.Context) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.GetOrCreateActivePlan(ctx)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: GetOrCreateActivePlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) GetActivePlan(ctx context.Context) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.GetActivePlan(ctx)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: GetActivePlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) GetActivePlanDetail(ctx context.Context, fromDateMs, toDateMs int64) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.GetActivePlanDetail(ctx, fromDateMs, toDateMs)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: GetActivePlanDetail error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) GetPlanHistory(ctx context.Context, page, pageSize int) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.GetPlanHistory(ctx, page, pageSize)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: GetPlanHistory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) TriggerReschedule(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.TriggerReschedule(ctx, payload)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: TriggerReschedule error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) GetPlanByID(ctx context.Context, planID int64) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.GetPlanByID(ctx, planID)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: GetPlanByID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) GetPlanWithEvents(ctx context.Context, planID int64, fromDateMs, toDateMs int64) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.GetPlanWithEvents(ctx, planID, fromDateMs, toDateMs)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: GetPlanWithEvents error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) ApplyProposedPlan(ctx context.Context, planID int64) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.ApplyProposedPlan(ctx, planID)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: ApplyProposedPlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) RevertToPlan(ctx context.Context, planID int64) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.RevertToPlan(ctx, planID)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: RevertToPlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SchedulePlanService) DiscardProposedPlan(ctx context.Context, planID int64) (*response.BaseResponse, error) {
	res, err := s.schedulePlanClient.DiscardProposedPlan(ctx, planID)
	if err != nil {
		log.Error(ctx, "SchedulePlanService: DiscardProposedPlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewSchedulePlanService(schedulePlanClient port.ISchedulePlanClientPort) ISchedulePlanService {
	return &SchedulePlanService{
		schedulePlanClient: schedulePlanClient,
	}
}
