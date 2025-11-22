/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type ProjectModel struct {
	BaseModel
	Name         string `gorm:"not null" json:"name"`
	Description  string `gorm:"not null" json:"description"`
	Color        string `gorm:"not null" json:"color"`
	Status       string `gorm:"not null" json:"status"`
	OwnerID      int64  `gorm:"not null;index" json:"ownerId"`
	IsDefault    bool   `gorm:"default:false" json:"isDefault"`
	ActiveStatus string `gorm:"default:ACTIVE" json:"activeStatus"`
}

func (ProjectModel) TableName() string {
	return "projects"
}
