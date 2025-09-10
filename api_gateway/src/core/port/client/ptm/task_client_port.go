/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type ITaskClientPort interface {
	CreateTask(ctx context.Context, req *request.CreateTaskRequest) (*response.BaseResponse, error)
	GetTaskByID(ctx context.Context, taskID int64) (*response.BaseResponse, error)
	UpdateTask(ctx context.Context, taskID int64, req *request.UpdateTaskRequest) (*response.BaseResponse, error)
	DeleteTask(ctx context.Context, taskID int64) (*response.BaseResponse, error)
	GetCommentsByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error)
}
