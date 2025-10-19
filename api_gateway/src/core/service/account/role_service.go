/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
)

type IRoleService interface {
	CreateRole(ctx context.Context, req *request.CreateRoleDto) (*response.BaseResponse, error)
	GetAllRoles(ctx context.Context) (*response.BaseResponse, error)
	AddPermissionsToRole(ctx context.Context, roleId int64, req *request.AddPermissionToRoleDto) (*response.BaseResponse, error)
}

type RoleService struct {
	roleClient port.IRoleClientPort
}

func (r *RoleService) CreateRole(ctx context.Context, req *request.CreateRoleDto) (*response.BaseResponse, error) {
	res, err := r.roleClient.CreateRole(ctx, req)
	if err != nil {
		log.Error(ctx, "RoleService: CreateRole error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (r *RoleService) GetAllRoles(ctx context.Context) (*response.BaseResponse, error) {
	res, err := r.roleClient.GetAllRoles(ctx)
	if err != nil {
		log.Error(ctx, "RoleService: GetAllRoles error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (r *RoleService) AddPermissionsToRole(ctx context.Context, roleId int64, req *request.AddPermissionToRoleDto) (*response.BaseResponse, error) {
	res, err := r.roleClient.AddPermissionsToRole(ctx, roleId, req)
	if err != nil {
		log.Error(ctx, "RoleService: AddPermissionsToRole error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewRoleService(roleClient port.IRoleClientPort) IRoleService {
	return &RoleService{
		roleClient: roleClient,
	}
}
