/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type IssueTypeSchemeItemEntity struct {
	BaseEntity
	TenantID    int64 `json:"tenantId"`
	SchemeID    int64 `json:"schemeId"`
	IssueTypeID int64 `json:"issueTypeId"`
	Sequence    int   `json:"sequence"`
}
