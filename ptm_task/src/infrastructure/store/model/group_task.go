/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type GroupTaskModel struct {
	BaseModel
	Title          string `gorm:"not null" json:"title"`
	Description    string `gorm:"not null" json:"description"`
	Priority       string `gorm:"type:text" json:"priority"`
	Status         string `gorm:"not null" json:"status"`
	TotalTasks     int    `gorm:"default:0" json:"totalTasks"`
	CompletedTasks int    `gorm:"default:0" json:"completedTasks"`
	OrdinalNumber  int    `gorm:"default:0" json:"ordinalNumber"`
	ActiveStatus   string `gorm:"default:ACTIVE" json:"activeStatus"`
	IsDefault      bool   `gorm:"default:false" json:"isDefault"`
	ProjectID      int64  `gorm:"not null;index" json:"projectId"`
}

func (GroupTaskModel) TableName() string {
	return "group_tasks"
}
