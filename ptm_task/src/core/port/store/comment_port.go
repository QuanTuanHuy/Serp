/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type ICommentPort interface {
	CreateComment(ctx context.Context, tx *gorm.DB, comment *entity.CommentEntity) (*entity.CommentEntity, error)
	UpdateComment(ctx context.Context, tx *gorm.DB, commentID int64, comment *entity.CommentEntity) (*entity.CommentEntity, error)
	DeleteComment(ctx context.Context, tx *gorm.DB, commentID int64) error
	GetCommentByID(ctx context.Context, commentID int64) (*entity.CommentEntity, error)
	GetCommentsByTaskID(ctx context.Context, taskID int64) ([]*entity.CommentEntity, error)
}
