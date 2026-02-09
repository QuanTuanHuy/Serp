/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type LabelModel struct {
	BaseModel

	ProjectID int64  `gorm:"not null;index:idx_label_project"`
	Name      string `gorm:"type:varchar(100);not null"`
	Color     string `gorm:"type:varchar(20);not null"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (LabelModel) TableName() string {
	return "labels"
}
