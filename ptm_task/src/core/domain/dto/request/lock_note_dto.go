/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type LockNoteDTO struct {
	NotePassword       string `json:"notePassword" validate:"required"`
	PasswordSuggestion string `json:"passwordSuggestion" validate:"required"`
}

type UnlockNoteDTO struct {
	NotePassword string `json:"notePassword" validate:"required"`
}
