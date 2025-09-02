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

type IAuthService interface {
	Register(ctx context.Context, req *request.RegisterDTO) (*response.BaseResponse, error)
	Login(ctx context.Context, req *request.LoginDTO) (*response.BaseResponse, error)
}

type AuthService struct {
	authClient port.IAuthClientPort
}

func (a *AuthService) Login(ctx context.Context, req *request.LoginDTO) (*response.BaseResponse, error) {
	res, err := a.authClient.Login(ctx, req)
	if err != nil {
		log.Error(ctx, "AuthService: Login error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (a *AuthService) Register(ctx context.Context, req *request.RegisterDTO) (*response.BaseResponse, error) {
	res, err := a.authClient.Register(ctx, req)
	if err != nil {
		log.Error(ctx, "AuthService: Register error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewAuthService(authClient port.IAuthClientPort) IAuthService {
	return &AuthService{
		authClient: authClient,
	}
}
