/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type IssueTypeModel struct {
	BaseModel
	TenantID       int64   `gorm:"column:tenant_id;not null" json:"tenantId"`
	TypeKey        string  `gorm:"column:type_key;type:varchar(20);not null" json:"typeKey"`
	Name           string  `gorm:"column:name;type:varchar(100);not null" json:"name"`
	Description    *string `gorm:"column:description" json:"description,omitempty"`
	IconUrl        *string `gorm:"column:icon_url" json:"iconUrl,omitempty"`
	HierarchyLevel int     `gorm:"column:hierarchy_level;not null" json:"hierarchyLevel"`
	IsSystem       bool    `gorm:"column:is_system;not null" json:"isSystem"`
}

func (IssueTypeModel) TableName() string {
	return "issue_types"
}
