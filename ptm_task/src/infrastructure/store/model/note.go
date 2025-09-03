/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type NoteModel struct {
	BaseModel
	Name               string  `gorm:"not null" json:"name"`
	SummaryDisplayText string  `gorm:"not null" json:"summaryDisplayText"`
	FileID             *string `gorm:"type:text" json:"fileId"`
	FileName           *string `gorm:"type:text" json:"fileName"`
	FilePath           *string `gorm:"type:text" json:"filePath"`
	FileStatus         *string `gorm:"type:text" json:"fileStatus"`
	IsLock             bool    `gorm:"default:false" json:"isLock"`
	ActiveStatus       string  `gorm:"default:ACTIVE" json:"activeStatus"`
	NotePassword       *string `gorm:"type:text" json:"notePassword"`
	PasswordSuggestion *string `gorm:"type:text" json:"passwordSuggestion"`
	OwnerID            int64   `gorm:"not null;index" json:"ownerId"`
}

func (NoteModel) TableName() string {
	return "notes"
}
