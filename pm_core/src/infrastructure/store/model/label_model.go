/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type LabelModel struct {
	BaseModel

	ProjectID int64  `gorm:"column:project_id;not null;index:idx_label_project"`
	Name      string `gorm:"column:name;type:varchar(100);not null"`
	Color     string `gorm:"column:color;type:varchar(20);not null"`
}

func (LabelModel) TableName() string {
	return "labels"
}
