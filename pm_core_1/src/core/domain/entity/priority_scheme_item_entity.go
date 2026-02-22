/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type PrioritySchemeItemEntity struct {
	BaseEntity
	TenantID   int64 `json:"tenantId"`
	SchemeID   int64 `json:"schemeId"`
	PriorityID int64 `json:"priorityId"`
	Sequence   int   `json:"sequence"`
}
