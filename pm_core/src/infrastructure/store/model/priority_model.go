/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type PriorityModel struct {
	BaseModel
	TenantID    int64   `gorm:"not null" json:"tenantId"`
	Name        string  `gorm:"type:varchar(100);not null" json:"name"`
	Description *string `json:"description,omitempty"`
	IconUrl     *string `json:"iconUrl,omitempty"`
	Color       *string `gorm:"type:varchar(20)" json:"color,omitempty"`
	Sequence    int     `gorm:"not null" json:"sequence"`
	IsSystem    bool    `gorm:"not null" json:"isSystem"`
}

func (PriorityModel) TableName() string {
	return "priorities"
}
