/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	port "github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type CommentStoreAdapter struct {
	db *gorm.DB
}

func (c *CommentStoreAdapter) CreateComment(ctx context.Context, tx *gorm.DB, comment *entity.CommentEntity) (*entity.CommentEntity, error) {
	var commentModel = mapper.ToCommentModel(comment)
	if err := tx.WithContext(ctx).Create(commentModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToCommentEntity(commentModel), nil
}

func (c *CommentStoreAdapter) GetCommentsByTaskID(ctx context.Context, taskID int64) ([]*entity.CommentEntity, error) {
	var comments []*model.CommentModel
	if err := c.db.WithContext(ctx).Where("task_id = ?", taskID).
		Find(&comments).Error; err != nil {
		return nil, err
	}
	return mapper.ToCommentEntities(comments), nil
}

func (c *CommentStoreAdapter) DeleteComment(ctx context.Context, tx *gorm.DB, commentID int64) error {
	if err := tx.WithContext(ctx).
		Where("id = ?", commentID).
		Delete(&model.CommentModel{}).Error; err != nil {
		return err
	}
	return nil
}

func (c *CommentStoreAdapter) UpdateComment(ctx context.Context, tx *gorm.DB, commentID int64, comment *entity.CommentEntity) (*entity.CommentEntity, error) {
	commentModel := mapper.ToCommentModel(comment)
	if err := tx.WithContext(ctx).
		Model(&model.CommentModel{}).
		Where("id = ?", commentID).
		Updates(commentModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToCommentEntity(commentModel), nil
}

func (c *CommentStoreAdapter) GetCommentByID(ctx context.Context, commentID int64) (*entity.CommentEntity, error) {
	var comment model.CommentModel
	if err := c.db.WithContext(ctx).
		Where("id = ?", commentID).
		First(&comment).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToCommentEntity(&comment), nil
}

func NewCommentStoreAdapter(db *gorm.DB) port.ICommentPort {
	return &CommentStoreAdapter{
		db: db,
	}
}
