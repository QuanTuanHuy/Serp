/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type NoteEntity struct {
	BaseEntity
	Name               string            `json:"name"`
	SummaryDisplayText string            `json:"summaryDisplayText"`
	FileID             *string           `json:"fileId"`
	FileName           *string           `json:"fileName"`
	FilePath           *string           `json:"filePath"`
	FileStatus         *string           `json:"fileStatus"`
	IsLock             bool              `json:"isLock"`
	ActiveStatus       enum.ActiveStatus `json:"activeStatus"`
	NotePassword       *string           `json:"notePassword"`
	PasswordSuggestion *string           `json:"passwordSuggestion"`
	OwnerID            int64             `json:"ownerId"`
}
