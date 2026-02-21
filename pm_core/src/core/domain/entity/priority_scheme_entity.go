/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type PrioritySchemeEntity struct {
	BaseEntity
	TenantID          int64   `json:"tenantId"`
	Name              string  `json:"name"`
	Description       *string `json:"description,omitempty"`
	DefaultPriorityID int64   `json:"defaultPriorityId"`

	Items []PrioritySchemeItemEntity `json:"items,omitempty"`
}
