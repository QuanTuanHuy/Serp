/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type PrioritySchemeModel struct {
	BaseModel
	TenantID          int64   `gorm:"column:tenant_id;not null" json:"tenantId"`
	Name              string  `gorm:"column:name;type:varchar(255);not null" json:"name"`
	Description       *string `gorm:"column:description" json:"description,omitempty"`
	DefaultPriorityID int64   `gorm:"column:default_priority_id;not null" json:"defaultPriorityId"`
}

func (PrioritySchemeModel) TableName() string {
	return "priority_schemes"
}
