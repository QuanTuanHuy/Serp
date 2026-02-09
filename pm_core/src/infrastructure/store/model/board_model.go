/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type BoardModel struct {
	BaseModel

	ProjectID int64  `gorm:"not null;index:idx_board_project"`
	Name      string `gorm:"type:varchar(200);not null"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (BoardModel) TableName() string {
	return "boards"
}
