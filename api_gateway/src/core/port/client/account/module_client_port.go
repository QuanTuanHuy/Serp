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

type IModuleClientPort interface {
	CreateModule(ctx context.Context, req *request.CreateModuleDto) (*response.BaseResponse, error)
	GetModuleById(ctx context.Context, moduleId int64) (*response.BaseResponse, error)
	GetRolesInModule(ctx context.Context, moduleId int64) (*response.BaseResponse, error)
	UpdateModule(ctx context.Context, moduleId int64, req *request.UpdateModuleDto) (*response.BaseResponse, error)
	GetAllModules(ctx context.Context) (*response.BaseResponse, error)
	UserRegisterModule(ctx context.Context, moduleId int64) (*response.BaseResponse, error)
	GetMyModules(ctx context.Context) (*response.BaseResponse, error)
}
