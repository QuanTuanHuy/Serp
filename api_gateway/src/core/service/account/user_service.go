/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
)

type IUserService interface {
	GetMyProfile(ctx context.Context) (*response.BaseResponse, error)
}

type UserService struct {
	userClient port.IUserClientPort
}

func (u *UserService) GetMyProfile(ctx context.Context) (*response.BaseResponse, error) {
	res, err := u.userClient.GetMyProfile(ctx)
	if err != nil {
		log.Error(ctx, "UserService: GetMyProfile error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewUserService(userClient port.IUserClientPort) IUserService {
	return &UserService{
		userClient: userClient,
	}
}
