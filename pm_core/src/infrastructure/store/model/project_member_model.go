/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type ProjectMemberModel struct {
	BaseModel

	ProjectID int64     `gorm:"not null;index:idx_member_project_user,priority:1"`
	UserID    int64     `gorm:"not null;index:idx_member_project_user,priority:2"`
	Role      string    `gorm:"type:varchar(20);not null"`
	JoinedAt  time.Time `gorm:"not null"`
}

func (ProjectMemberModel) TableName() string {
	return "project_members"
}
