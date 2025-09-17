/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"time"

	"github.com/serp/ptm-task/src/core/domain/entity"
	port "github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type UserTagStoreAdapter struct {
	db *gorm.DB
}

func (u *UserTagStoreAdapter) CreateUserTag(ctx context.Context, tx *gorm.DB, userTag *entity.UserTagEntity) (*entity.UserTagEntity, error) {
	userTagModel := mapper.ToUserTagModel(userTag)
	if err := tx.WithContext(ctx).Create(userTagModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToUserTagEntity(userTagModel), nil
}

func (u *UserTagStoreAdapter) DeleteUserTag(ctx context.Context, tx *gorm.DB, ID int64) error {
	if err := tx.WithContext(ctx).
		Where("id = ?", ID).
		Delete(&model.UserTagModel{}).Error; err != nil {
		return err
	}
	return nil
}

func (u *UserTagStoreAdapter) GetTagByUserIDAndName(ctx context.Context, userID int64, name string) (*entity.UserTagEntity, error) {
	var userTag model.UserTagModel
	if err := u.db.WithContext(ctx).
		Where("user_id = ? AND name = ?", userID, name).
		First(&userTag).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToUserTagEntity(&userTag), nil
}

func (u *UserTagStoreAdapter) GetUserTagByID(ctx context.Context, ID int64) (*entity.UserTagEntity, error) {
	var userTag model.UserTagModel
	if err := u.db.WithContext(ctx).Where("id = ?", ID).
		First(&userTag).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToUserTagEntity(&userTag), nil
}

func (u *UserTagStoreAdapter) GetUserTagsByUserID(ctx context.Context, userID int64) ([]*entity.UserTagEntity, error) {
	var userTags []*model.UserTagModel
	if err := u.db.WithContext(ctx).Where("user_id = ?", userID).
		Find(&userTags).Error; err != nil {
		return nil, err
	}
	return mapper.ToUserTagEntityList(userTags), nil
}

func (u *UserTagStoreAdapter) UpdateUserTag(ctx context.Context, tx *gorm.DB, ID int64, userTag *entity.UserTagEntity) (*entity.UserTagEntity, error) {
	userTagModel := mapper.ToUserTagModel(userTag)
	if err := tx.WithContext(ctx).
		Model(&model.UserTagModel{}).
		Where("id = ?", ID).
		Updates(userTagModel).Error; err != nil {
		return nil, err
	}
	userTag.UpdatedAt = time.Now().UnixMilli()
	return userTag, nil
}

func NewUserTagStoreAdapter(db *gorm.DB) port.IUserTagPort {
	return &UserTagStoreAdapter{db: db}
}
