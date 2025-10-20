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

type IRoleClientPort interface {
	CreateRole(ctx context.Context, req *request.CreateRoleDto) (*response.BaseResponse, error)
	GetAllRoles(ctx context.Context) (*response.BaseResponse, error)
	AddPermissionsToRole(ctx context.Context, roleId int64, req *request.AddPermissionToRoleDto) (*response.BaseResponse, error)
}
