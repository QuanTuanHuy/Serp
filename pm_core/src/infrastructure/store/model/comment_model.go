/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type CommentModel struct {
	BaseModel

	WorkItemID      int64  `gorm:"not null;index:idx_comment_workitem"`
	AuthorID        int64  `gorm:"not null"`
	Content         string `gorm:"type:text;not null"`
	ParentCommentID *int64 `gorm:"index:idx_comment_parent"`

	IsEdited   bool `gorm:"not null;default:false"`
	EditedAtMs *int64

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (CommentModel) TableName() string {
	return "comments"
}
