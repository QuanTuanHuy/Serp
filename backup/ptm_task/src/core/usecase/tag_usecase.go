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
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type ITagUsecase interface {
	CreateTag(ctx context.Context, userID int64, req *request.CreateTagDTO) (*entity.TagEntity, error)
	UpdateTag(ctx context.Context, userID, tagID int64, req *request.UpdateTagDTO) (*entity.TagEntity, error)
	DeleteTag(ctx context.Context, userID, tagID int64) error
	GetTagByID(ctx context.Context, userID, tagID int64) (*entity.TagEntity, error)
	GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.TagEntity, error)

	TagResource(ctx context.Context, userID, tagID int64, resourceType enum.ResourceType, resourceID int64) error
	TagResourcesBatch(ctx context.Context, userID, tagID int64, resourceType enum.ResourceType, resourceIDs []int64) error
	RemoveTagFromResource(ctx context.Context, userID, tagID int64, resourceType enum.ResourceType, resourceID int64) error
	GetTagsForResource(ctx context.Context, userID int64, resourceType enum.ResourceType, resourceID int64) ([]*entity.TagEntity, error)
}

type TagUseCase struct {
	tagService    service.ITagService
	taggedService service.ITaggedItemService
	txService     service.ITransactionService
}

func (u *TagUseCase) GetTagByID(ctx context.Context, userID int64, tagID int64) (*entity.TagEntity, error) {
	tag, err := u.tagService.GetTagByID(ctx, tagID)
	if err != nil {
		return nil, err
	}
	if tag.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to access tag ", tagID)
		return nil, errors.New(constant.TagNotFound)
	}
	return tag, nil
}

func (u *TagUseCase) CreateTag(ctx context.Context, userID int64, req *request.CreateTagDTO) (*entity.TagEntity, error) {
	if existing, err := u.tagService.GetTagByUserIDAndName(ctx, userID, req.Name); err == nil && existing != nil {
		return nil, errors.New(constant.TagAlreadyInUse)
	}
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		tag, err := u.tagService.CreateTag(ctx, tx, userID, req)
		if err != nil {
			return nil, err
		}
		return tag, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.TagEntity), nil
}

func (u *TagUseCase) DeleteTag(ctx context.Context, userID int64, tagID int64) error {
	tag, err := u.tagService.GetTagByID(ctx, tagID)
	if err != nil {
		return err
	}
	if tag.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to delete tag ", tagID)
		return errors.New(constant.DeleteTagForbidden)
	}
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.tagService.DeleteTag(ctx, tx, tagID)
	})
}

func (u *TagUseCase) GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.TagEntity, error) {
	return u.tagService.GetTagsByUserID(ctx, userID)
}

func (u *TagUseCase) UpdateTag(ctx context.Context, userID int64, tagID int64, req *request.UpdateTagDTO) (*entity.TagEntity, error) {
	tag, err := u.tagService.GetTagByID(ctx, tagID)
	if err != nil {
		return nil, err
	}
	if tag.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to update tag ", tagID)
		return nil, errors.New(constant.UpdateTagForbidden)
	}
	updated := mapper.UpdateTagMapper(tag, req)
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		ut, err := u.tagService.UpdateTag(ctx, tx, updated)
		if err != nil {
			return nil, err
		}
		return ut, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.TagEntity), nil
}

func (u *TagUseCase) TagResource(ctx context.Context, userID, tagID int64, resourceType enum.ResourceType, resourceID int64) error {
	tag, err := u.tagService.GetTagByID(ctx, tagID)
	if err != nil {
		return err
	}
	if tag.UserID != userID {
		return errors.New(constant.UpdateTagForbidden)
	}
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		item := &entity.TaggedItemEntity{TagID: tagID, ResourceType: resourceType, ResourceID: resourceID}
		return u.taggedService.CreateTaggedItem(ctx, tx, item)
	})
}

func (u *TagUseCase) TagResourcesBatch(ctx context.Context, userID, tagID int64, resourceType enum.ResourceType, resourceIDs []int64) error {
	tag, err := u.tagService.GetTagByID(ctx, tagID)
	if err != nil {
		return err
	}
	if tag.UserID != userID {
		return errors.New(constant.UpdateTagForbidden)
	}
	items := make([]*entity.TaggedItemEntity, 0, len(resourceIDs))
	for _, id := range resourceIDs {
		items = append(items, &entity.TaggedItemEntity{TagID: tagID, ResourceType: resourceType, ResourceID: id})
	}
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.taggedService.CreateTaggedItems(ctx, tx, items)
	})
}

func (u *TagUseCase) RemoveTagFromResource(ctx context.Context, userID, tagID int64, resourceType enum.ResourceType, resourceID int64) error {
	tag, err := u.tagService.GetTagByID(ctx, tagID)
	if err != nil {
		return err
	}
	if tag.UserID != userID {
		return errors.New(constant.UpdateTagForbidden)
	}
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.taggedService.DeleteTagFromResource(ctx, tx, tagID, resourceType, resourceID)
	})
}

func (u *TagUseCase) GetTagsForResource(ctx context.Context, userID int64, resourceType enum.ResourceType, resourceID int64) ([]*entity.TagEntity, error) {
	return u.taggedService.GetTagsForResource(ctx, userID, resourceType, resourceID)
}

func NewTagUseCase(tagService service.ITagService, taggedService service.ITaggedItemService, txService service.ITransactionService) ITagUsecase {
	return &TagUseCase{tagService: tagService, taggedService: taggedService, txService: txService}
}
