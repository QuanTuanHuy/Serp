/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type CommentModel struct {
	BaseModel
	Content      string `gorm:"not null" json:"content"`
	TaskID       int64  `gorm:"not null;index" json:"taskId"`
	ActiveStatus string `gorm:"default:ACTIVE" json:"activeStatus"`
}

func (CommentModel) TableName() string {
	return "comments"
}
