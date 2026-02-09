/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type BoardColumnModel struct {
	BaseModel

	BoardID       int64   `gorm:"not null;index:idx_column_board"`
	Name          string  `gorm:"type:varchar(200);not null"`
	Position      int     `gorm:"not null;default:0"`
	StatusMapping *string `gorm:"type:varchar(20)"`
	WipLimit      int     `gorm:"not null;default:0"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (BoardColumnModel) TableName() string {
	return "board_columns"
}
