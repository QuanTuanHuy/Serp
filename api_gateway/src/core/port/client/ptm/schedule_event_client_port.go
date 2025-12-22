/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IScheduleEventClientPort interface {
	ListEvents(ctx context.Context, planID, fromDateMs, toDateMs int64) (*response.BaseResponse, error)
	SaveEvents(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	ManuallyMoveEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error)
	CompleteEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error)
	SplitEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error)
}
