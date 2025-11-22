/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
)

func ToNoteEntity(note *model.NoteModel) *entity.NoteEntity {
	if note == nil {
		return nil
	}

	return &entity.NoteEntity{
		BaseEntity: entity.BaseEntity{
			ID:        note.ID,
			CreatedAt: note.CreatedAt.UnixMilli(),
			UpdatedAt: note.UpdatedAt.UnixMilli(),
		},
		Name:               note.Name,
		SummaryDisplayText: note.SummaryDisplayText,
		FileID:             note.FileID,
		FileName:           note.FileName,
		FilePath:           note.FilePath,
		FileStatus:         note.FileStatus,
		IsLock:             note.IsLock,
		ActiveStatus:       enum.ActiveStatus(note.ActiveStatus),
		NotePassword:       note.NotePassword,
		PasswordSuggestion: note.PasswordSuggestion,
		OwnerID:            note.OwnerID,
	}
}

func ToNoteModel(note *entity.NoteEntity) *model.NoteModel {
	if note == nil {
		return nil
	}

	return &model.NoteModel{
		BaseModel: model.BaseModel{
			ID: note.ID,
		},
		Name:               note.Name,
		SummaryDisplayText: note.SummaryDisplayText,
		FileID:             note.FileID,
		FileName:           note.FileName,
		FilePath:           note.FilePath,
		FileStatus:         note.FileStatus,
		IsLock:             note.IsLock,
		ActiveStatus:       string(note.ActiveStatus),
		NotePassword:       note.NotePassword,
		PasswordSuggestion: note.PasswordSuggestion,
		OwnerID:            note.OwnerID,
	}
}

func ToNoteEntities(notes []*model.NoteModel) []*entity.NoteEntity {
	if notes == nil {
		return nil
	}

	entities := make([]*entity.NoteEntity, len(notes))
	for i, note := range notes {
		entities[i] = ToNoteEntity(note)
	}
	return entities
}
