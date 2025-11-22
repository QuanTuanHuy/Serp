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

type NoteStoreAdapter struct {
	db *gorm.DB
}

func (n *NoteStoreAdapter) CreateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) (*entity.NoteEntity, error) {
	noteModel := mapper.ToNoteModel(note)
	if err := tx.WithContext(ctx).Create(noteModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToNoteEntity(noteModel), nil
}

func (n *NoteStoreAdapter) DeleteNote(ctx context.Context, tx *gorm.DB, noteID int64) error {
	if err := tx.WithContext(ctx).
		Where("id = ?", noteID).
		Delete(&model.NoteModel{}).Error; err != nil {
		return err
	}
	return nil
}

func (n *NoteStoreAdapter) GetNoteByID(ctx context.Context, noteID int64) (*entity.NoteEntity, error) {
	var note model.NoteModel
	if err := n.db.WithContext(ctx).
		Where("id = ?", noteID).
		First(&note).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToNoteEntity(&note), nil
}

func (n *NoteStoreAdapter) GetNotesByUserID(ctx context.Context, userID int64) ([]*entity.NoteEntity, error) {
	var notes []*model.NoteModel
	if err := n.db.WithContext(ctx).
		Where("owner_id = ?", userID).
		Find(&notes).Error; err != nil {
		return nil, err
	}
	return mapper.ToNoteEntities(notes), nil
}

func (n *NoteStoreAdapter) UpdateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) (*entity.NoteEntity, error) {
	noteModel := mapper.ToNoteModel(note)
	if err := tx.WithContext(ctx).Save(noteModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToNoteEntity(noteModel), nil
}

func NewNoteStoreAdapter(db *gorm.DB) port.INotePort {
	return &NoteStoreAdapter{db: db}
}
