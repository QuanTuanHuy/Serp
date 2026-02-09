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

type IBoardColumnPort interface {
	CreateColumn(ctx context.Context, tx *gorm.DB, column *entity.BoardColumnEntity) (*entity.BoardColumnEntity, error)
	GetColumnByID(ctx context.Context, id int64) (*entity.BoardColumnEntity, error)
	GetColumnsByBoardID(ctx context.Context, boardID int64) ([]*entity.BoardColumnEntity, error)
	UpdateColumn(ctx context.Context, tx *gorm.DB, column *entity.BoardColumnEntity) error
	SoftDeleteColumn(ctx context.Context, tx *gorm.DB, columnID int64) error
}
