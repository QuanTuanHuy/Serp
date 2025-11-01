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

type ITagService interface {
	CreateTag(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateTagDTO) (*entity.TagEntity, error)
	UpdateTag(ctx context.Context, tx *gorm.DB, tag *entity.TagEntity) (*entity.TagEntity, error)
	DeleteTag(ctx context.Context, tx *gorm.DB, tagID int64) error
	GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.TagEntity, error)
	GetTagByID(ctx context.Context, tagID int64) (*entity.TagEntity, error)
	GetTagByUserIDAndName(ctx context.Context, userID int64, name string) (*entity.TagEntity, error)
}

type TagService struct {
	tagPort port.ITagPort
}

func (s *TagService) GetTagByID(ctx context.Context, tagID int64) (*entity.TagEntity, error) {
	tag, err := s.tagPort.GetTagByID(ctx, tagID)
	if err != nil {
		log.Error(ctx, "Failed to get tag by ID: ", err)
		return nil, err
	}
	if tag == nil {
		return nil, errors.New(constant.TagNotFound)
	}
	return tag, nil
}

func (s *TagService) CreateTag(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateTagDTO) (*entity.TagEntity, error) {
	tag := mapper.CreateTagMapper(req, userID)
	created, err := s.tagPort.CreateTag(ctx, tx, tag)
	if err != nil {
		log.Error(ctx, "Failed to create tag: ", err)
		return nil, err
	}
	return created, nil
}

func (s *TagService) UpdateTag(ctx context.Context, tx *gorm.DB, tag *entity.TagEntity) (*entity.TagEntity, error) {
	updated, err := s.tagPort.UpdateTag(ctx, tx, tag.ID, tag)
	if err != nil {
		log.Error(ctx, "Failed to update tag: ", err)
		return nil, err
	}
	return updated, nil
}

func (s *TagService) DeleteTag(ctx context.Context, tx *gorm.DB, tagID int64) error {
	if err := s.tagPort.DeleteTag(ctx, tx, tagID); err != nil {
		log.Error(ctx, "Failed to delete tag: ", err)
		return err
	}
	return nil
}

func (s *TagService) GetTagByUserIDAndName(ctx context.Context, userID int64, name string) (*entity.TagEntity, error) {
	tag, err := s.tagPort.GetTagByUserIDAndName(ctx, userID, name)
	if err != nil {
		log.Error(ctx, "Failed to get tag by userID and name: ", err)
		return nil, err
	}
	if tag == nil {
		return nil, errors.New(constant.TagNotFound)
	}
	return tag, nil
}

func (s *TagService) GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.TagEntity, error) {
	tags, err := s.tagPort.GetTagsByUserID(ctx, userID)
	if err != nil {
		log.Error(ctx, "Failed to get tags by userID: ", err)
		return nil, err
	}
	return tags, nil
}

func NewTagService(tagPort port.ITagPort) ITagService {
	return &TagService{tagPort: tagPort}
}
