/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type TaggedItemModel struct {
	BaseModel
	TagID        int64  `gorm:"not null;index" json:"tagId"`
	ResourceType string `gorm:"not null;size:50;index" json:"resourceType"`
	ResourceID   int64  `gorm:"not null;index" json:"resourceId"`
}

func (TaggedItemModel) TableName() string {
	return "tagged_items"
}
