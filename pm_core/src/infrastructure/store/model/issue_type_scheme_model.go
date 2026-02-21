/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type IssueTypeSchemeModel struct {
	BaseModel
	TenantID           int64   `gorm:"column:tenant_id;not null" json:"tenantId"`
	Name               string  `gorm:"column:name;type:varchar(255);not null" json:"name"`
	Description        *string `gorm:"column:description" json:"description,omitempty"`
	DefaultIssueTypeID int64   `gorm:"column:default_issue_type_id;not null" json:"defaultIssueTypeId"`
}

func (IssueTypeSchemeModel) TableName() string {
	return "issue_type_schemes"
}
