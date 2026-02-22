/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type IssueTypeEntity struct {
	BaseEntity

	TenantID       int64   `json:"tenantId"`
	TypeKey        string  `json:"typeKey"`
	Name           string  `json:"name"`
	Description    *string `json:"description,omitempty"`
	IconUrl        *string `json:"iconUrl,omitempty"`
	HierarchyLevel int     `json:"hierarchyLevel"`
	IsSystem       bool    `json:"isSystem"`
}
