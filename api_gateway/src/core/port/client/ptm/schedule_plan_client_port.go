/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type ISchedulePlanClientPort interface {
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
