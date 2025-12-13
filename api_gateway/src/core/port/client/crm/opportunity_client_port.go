/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package crm

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IOpportunityClientPort interface {
	CreateOpportunity(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	UpdateOpportunity(ctx context.Context, opportunityId int64, payload map[string]any) (*response.BaseResponse, error)
	GetOpportunityByID(ctx context.Context, opportunityId int64) (*response.BaseResponse, error)
	GetOpportunities(ctx context.Context, page, size int) (*response.BaseResponse, error)
	FilterOpportunities(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)

	ChangeStage(ctx context.Context, opportunityId int64, newStage string) (*response.BaseResponse, error)
	CloseAsWon(ctx context.Context, opportunityId int64) (*response.BaseResponse, error)
	CloseAsLost(ctx context.Context, opportunityId int64, lostReason string) (*response.BaseResponse, error)
	DeleteOpportunity(ctx context.Context, opportunityId int64) (*response.BaseResponse, error)
}
