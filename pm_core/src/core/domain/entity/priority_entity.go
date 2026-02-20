/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type PriorityEntity struct {
	BaseEntity
	TenantID int64 `json:"tenantId"`

	Name        string  `json:"name"`
	Description *string `json:"description,omitempty"`
	IconUrl     *string `json:"iconUrl,omitempty"`
	Color       *string `json:"color,omitempty"`
	Sequence    int     `json:"sequence"`
	IsSystem    bool    `json:"isSystem"`
}
