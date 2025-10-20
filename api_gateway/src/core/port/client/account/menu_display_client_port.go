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

type IMenuDisplayClientPort interface {
	CreateMenuDisplay(ctx context.Context, req *request.CreateMenuDisplayDto) (*response.BaseResponse, error)
	UpdateMenuDisplay(ctx context.Context, id int64, req *request.UpdateMenuDisplayDto) (*response.BaseResponse, error)
	DeleteMenuDisplay(ctx context.Context, id int64) (*response.BaseResponse, error)
	GetMenuDisplaysByModuleId(ctx context.Context, moduleId int64) (*response.BaseResponse, error)
	AssignMenuDisplaysToRole(ctx context.Context, req *request.AssignMenuDisplayToRoleDto) (*response.BaseResponse, error)
	UnassignMenuDisplaysFromRole(ctx context.Context, req *request.AssignMenuDisplayToRoleDto) (*response.BaseResponse, error)
	GetMenuDisplaysByRoleIds(ctx context.Context, roleIds []int64) (*response.BaseResponse, error)
}
