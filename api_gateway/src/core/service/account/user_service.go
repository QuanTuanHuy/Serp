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

type IUserService interface {
	GetMyProfile(ctx context.Context) (*response.BaseResponse, error)
	GetUsers(ctx context.Context, params *request.GetUserParams) (*response.BaseResponse, error)
	AssignRolesToUser(ctx context.Context, req *request.AssignRoleToUserDto) (*response.BaseResponse, error)
	UpdateUserInfo(ctx context.Context, userId int64, req *request.UpdateUserInfoRequest) (*response.BaseResponse, error)
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

func (u *UserService) GetUsers(ctx context.Context, params *request.GetUserParams) (*response.BaseResponse, error) {
	res, err := u.userClient.GetUsers(ctx, params)
	if err != nil {
		log.Error(ctx, "UserService: GetUsers error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (u *UserService) AssignRolesToUser(ctx context.Context, req *request.AssignRoleToUserDto) (*response.BaseResponse, error) {
	res, err := u.userClient.AssignRolesToUser(ctx, req)
	if err != nil {
		log.Error(ctx, "UserService: AssignRolesToUser error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (u *UserService) UpdateUserInfo(ctx context.Context, userId int64, req *request.UpdateUserInfoRequest) (*response.BaseResponse, error) {
	res, err := u.userClient.UpdateUserInfo(ctx, userId, req)
	if err != nil {
		log.Error(ctx, "UserService: UpdateUserInfo error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewUserService(userClient port.IUserClientPort) IUserService {
	return &UserService{
		userClient: userClient,
	}
}
