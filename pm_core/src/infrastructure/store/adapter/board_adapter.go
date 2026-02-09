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

type BoardAdapter struct {
	db     *gorm.DB
	mapper *mapper.BoardMapper
}

func NewBoardAdapter(db *gorm.DB) store.IBoardPort {
	return &BoardAdapter{
		db:     db,
		mapper: mapper.NewBoardMapper(),
	}
}

func (a *BoardAdapter) CreateBoard(ctx context.Context, tx *gorm.DB, board *entity.BoardEntity) (*entity.BoardEntity, error) {
	db := a.getDB(tx)
	boardModel := a.mapper.ToModel(board)
	if err := db.WithContext(ctx).Create(boardModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create board: %w", err)
	}
	return a.mapper.ToEntity(boardModel), nil
}

func (a *BoardAdapter) GetBoardByID(ctx context.Context, id int64) (*entity.BoardEntity, error) {
	var boardModel model.BoardModel
	if err := a.db.WithContext(ctx).First(&boardModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get board by id: %w", err)
	}
	return a.mapper.ToEntity(&boardModel), nil
}

func (a *BoardAdapter) GetBoardsByProjectID(ctx context.Context, projectID int64) ([]*entity.BoardEntity, error) {
	var models []*model.BoardModel
	if err := a.db.WithContext(ctx).Where("project_id = ? AND active_status = ?", projectID, "ACTIVE").Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get boards: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *BoardAdapter) UpdateBoard(ctx context.Context, tx *gorm.DB, board *entity.BoardEntity) error {
	db := a.getDB(tx)
	boardModel := a.mapper.ToModel(board)
	if err := db.WithContext(ctx).Save(boardModel).Error; err != nil {
		return fmt.Errorf("failed to update board: %w", err)
	}
	return nil
}

func (a *BoardAdapter) SoftDeleteBoard(ctx context.Context, tx *gorm.DB, boardID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Model(&model.BoardModel{}).
		Where("id = ?", boardID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete board: %w", err)
	}
	return nil
}

func (a *BoardAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
