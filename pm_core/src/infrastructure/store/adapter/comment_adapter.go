/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/core/port/store"
	"github.com/serp/pm-core/src/infrastructure/store/mapper"
	"github.com/serp/pm-core/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type CommentAdapter struct {
	db     *gorm.DB
	mapper *mapper.CommentMapper
}

func NewCommentAdapter(db *gorm.DB) store.ICommentPort {
	return &CommentAdapter{
		db:     db,
		mapper: mapper.NewCommentMapper(),
	}
}

func (a *CommentAdapter) CreateComment(ctx context.Context, tx *gorm.DB, comment *entity.CommentEntity) (*entity.CommentEntity, error) {
	db := a.getDB(tx)
	commentModel := a.mapper.ToModel(comment)
	if err := db.WithContext(ctx).Create(commentModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create comment: %w", err)
	}
	return a.mapper.ToEntity(commentModel), nil
}

func (a *CommentAdapter) GetCommentByID(ctx context.Context, id int64) (*entity.CommentEntity, error) {
	var commentModel model.CommentModel
	if err := a.db.WithContext(ctx).First(&commentModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get comment by id: %w", err)
	}
	return a.mapper.ToEntity(&commentModel), nil
}

func (a *CommentAdapter) GetCommentsByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.CommentEntity, error) {
	var models []*model.CommentModel
	if err := a.db.WithContext(ctx).Where("work_item_id = ? AND active_status = ?", workItemID, "ACTIVE").
		Order("created_at DESC").Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get comments: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *CommentAdapter) UpdateComment(ctx context.Context, tx *gorm.DB, comment *entity.CommentEntity) error {
	db := a.getDB(tx)
	commentModel := a.mapper.ToModel(comment)
	if err := db.WithContext(ctx).Save(commentModel).Error; err != nil {
		return fmt.Errorf("failed to update comment: %w", err)
	}
	return nil
}

func (a *CommentAdapter) SoftDeleteComment(ctx context.Context, tx *gorm.DB, commentID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Model(&model.CommentModel{}).
		Where("id = ?", commentID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete comment: %w", err)
	}
	return nil
}

func (a *CommentAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
