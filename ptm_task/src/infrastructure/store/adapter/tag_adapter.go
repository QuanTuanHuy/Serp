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

type TagStoreAdapter struct {
	db *gorm.DB
}

func (a *TagStoreAdapter) CreateTag(ctx context.Context, tx *gorm.DB, tag *entity.TagEntity) (*entity.TagEntity, error) {
	m := mapper.ToTagModel(tag)
	if err := tx.WithContext(ctx).Create(m).Error; err != nil {
		return nil, err
	}
	e := mapper.ToTagEntity(m)
	return e, nil
}

func (a *TagStoreAdapter) UpdateTag(ctx context.Context, tx *gorm.DB, tagID int64, tag *entity.TagEntity) (*entity.TagEntity, error) {
	m := mapper.ToTagModel(tag)
	if err := tx.WithContext(ctx).
		Model(&model.TagModel{}).
		Where("id = ?", tagID).
		Updates(m).Error; err != nil {
		return nil, err
	}
	tag.UpdatedAt = time.Now().UnixMilli()
	return tag, nil
}

func (a *TagStoreAdapter) GetTagByID(ctx context.Context, ID int64) (*entity.TagEntity, error) {
	var m model.TagModel
	if err := a.db.WithContext(ctx).Where("id = ?", ID).First(&m).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	e := mapper.ToTagEntity(&m)
	return e, nil
}

func (a *TagStoreAdapter) GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.TagEntity, error) {
	var ms []*model.TagModel
	if err := a.db.WithContext(ctx).Where("user_id = ?", userID).Find(&ms).Error; err != nil {
		return nil, err
	}
	es := mapper.ToTagEntityList(ms)
	return es, nil
}

func (a *TagStoreAdapter) DeleteTag(ctx context.Context, tx *gorm.DB, ID int64) error {
	if err := tx.WithContext(ctx).
		Where("id = ?", ID).
		Delete(&model.TagModel{}).Error; err != nil {
		return err
	}
	return nil
}

func (a *TagStoreAdapter) GetTagByUserIDAndName(ctx context.Context, userID int64, name string) (*entity.TagEntity, error) {
	var m model.TagModel
	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND name = ?", userID, name).
		First(&m).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	e := mapper.ToTagEntity(&m)
	return e, nil
}

func NewTagStoreAdapter(db *gorm.DB) port.ITagPort {
	return &TagStoreAdapter{db: db}
}
