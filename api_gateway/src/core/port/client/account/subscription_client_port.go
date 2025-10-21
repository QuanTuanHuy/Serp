/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type ISubscriptionClientPort interface {
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
