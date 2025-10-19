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

type IAuthClientPort interface {
	Register(ctx context.Context, req *request.RegisterDTO) (*response.BaseResponse, error)
	Login(ctx context.Context, req *request.LoginDTO) (*response.BaseResponse, error)
	GetToken(ctx context.Context, req *request.LoginDTO) (*response.BaseResponse, error)
	RefreshToken(ctx context.Context, req *request.RefreshTokenDTO) (*response.BaseResponse, error)
	RevokeToken(ctx context.Context, req *request.RefreshTokenDTO) (*response.BaseResponse, error)
}
