/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type IssueTypeSchemeEntity struct {
	BaseEntity
	TenantID           int64   `json:"tenantId"`
	Name               string  `json:"name"`
	Description        *string `json:"description,omitempty"`
	DefaultIssueTypeID int64   `json:"defaultIssueTypeId"`

	Items []IssueTypeSchemeItemEntity `json:"items,omitempty"`
}
