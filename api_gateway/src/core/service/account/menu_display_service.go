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

type IMenuDisplayService interface {
	CreateMenuDisplay(ctx context.Context, req *request.CreateMenuDisplayDto) (*response.BaseResponse, error)
	UpdateMenuDisplay(ctx context.Context, id int64, req *request.UpdateMenuDisplayDto) (*response.BaseResponse, error)
	DeleteMenuDisplay(ctx context.Context, id int64) (*response.BaseResponse, error)
	GetMenuDisplaysByModuleId(ctx context.Context, moduleId int64) (*response.BaseResponse, error)
	GetMenuDisplaysByModuleIdAndUserId(ctx context.Context, moduleId int64) (*response.BaseResponse, error)
	GetAllMenuDisplays(ctx context.Context, params *request.GetMenuDisplayParams) (*response.BaseResponse, error)
	AssignMenuDisplaysToRole(ctx context.Context, req *request.AssignMenuDisplayToRoleDto) (*response.BaseResponse, error)
	UnassignMenuDisplaysFromRole(ctx context.Context, req *request.AssignMenuDisplayToRoleDto) (*response.BaseResponse, error)
	GetMenuDisplaysByRoleIds(ctx context.Context, roleIds []int64) (*response.BaseResponse, error)
}

type MenuDisplayService struct {
	menuDisplayClient port.IMenuDisplayClientPort
}

func (m *MenuDisplayService) CreateMenuDisplay(ctx context.Context, req *request.CreateMenuDisplayDto) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.CreateMenuDisplay(ctx, req)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: CreateMenuDisplay error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (m *MenuDisplayService) UpdateMenuDisplay(ctx context.Context, id int64, req *request.UpdateMenuDisplayDto) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.UpdateMenuDisplay(ctx, id, req)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: UpdateMenuDisplay error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (m *MenuDisplayService) DeleteMenuDisplay(ctx context.Context, id int64) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.DeleteMenuDisplay(ctx, id)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: DeleteMenuDisplay error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (m *MenuDisplayService) GetMenuDisplaysByModuleId(ctx context.Context, moduleId int64) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.GetMenuDisplaysByModuleId(ctx, moduleId)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: GetMenuDisplaysByModuleId error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (m *MenuDisplayService) GetAllMenuDisplays(ctx context.Context, params *request.GetMenuDisplayParams) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.GetAllMenuDisplays(ctx, params)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: GetAllMenuDisplays error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (m *MenuDisplayService) GetMenuDisplaysByModuleIdAndUserId(ctx context.Context, moduleId int64) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.GetMenuDisplaysByModuleIdAndUserId(ctx, moduleId)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: GetMenuDisplaysByModuleIdAndUserId error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (m *MenuDisplayService) AssignMenuDisplaysToRole(ctx context.Context, req *request.AssignMenuDisplayToRoleDto) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.AssignMenuDisplaysToRole(ctx, req)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: AssignMenuDisplaysToRole error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (m *MenuDisplayService) UnassignMenuDisplaysFromRole(ctx context.Context, req *request.AssignMenuDisplayToRoleDto) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.UnassignMenuDisplaysFromRole(ctx, req)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: UnassignMenuDisplaysFromRole error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (m *MenuDisplayService) GetMenuDisplaysByRoleIds(ctx context.Context, roleIds []int64) (*response.BaseResponse, error) {
	res, err := m.menuDisplayClient.GetMenuDisplaysByRoleIds(ctx, roleIds)
	if err != nil {
		log.Error(ctx, "MenuDisplayService: GetMenuDisplaysByRoleIds error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewMenuDisplayService(menuDisplayClient port.IMenuDisplayClientPort) IMenuDisplayService {
	return &MenuDisplayService{
		menuDisplayClient: menuDisplayClient,
	}
}
