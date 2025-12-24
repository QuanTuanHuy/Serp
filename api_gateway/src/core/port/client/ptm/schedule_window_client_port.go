/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IScheduleWindowClientPort interface {
	ListAvailabilityWindows(ctx context.Context, fromDateMs, toDateMs int64) (*response.BaseResponse, error)
	MaterializeWindows(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
}
