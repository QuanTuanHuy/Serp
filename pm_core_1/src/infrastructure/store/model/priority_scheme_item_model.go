/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type PrioritySchemeItemModel struct {
	BaseModel
	TenantID   int64 `gorm:"column:tenant_id;not null" json:"tenantId"`
	SchemeID   int64 `gorm:"column:scheme_id;not null" json:"schemeId"`
	PriorityID int64 `gorm:"column:priority_id;not null" json:"priorityId"`
	Sequence   int   `gorm:"column:sequence;not null" json:"sequence"`
}

func (PrioritySchemeItemModel) TableName() string {
	return "priority_scheme_items"
}
