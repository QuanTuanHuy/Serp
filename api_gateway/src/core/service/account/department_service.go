/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
)

type IDepartmentService interface {
	CreateDepartment(ctx context.Context, organizationId int64, req any) (*response.BaseResponse, error)
	GetDepartments(ctx context.Context, organizationId int64, query map[string]string) (*response.BaseResponse, error)
	GetDepartmentById(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error)
	UpdateDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error)
	DeleteDepartment(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error)
	GetDepartmentTree(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error)

	AssignUserToDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error)
	BulkAssignUsersToDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error)
	RemoveUserFromDepartment(ctx context.Context, organizationId int64, departmentId int64, userId int64) (*response.BaseResponse, error)
	GetDepartmentMembers(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error)
}

type DepartmentService struct {
	deptClient port.IDepartmentClientPort
}

func (d *DepartmentService) CreateDepartment(ctx context.Context, organizationId int64, req any) (*response.BaseResponse, error) {
	return d.deptClient.CreateDepartment(ctx, organizationId, req)
}

func (d *DepartmentService) GetDepartments(ctx context.Context, organizationId int64, query map[string]string) (*response.BaseResponse, error) {
	return d.deptClient.GetDepartments(ctx, organizationId, query)
}

func (d *DepartmentService) GetDepartmentById(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error) {
	return d.deptClient.GetDepartmentById(ctx, organizationId, departmentId)
}

func (d *DepartmentService) UpdateDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error) {
	return d.deptClient.UpdateDepartment(ctx, organizationId, departmentId, req)
}

func (d *DepartmentService) DeleteDepartment(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error) {
	return d.deptClient.DeleteDepartment(ctx, organizationId, departmentId)
}

func (d *DepartmentService) GetDepartmentTree(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error) {
	return d.deptClient.GetDepartmentTree(ctx, organizationId, departmentId)
}

func (d *DepartmentService) AssignUserToDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error) {
	return d.deptClient.AssignUserToDepartment(ctx, organizationId, departmentId, req)
}

func (d *DepartmentService) BulkAssignUsersToDepartment(ctx context.Context, organizationId int64, departmentId int64, req any) (*response.BaseResponse, error) {
	return d.deptClient.BulkAssignUsersToDepartment(ctx, organizationId, departmentId, req)
}

func (d *DepartmentService) RemoveUserFromDepartment(ctx context.Context, organizationId int64, departmentId int64, userId int64) (*response.BaseResponse, error) {
	return d.deptClient.RemoveUserFromDepartment(ctx, organizationId, departmentId, userId)
}

func (d *DepartmentService) GetDepartmentMembers(ctx context.Context, organizationId int64, departmentId int64) (*response.BaseResponse, error) {
	return d.deptClient.GetDepartmentMembers(ctx, organizationId, departmentId)
}

func NewDepartmentService(deptClient port.IDepartmentClientPort) IDepartmentService {
	return &DepartmentService{deptClient: deptClient}
}
