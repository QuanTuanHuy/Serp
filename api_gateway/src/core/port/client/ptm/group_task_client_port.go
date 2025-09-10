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

type IGroupTaskClientPort interface {
	CreateGroupTask(ctx context.Context, req *request.CreateGroupTaskRequest) (*response.BaseResponse, error)
	GetGroupTaskByID(ctx context.Context, groupTaskID int64) (*response.BaseResponse, error)
}
