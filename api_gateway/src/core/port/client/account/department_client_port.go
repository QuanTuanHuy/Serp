/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IDepartmentClientPort interface {
	CreateDepartment(ctx context.Context, organizationId int64, req interface{}) (*response.BaseResponse, error)
	GetDepartments(ctx context.Context, organizationId int64, query map[string]string) (*response.BaseResponse, error)
	GetDepartmentById(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error)
	UpdateDepartment(ctx context.Context, organizationId int64, departmentId int64, req interface{}) (*response.BaseResponse, error)
	DeleteDepartment(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error)
	GetDepartmentTree(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error)

	AssignUserToDepartment(ctx context.Context, organizationId int64, departmentId int64, req interface{}) (*response.BaseResponse, error)
	BulkAssignUsersToDepartment(ctx context.Context, organizationId int64, departmentId int64, req interface{}) (*response.BaseResponse, error)
	RemoveUserFromDepartment(ctx context.Context, organizationId int64, departmentId int64, userId int64) (*response.BaseResponse, error)
	GetDepartmentMembers(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error)
}
