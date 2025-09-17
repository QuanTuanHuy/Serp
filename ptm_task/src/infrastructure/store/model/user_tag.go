/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type UserTagModel struct {
	BaseModel
	Name         string  `gorm:"not null;size:255" json:"name"`
	Color        string  `gorm:"not null;size:50" json:"color"`
	Weight       float64 `gorm:"default:0" json:"weight"`
	ActiveStatus string  `gorm:"default:ACTIVE" json:"activeStatus"`
	UserID       int64   `gorm:"not null;index" json:"userId"`
}

func (UserTagModel) TableName() string {
	return "user_tags"
}
