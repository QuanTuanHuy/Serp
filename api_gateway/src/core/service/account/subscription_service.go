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

type ISubscriptionService interface {
	GetAllSubscriptions(ctx context.Context, params *request.GetSubscriptionParams) (*response.BaseResponse, error)
	Subscribe(ctx context.Context, req *request.SubscribeRequest) (*response.BaseResponse, error)
	StartTrial(ctx context.Context, planId int64) (*response.BaseResponse, error)
	ActivateSubscription(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error)
	RejectSubscription(ctx context.Context, subscriptionId int64, req *request.RejectSubscriptionRequest) (*response.BaseResponse, error)
	UpgradeSubscription(ctx context.Context, req *request.UpgradeSubscriptionRequest) (*response.BaseResponse, error)
	DowngradeSubscription(ctx context.Context, req *request.DowngradeSubscriptionRequest) (*response.BaseResponse, error)
	CancelSubscription(ctx context.Context, req *request.CancelSubscriptionRequest) (*response.BaseResponse, error)
	RenewSubscription(ctx context.Context) (*response.BaseResponse, error)
	ExtendTrial(ctx context.Context, subscriptionId int64, req *request.ExtendTrialRequest) (*response.BaseResponse, error)
	ExpireSubscription(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error)
	GetActiveSubscription(ctx context.Context) (*response.BaseResponse, error)
	GetSubscriptionById(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error)
	GetSubscriptionHistory(ctx context.Context) (*response.BaseResponse, error)
}

type SubscriptionService struct {
	subscriptionClient port.ISubscriptionClientPort
}

func (s *SubscriptionService) GetAllSubscriptions(ctx context.Context, params *request.GetSubscriptionParams) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.GetAllSubscriptions(ctx, params)
	if err != nil {
		log.Error(ctx, "SubscriptionService: GetAllSubscriptions error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) Subscribe(ctx context.Context, req *request.SubscribeRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.Subscribe(ctx, req)
	if err != nil {
		log.Error(ctx, "SubscriptionService: Subscribe error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) StartTrial(ctx context.Context, planId int64) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.StartTrial(ctx, planId)
	if err != nil {
		log.Error(ctx, "SubscriptionService: StartTrial error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) ActivateSubscription(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.ActivateSubscription(ctx, subscriptionId)
	if err != nil {
		log.Error(ctx, "SubscriptionService: ActivateSubscription error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) RejectSubscription(ctx context.Context, subscriptionId int64, req *request.RejectSubscriptionRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.RejectSubscription(ctx, subscriptionId, req)
	if err != nil {
		log.Error(ctx, "SubscriptionService: RejectSubscription error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) UpgradeSubscription(ctx context.Context, req *request.UpgradeSubscriptionRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.UpgradeSubscription(ctx, req)
	if err != nil {
		log.Error(ctx, "SubscriptionService: UpgradeSubscription error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) DowngradeSubscription(ctx context.Context, req *request.DowngradeSubscriptionRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.DowngradeSubscription(ctx, req)
	if err != nil {
		log.Error(ctx, "SubscriptionService: DowngradeSubscription error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) CancelSubscription(ctx context.Context, req *request.CancelSubscriptionRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.CancelSubscription(ctx, req)
	if err != nil {
		log.Error(ctx, "SubscriptionService: CancelSubscription error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) RenewSubscription(ctx context.Context) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.RenewSubscription(ctx)
	if err != nil {
		log.Error(ctx, "SubscriptionService: RenewSubscription error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) ExtendTrial(ctx context.Context, subscriptionId int64, req *request.ExtendTrialRequest) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.ExtendTrial(ctx, subscriptionId, req)
	if err != nil {
		log.Error(ctx, "SubscriptionService: ExtendTrial error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) ExpireSubscription(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.ExpireSubscription(ctx, subscriptionId)
	if err != nil {
		log.Error(ctx, "SubscriptionService: ExpireSubscription error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) GetActiveSubscription(ctx context.Context) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.GetActiveSubscription(ctx)
	if err != nil {
		log.Error(ctx, "SubscriptionService: GetActiveSubscription error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) GetSubscriptionById(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.GetSubscriptionById(ctx, subscriptionId)
	if err != nil {
		log.Error(ctx, "SubscriptionService: GetSubscriptionById error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SubscriptionService) GetSubscriptionHistory(ctx context.Context) (*response.BaseResponse, error) {
	res, err := s.subscriptionClient.GetSubscriptionHistory(ctx)
	if err != nil {
		log.Error(ctx, "SubscriptionService: GetSubscriptionHistory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewSubscriptionService(subscriptionClient port.ISubscriptionClientPort) ISubscriptionService {
	return &SubscriptionService{
		subscriptionClient: subscriptionClient,
	}
}
