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

type IPermissionService interface {
CreatePermission(ctx context.Context, req *request.CreatePermissionDto) (*response.BaseResponse, error)
GetAllPermissions(ctx context.Context) (*response.BaseResponse, error)
}

type PermissionService struct {
permissionClient port.IPermissionClientPort
}

func (p *PermissionService) CreatePermission(ctx context.Context, req *request.CreatePermissionDto) (*response.BaseResponse, error) {
res, err := p.permissionClient.CreatePermission(ctx, req)
if err != nil {
log.Error(ctx, "PermissionService: CreatePermission error: ", err.Error())
return nil, err
}
return res, nil
}

func (p *PermissionService) GetAllPermissions(ctx context.Context) (*response.BaseResponse, error) {
res, err := p.permissionClient.GetAllPermissions(ctx)
if err != nil {
log.Error(ctx, "PermissionService: GetAllPermissions error: ", err.Error())
return nil, err
}
return res, nil
}

func NewPermissionService(permissionClient port.IPermissionClientPort) IPermissionService {
return &PermissionService{
permissionClient: permissionClient,
}
}
