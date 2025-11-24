/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
)

func ToNoteEntity(createNote *request.CreateNoteDTO) *entity.NoteEntity {
	return &entity.NoteEntity{
		Name:               createNote.Name,
		SummaryDisplayText: createNote.SummaryDisplayText,
		FileID:             createNote.FileID,
		FileName:           createNote.FileName,
		FilePath:           createNote.FilePath,
		FileStatus:         createNote.FileStatus,
		IsLock:             createNote.IsLock,
		ActiveStatus:       enum.ActiveStatus(*createNote.ActiveStatus),
		NotePassword:       createNote.NotePassword,
		PasswordSuggestion: createNote.PasswordSuggestion,
	}
}

func LockNote(lockNote *request.LockNoteDTO, note *entity.NoteEntity) *entity.NoteEntity {
	note.NotePassword = &lockNote.NotePassword
	note.PasswordSuggestion = &lockNote.PasswordSuggestion
	note.IsLock = true
	return note
}

func UnlockNote(note *entity.NoteEntity) *entity.NoteEntity {
	note.NotePassword = nil
	note.PasswordSuggestion = nil
	note.IsLock = false
	return note
}
