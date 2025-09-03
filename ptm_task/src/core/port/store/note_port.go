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

type INotePort interface {
	CreateNode(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) (*entity.NoteEntity, error)
	GetNotesByUserID(ctx context.Context, userID int64) ([]*entity.NoteEntity, error)
	UpdateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) (*entity.NoteEntity, error)
	GetNoteByID(ctx context.Context, noteID int64) (*entity.NoteEntity, error)
	DeleteNote(ctx context.Context, tx *gorm.DB, noteID int64) error
}
