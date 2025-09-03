/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateNoteDTO struct {
	Name               string  `json:"name"`
	SummaryDisplayText string  `json:"summaryDisplayText"`
	FileID             *string `json:"fileId"`
	FileName           *string `json:"fileName"`
	FilePath           *string `json:"filePath"`
	FileStatus         *string `json:"fileStatus"`
	IsLock             bool    `json:"isLock"`
	ActiveStatus       *string `json:"activeStatus" validate:"omitempty,oneof=ACTIVE INACTIVE"`
	NotePassword       *string `json:"notePassword"`
	PasswordSuggestion *string `json:"passwordSuggestion"`
}
