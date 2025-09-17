/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
)

type IUserTagService interface {
	CreateUserTag(ctx context.Context, req *request.CreateUserTagRequest) (*response.BaseResponse, error)
	GetUserTags(ctx context.Context) (*response.BaseResponse, error)
	GetUserTagByID(ctx context.Context, tagID int64) (*response.BaseResponse, error)
	UpdateUserTag(ctx context.Context, tagID int64, req *request.UpdateUserTagRequest) (*response.BaseResponse, error)
	DeleteUserTag(ctx context.Context, tagID int64) (*response.BaseResponse, error)
}

type UserTagService struct {
	userTagClient port.IUserTagClientPort
}

func (u *UserTagService) CreateUserTag(ctx context.Context, req *request.CreateUserTagRequest) (*response.BaseResponse, error) {
	res, err := u.userTagClient.CreateUserTag(ctx, req)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error creating user tag: %v", err))
		return nil, fmt.Errorf("failed to create user tag: %w", err)
	}
	return res, nil
}

func (u *UserTagService) GetUserTags(ctx context.Context) (*response.BaseResponse, error) {
	res, err := u.userTagClient.GetUserTags(ctx)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error getting user tags: %v", err))
		return nil, fmt.Errorf("failed to get user tags: %w", err)
	}
	return res, nil
}

func (u *UserTagService) GetUserTagByID(ctx context.Context, tagID int64) (*response.BaseResponse, error) {
	res, err := u.userTagClient.GetUserTagByID(ctx, tagID)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error getting user tag by ID: %v", err))
		return nil, fmt.Errorf("failed to get user tag by ID: %w", err)
	}
	return res, nil
}

func (u *UserTagService) UpdateUserTag(ctx context.Context, tagID int64, req *request.UpdateUserTagRequest) (*response.BaseResponse, error) {
	res, err := u.userTagClient.UpdateUserTag(ctx, tagID, req)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error updating user tag: %v", err))
		return nil, fmt.Errorf("failed to update user tag: %w", err)
	}
	return res, nil
}

func (u *UserTagService) DeleteUserTag(ctx context.Context, tagID int64) (*response.BaseResponse, error) {
	res, err := u.userTagClient.DeleteUserTag(ctx, tagID)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Error deleting user tag: %v", err))
		return nil, fmt.Errorf("failed to delete user tag: %w", err)
	}
	return res, nil
}

func NewUserTagService(userTagClient port.IUserTagClientPort) IUserTagService {
	return &UserTagService{
		userTagClient: userTagClient,
	}
}
