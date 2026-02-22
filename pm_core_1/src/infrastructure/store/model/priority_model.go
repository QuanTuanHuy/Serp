/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type PriorityModel struct {
	BaseModel
	TenantID    int64   `gorm:"column:tenant_id;not null" json:"tenantId"`
	Name        string  `gorm:"column:name;type:varchar(100);not null" json:"name"`
	Description *string `gorm:"column:description" json:"description,omitempty"`
	IconUrl     *string `gorm:"column:icon_url" json:"iconUrl,omitempty"`
	Color       *string `gorm:"column:color;type:varchar(20)" json:"color,omitempty"`
	Sequence    int     `gorm:"column:sequence;not null" json:"sequence"`
	IsSystem    bool    `gorm:"column:is_system;not null" json:"isSystem"`
}

func (PriorityModel) TableName() string {
	return "priorities"
}
