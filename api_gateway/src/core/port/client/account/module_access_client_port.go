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

type IModuleAccessClientPort interface {
	CanOrganizationAccessModule(ctx context.Context, organizationId int64, moduleId int64) (*response.BaseResponse, error)
	GetAccessibleModulesForOrganization(ctx context.Context, organizationId int64) (*response.BaseResponse, error)
	AssignUserToModule(ctx context.Context, organizationId int64, req *request.AssignUserToModuleRequest) (*response.BaseResponse, error)
	BulkAssignUsersToModule(ctx context.Context, req *request.BulkAssignUsersRequest) (*response.BaseResponse, error)
	RevokeUserAccessToModule(ctx context.Context, organizationId int64, moduleId int64, userId int64) (*response.BaseResponse, error)
	GetUsersWithAccessToModule(ctx context.Context, organizationId int64, moduleId int64) (*response.BaseResponse, error)
	GetModulesAccessibleByUser(ctx context.Context, organizationId int64) (*response.BaseResponse, error)
}
