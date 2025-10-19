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

type IPermissionClientPort interface {
	CreatePermission(ctx context.Context, req *request.CreatePermissionDto) (*response.BaseResponse, error)
	GetAllPermissions(ctx context.Context) (*response.BaseResponse, error)
}
