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

type ISubscriptionPlanClientPort interface {
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
