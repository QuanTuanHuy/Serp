/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/pm-core/src/core/domain/entity"
	"gorm.io/gorm"
)

type ICommentPort interface {
	CreateComment(ctx context.Context, tx *gorm.DB, comment *entity.CommentEntity) (*entity.CommentEntity, error)
	GetCommentByID(ctx context.Context, id int64) (*entity.CommentEntity, error)
	GetCommentsByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.CommentEntity, error)
	UpdateComment(ctx context.Context, tx *gorm.DB, comment *entity.CommentEntity) error
	SoftDeleteComment(ctx context.Context, tx *gorm.DB, commentID int64) error
}
