/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type INotePort interface {
	CreateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) error

	GetNoteByID(ctx context.Context, id int64) (*entity.NoteEntity, error)
	GetNotesByTaskID(ctx context.Context, taskID int64, filter *NoteFilter) ([]*entity.NoteEntity, error)
	GetNotesByProjectID(ctx context.Context, projectID int64, filter *NoteFilter) ([]*entity.NoteEntity, error)
	GetNotesByUserID(ctx context.Context, userID int64, filter *NoteFilter) ([]*entity.NoteEntity, error)
	CountNotesByUserID(ctx context.Context, userID int64, filter *NoteFilter) (int64, error)

	UpdateNote(ctx context.Context, tx *gorm.DB, note *entity.NoteEntity) error
	UpdateNotePin(ctx context.Context, tx *gorm.DB, noteID int64, isPinned bool) error

	SoftDeleteNote(ctx context.Context, tx *gorm.DB, noteID int64) error
	SoftDeleteNotesByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error
	SoftDeleteNotesByProjectID(ctx context.Context, tx *gorm.DB, projectID int64) error

	SearchNotes(ctx context.Context, userID int64, searchQuery string, filter *NoteFilter) ([]*entity.NoteEntity, error)
}

type NoteFilter struct {
	TaskID    *int64
	ProjectID *int64

	IsPinned     *bool
	ActiveStatus *string

	HasAttachments *bool

	CreatedFrom *int64
	CreatedTo   *int64

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

func NewNoteFilter() *NoteFilter {
	return &NoteFilter{
		SortBy:    "created_at",
		SortOrder: "DESC",
		Limit:     20,
		Offset:    0,
	}
}
