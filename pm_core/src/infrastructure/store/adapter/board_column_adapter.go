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

type BoardColumnAdapter struct {
	db     *gorm.DB
	mapper *mapper.BoardColumnMapper
}

func NewBoardColumnAdapter(db *gorm.DB) store.IBoardColumnPort {
	return &BoardColumnAdapter{
		db:     db,
		mapper: mapper.NewBoardColumnMapper(),
	}
}

func (a *BoardColumnAdapter) CreateColumn(ctx context.Context, tx *gorm.DB, column *entity.BoardColumnEntity) (*entity.BoardColumnEntity, error) {
	db := a.getDB(tx)
	columnModel := a.mapper.ToModel(column)
	if err := db.WithContext(ctx).Create(columnModel).Error; err != nil {
		return nil, fmt.Errorf("failed to create board column: %w", err)
	}
	return a.mapper.ToEntity(columnModel), nil
}

func (a *BoardColumnAdapter) GetColumnByID(ctx context.Context, id int64) (*entity.BoardColumnEntity, error) {
	var columnModel model.BoardColumnModel
	if err := a.db.WithContext(ctx).First(&columnModel, id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get board column by id: %w", err)
	}
	return a.mapper.ToEntity(&columnModel), nil
}

func (a *BoardColumnAdapter) GetColumnsByBoardID(ctx context.Context, boardID int64) ([]*entity.BoardColumnEntity, error) {
	var models []*model.BoardColumnModel
	if err := a.db.WithContext(ctx).Where("board_id = ? AND active_status = ?", boardID, "ACTIVE").
		Order("position ASC").Find(&models).Error; err != nil {
		return nil, fmt.Errorf("failed to get board columns: %w", err)
	}
	return a.mapper.ToEntities(models), nil
}

func (a *BoardColumnAdapter) UpdateColumn(ctx context.Context, tx *gorm.DB, column *entity.BoardColumnEntity) error {
	db := a.getDB(tx)
	columnModel := a.mapper.ToModel(column)
	if err := db.WithContext(ctx).Save(columnModel).Error; err != nil {
		return fmt.Errorf("failed to update board column: %w", err)
	}
	return nil
}

func (a *BoardColumnAdapter) SoftDeleteColumn(ctx context.Context, tx *gorm.DB, columnID int64) error {
	db := a.getDB(tx)
	if err := db.WithContext(ctx).Model(&model.BoardColumnModel{}).
		Where("id = ?", columnID).
		Update("active_status", "DELETED").Error; err != nil {
		return fmt.Errorf("failed to soft delete board column: %w", err)
	}
	return nil
}

func (a *BoardColumnAdapter) getDB(tx *gorm.DB) *gorm.DB {
	if tx != nil {
		return tx
	}
	return a.db
}
