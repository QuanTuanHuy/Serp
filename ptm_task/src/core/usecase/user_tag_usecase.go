/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type IUserTagUsecase interface {
	CreateUserTag(ctx context.Context, userID int64, req *request.CreateTagDTO) (*entity.UserTagEntity, error)
	UpdateUserTag(ctx context.Context, userID, tagID int64, req *request.UpdateTagDTO) (*entity.UserTagEntity, error)
	DeleteUserTag(ctx context.Context, userID, tagID int64) error
	GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.UserTagEntity, error)
}

type UserTagUseCase struct {
	userTagService service.IUserTagService
	txService      service.ITransactionService
}

func (u *UserTagUseCase) CreateUserTag(ctx context.Context, userID int64, req *request.CreateTagDTO) (*entity.UserTagEntity, error) {
	_, err := u.userTagService.GetTagByUserIDAndName(ctx, userID, req.Name)
	if err != nil {
		return nil, err
	}
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		userTag, err := u.userTagService.CreateUserTag(ctx, tx, userID, req)
		if err != nil {
			return nil, err
		}
		return userTag, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.UserTagEntity), nil
}

func (u *UserTagUseCase) DeleteUserTag(ctx context.Context, userID int64, tagID int64) error {
	tag, err := u.userTagService.GetTagByID(ctx, tagID)
	if err != nil {
		return err
	}
	if tag.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to delete tag ", tagID)
		return errors.New(constant.DeleteTagForbidden)
	}
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.userTagService.DeleteUserTag(ctx, tx, tagID)
	})
}

func (u *UserTagUseCase) GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.UserTagEntity, error) {
	return u.userTagService.GetTagsByUserID(ctx, userID)
}

func (u *UserTagUseCase) UpdateUserTag(ctx context.Context, userID int64, tagID int64, req *request.UpdateTagDTO) (*entity.UserTagEntity, error) {
	tag, err := u.userTagService.GetTagByID(ctx, tagID)
	if err != nil {
		return nil, err
	}
	if tag.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to update tag ", tagID)
		return nil, errors.New(constant.UpdateTagForbidden)
	}
	tag = mapper.UpdateUserTagMapper(tag, req)
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		updatedTag, err := u.userTagService.UpdateUserTag(ctx, tx, tag)
		if err != nil {
			return nil, err
		}
		return updatedTag, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.UserTagEntity), nil
}

func NewUserTagUseCase(userTagService service.IUserTagService, txService service.ITransactionService) IUserTagUsecase {
	return &UserTagUseCase{
		userTagService: userTagService,
		txService:      txService,
	}
}
