/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type ActivityLogEntity struct {
	BaseEntity

	ProjectID  int64  `json:"projectId"`
	WorkItemID *int64 `json:"workItemId,omitempty"`
	UserID     int64  `json:"userId"`

	EntityType string `json:"entityType"`
	EntityID   int64  `json:"entityId"`

	Action   string  `json:"action"`
	Field    *string `json:"field,omitempty"`
	OldValue *string `json:"oldValue,omitempty"`
	NewValue *string `json:"newValue,omitempty"`
}
