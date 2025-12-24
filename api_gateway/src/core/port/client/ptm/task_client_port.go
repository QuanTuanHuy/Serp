/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type ITaskClientPort interface {
	CreateTask(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	GetTasksByUserID(ctx context.Context, params map[string]string) (*response.BaseResponse, error)
	GetTaskByID(ctx context.Context, taskID int64) (*response.BaseResponse, error)
	UpdateTask(ctx context.Context, taskID int64, payload map[string]any) (*response.BaseResponse, error)
	DeleteTask(ctx context.Context, taskID int64) (*response.BaseResponse, error)
}
