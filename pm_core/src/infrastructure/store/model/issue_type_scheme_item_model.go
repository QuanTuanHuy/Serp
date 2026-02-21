/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type IssueTypeSchemeItemModel struct {
	BaseModel
	TenantID    int64 `gorm:"column:tenant_id;not null" json:"tenantId"`
	SchemeID    int64 `gorm:"column:scheme_id;not null" json:"schemeId"`
	IssueTypeID int64 `gorm:"column:issue_type_id;not null" json:"issueTypeId"`
	Sequence    int   `gorm:"column:sequence;not null" json:"sequence"`
}

func (IssueTypeSchemeItemModel) TableName() string {
	return "issue_type_scheme_items"
}
