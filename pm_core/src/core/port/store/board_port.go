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

type IBoardPort interface {
	CreateBoard(ctx context.Context, tx *gorm.DB, board *entity.BoardEntity) (*entity.BoardEntity, error)
	GetBoardByID(ctx context.Context, id int64) (*entity.BoardEntity, error)
	GetBoardsByProjectID(ctx context.Context, projectID int64) ([]*entity.BoardEntity, error)
	UpdateBoard(ctx context.Context, tx *gorm.DB, board *entity.BoardEntity) error
	SoftDeleteBoard(ctx context.Context, tx *gorm.DB, boardID int64) error
}
