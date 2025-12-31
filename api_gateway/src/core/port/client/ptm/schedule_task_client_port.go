/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IScheduleTaskClientPort interface {
	GetScheduleTasksByPlanID(ctx context.Context, params map[string]string) (*response.BaseResponse, error)
}
