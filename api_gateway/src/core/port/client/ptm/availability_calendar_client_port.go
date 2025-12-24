/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IAvailabilityCalendarClientPort interface {
	GetAvailability(ctx context.Context) (*response.BaseResponse, error)
	SetAvailability(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	ReplaceAvailability(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
}
