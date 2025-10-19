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

type IModuleService interface {
CreateModule(ctx context.Context, req *request.CreateModuleDto) (*response.BaseResponse, error)
GetModuleById(ctx context.Context, moduleId int64) (*response.BaseResponse, error)
UpdateModule(ctx context.Context, moduleId int64, req *request.UpdateModuleDto) (*response.BaseResponse, error)
GetAllModules(ctx context.Context) (*response.BaseResponse, error)
UserRegisterModule(ctx context.Context, moduleId int64) (*response.BaseResponse, error)
GetMyModules(ctx context.Context) (*response.BaseResponse, error)
}

type ModuleService struct {
moduleClient port.IModuleClientPort
}

func (m *ModuleService) CreateModule(ctx context.Context, req *request.CreateModuleDto) (*response.BaseResponse, error) {
res, err := m.moduleClient.CreateModule(ctx, req)
if err != nil {
log.Error(ctx, "ModuleService: CreateModule error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleService) GetModuleById(ctx context.Context, moduleId int64) (*response.BaseResponse, error) {
res, err := m.moduleClient.GetModuleById(ctx, moduleId)
if err != nil {
log.Error(ctx, "ModuleService: GetModuleById error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleService) UpdateModule(ctx context.Context, moduleId int64, req *request.UpdateModuleDto) (*response.BaseResponse, error) {
res, err := m.moduleClient.UpdateModule(ctx, moduleId, req)
if err != nil {
log.Error(ctx, "ModuleService: UpdateModule error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleService) GetAllModules(ctx context.Context) (*response.BaseResponse, error) {
res, err := m.moduleClient.GetAllModules(ctx)
if err != nil {
log.Error(ctx, "ModuleService: GetAllModules error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleService) UserRegisterModule(ctx context.Context, moduleId int64) (*response.BaseResponse, error) {
res, err := m.moduleClient.UserRegisterModule(ctx, moduleId)
if err != nil {
log.Error(ctx, "ModuleService: UserRegisterModule error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleService) GetMyModules(ctx context.Context) (*response.BaseResponse, error) {
res, err := m.moduleClient.GetMyModules(ctx)
if err != nil {
log.Error(ctx, "ModuleService: GetMyModules error: ", err.Error())
return nil, err
}
return res, nil
}

func NewModuleService(moduleClient port.IModuleClientPort) IModuleService {
return &ModuleService{
moduleClient: moduleClient,
}
}
