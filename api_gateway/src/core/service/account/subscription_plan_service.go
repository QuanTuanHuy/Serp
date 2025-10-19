/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
)

type ISubscriptionPlanService interface {
	CreatePlan(ctx context.Context, req *request.CreateSubscriptionPlanRequest) (*response.BaseResponse, error)
	UpdatePlan(ctx context.Context, planId int64, req *request.UpdateSubscriptionPlanRequest) (*response.BaseResponse, error)
	DeletePlan(ctx context.Context, planId int64) (*response.BaseResponse, error)
	GetPlanById(ctx context.Context, planId int64) (*response.BaseResponse, error)
	GetPlanByCode(ctx context.Context, planCode string) (*response.BaseResponse, error)
	GetAllPlans(ctx context.Context) (*response.BaseResponse, error)
	AddModuleToPlan(ctx context.Context, planId int64, req *request.AddModuleToPlanRequest) (*response.BaseResponse, error)
	RemoveModuleFromPlan(ctx context.Context, planId int64, moduleId int64) (*response.BaseResponse, error)
	GetPlanModules(ctx context.Context, planId int64) (*response.BaseResponse, error)
}

type SubscriptionPlanService struct {
	subscriptionPlanClient port.ISubscriptionPlanClientPort
}

func (s *SubscriptionPlanService) CreatePlan(ctx context.Context, req *request.CreateSubscriptionPlanRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.CreatePlan(ctx, req)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: CreatePlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionPlanService) UpdatePlan(ctx context.Context, planId int64, req *request.UpdateSubscriptionPlanRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.UpdatePlan(ctx, planId, req)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: UpdatePlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionPlanService) DeletePlan(ctx context.Context, planId int64) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.DeletePlan(ctx, planId)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: DeletePlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionPlanService) GetPlanById(ctx context.Context, planId int64) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.GetPlanById(ctx, planId)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: GetPlanById error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionPlanService) GetPlanByCode(ctx context.Context, planCode string) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.GetPlanByCode(ctx, planCode)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: GetPlanByCode error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionPlanService) GetAllPlans(ctx context.Context) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.GetAllPlans(ctx)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: GetAllPlans error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionPlanService) AddModuleToPlan(ctx context.Context, planId int64, req *request.AddModuleToPlanRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.AddModuleToPlan(ctx, planId, req)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: AddModuleToPlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionPlanService) RemoveModuleFromPlan(ctx context.Context, planId int64, moduleId int64) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.RemoveModuleFromPlan(ctx, planId, moduleId)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: RemoveModuleFromPlan error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionPlanService) GetPlanModules(ctx context.Context, planId int64) (*response.BaseResponse, error) {
	res, err := s.subscriptionPlanClient.GetPlanModules(ctx, planId)
	if err != nil {
		log.Error(ctx, "SubscriptionPlanService: GetPlanModules error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewSubscriptionPlanService(subscriptionPlanClient port.ISubscriptionPlanClientPort) ISubscriptionPlanService {
	return &SubscriptionPlanService{
		subscriptionPlanClient: subscriptionPlanClient,
	}
}
