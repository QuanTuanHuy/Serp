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

type IModuleAccessService interface {
CanOrganizationAccessModule(ctx context.Context, organizationId int64, moduleId int64) (*response.BaseResponse, error)
GetAccessibleModulesForOrganization(ctx context.Context, organizationId int64) (*response.BaseResponse, error)
AssignUserToModule(ctx context.Context, organizationId int64, req *request.AssignUserToModuleRequest) (*response.BaseResponse, error)
BulkAssignUsersToModule(ctx context.Context, req *request.BulkAssignUsersRequest) (*response.BaseResponse, error)
RevokeUserAccessToModule(ctx context.Context, organizationId int64, moduleId int64, userId int64) (*response.BaseResponse, error)
GetUsersWithAccessToModule(ctx context.Context, organizationId int64, moduleId int64) (*response.BaseResponse, error)
GetModulesAccessibleByUser(ctx context.Context, organizationId int64) (*response.BaseResponse, error)
}

type ModuleAccessService struct {
moduleAccessClient port.IModuleAccessClientPort
}

func (m *ModuleAccessService) CanOrganizationAccessModule(ctx context.Context, organizationId int64, moduleId int64) (*response.BaseResponse, error) {
res, err := m.moduleAccessClient.CanOrganizationAccessModule(ctx, organizationId, moduleId)
if err != nil {
log.Error(ctx, "ModuleAccessService: CanOrganizationAccessModule error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleAccessService) GetAccessibleModulesForOrganization(ctx context.Context, organizationId int64) (*response.BaseResponse, error) {
res, err := m.moduleAccessClient.GetAccessibleModulesForOrganization(ctx, organizationId)
if err != nil {
log.Error(ctx, "ModuleAccessService: GetAccessibleModulesForOrganization error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleAccessService) AssignUserToModule(ctx context.Context, organizationId int64, req *request.AssignUserToModuleRequest) (*response.BaseResponse, error) {
res, err := m.moduleAccessClient.AssignUserToModule(ctx, organizationId, req)
if err != nil {
log.Error(ctx, "ModuleAccessService: AssignUserToModule error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleAccessService) BulkAssignUsersToModule(ctx context.Context, req *request.BulkAssignUsersRequest) (*response.BaseResponse, error) {
res, err := m.moduleAccessClient.BulkAssignUsersToModule(ctx, req)
if err != nil {
log.Error(ctx, "ModuleAccessService: BulkAssignUsersToModule error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleAccessService) RevokeUserAccessToModule(ctx context.Context, organizationId int64, moduleId int64, userId int64) (*response.BaseResponse, error) {
res, err := m.moduleAccessClient.RevokeUserAccessToModule(ctx, organizationId, moduleId, userId)
if err != nil {
log.Error(ctx, "ModuleAccessService: RevokeUserAccessToModule error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleAccessService) GetUsersWithAccessToModule(ctx context.Context, organizationId int64, moduleId int64) (*response.BaseResponse, error) {
res, err := m.moduleAccessClient.GetUsersWithAccessToModule(ctx, organizationId, moduleId)
if err != nil {
log.Error(ctx, "ModuleAccessService: GetUsersWithAccessToModule error: ", err.Error())
return nil, err
}
return res, nil
}

func (m *ModuleAccessService) GetModulesAccessibleByUser(ctx context.Context, organizationId int64) (*response.BaseResponse, error) {
res, err := m.moduleAccessClient.GetModulesAccessibleByUser(ctx, organizationId)
if err != nil {
log.Error(ctx, "ModuleAccessService: GetModulesAccessibleByUser error: ", err.Error())
return nil, err
}
return res, nil
}

func NewModuleAccessService(moduleAccessClient port.IModuleAccessClientPort) IModuleAccessService {
return &ModuleAccessService{
moduleAccessClient: moduleAccessClient,
}
}
