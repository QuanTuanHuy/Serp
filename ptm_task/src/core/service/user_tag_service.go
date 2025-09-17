/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	port "github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type IUserTagService interface {
	CreateUserTag(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateTagDTO) (*entity.UserTagEntity, error)
	UpdateUserTag(ctx context.Context, tx *gorm.DB, userTag *entity.UserTagEntity) (*entity.UserTagEntity, error)
	DeleteUserTag(ctx context.Context, tx *gorm.DB, tagID int64) error
	GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.UserTagEntity, error)
	GetTagByID(ctx context.Context, tagID int64) (*entity.UserTagEntity, error)
	GetTagByUserIDAndName(ctx context.Context, userID int64, name string) (*entity.UserTagEntity, error)
}

type UserTagService struct {
	userTagPort port.IUserTagPort
}

func (u *UserTagService) GetTagByID(ctx context.Context, tagID int64) (*entity.UserTagEntity, error) {
	tag, err := u.userTagPort.GetUserTagByID(ctx, tagID)
	if err != nil {
		log.Error(ctx, "Failed to get user tag by ID: ", err)
		return nil, err
	}
	if tag == nil {
		return nil, errors.New(constant.TagNotFound)
	}
	return tag, nil
}

func (u *UserTagService) CreateUserTag(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateTagDTO) (*entity.UserTagEntity, error) {
	userTag := mapper.CreateUserTagMapper(req, userID)
	userTag, err := u.userTagPort.CreateUserTag(ctx, tx, userTag)
	if err != nil {
		log.Error(ctx, "Failed to create user tag: ", err)
		return nil, err
	}
	return userTag, nil
}

func (u *UserTagService) UpdateUserTag(ctx context.Context, tx *gorm.DB, userTag *entity.UserTagEntity) (*entity.UserTagEntity, error) {
	userTag, err := u.userTagPort.UpdateUserTag(ctx, tx, userTag.ID, userTag)
	if err != nil {
		log.Error(ctx, "Failed to update user tag: ", err)
		return nil, err
	}
	return userTag, nil
}

func (u *UserTagService) DeleteUserTag(ctx context.Context, tx *gorm.DB, tagID int64) error {
	err := u.userTagPort.DeleteUserTag(ctx, tx, tagID)
	if err != nil {
		log.Error(ctx, "Failed to delete user tag: ", err)
		return err
	}
	return nil
}

func (u *UserTagService) GetTagByUserIDAndName(ctx context.Context, userID int64, name string) (*entity.UserTagEntity, error) {
	userTag, err := u.userTagPort.GetTagByUserIDAndName(ctx, userID, name)
	if err != nil {
		log.Error(ctx, "Failed to get user tag by userID and name: ", err)
		return nil, err
	}
	if userTag == nil {
		return nil, errors.New(constant.TagNotFound)
	}
	return userTag, nil
}

func (u *UserTagService) GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.UserTagEntity, error) {
	userTags, err := u.userTagPort.GetUserTagsByUserID(ctx, userID)
	if err != nil {
		log.Error(ctx, "Failed to get user tags by userID: ", err)
		return nil, err
	}
	return userTags, nil
}

func NewUserTagService(userTagPort port.IUserTagPort) IUserTagService {
	return &UserTagService{
		userTagPort: userTagPort,
	}
}
