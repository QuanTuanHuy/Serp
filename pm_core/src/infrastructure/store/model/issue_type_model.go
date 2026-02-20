/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type IssueTypeModel struct {
	BaseModel
	TenantID       int64   `gorm:"not null" json:"tenantId"`
	TypeKey        string  `gorm:"type:varchar(20);not null" json:"typeKey"`
	Name           string  `gorm:"type:varchar(100);not null" json:"name"`
	Description    *string `json:"description,omitempty"`
	IconUrl        *string `json:"iconUrl,omitempty"`
	HierarchyLevel int     `gorm:"not null" json:"hierarchyLevel"`
	IsSystem       bool    `gorm:"not null" json:"isSystem"`
}

func (IssueTypeModel) TableName() string {
	return "issue_types"
}
