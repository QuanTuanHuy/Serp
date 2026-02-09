/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type MilestoneEntity struct {
	BaseEntity

	ProjectID   int64   `json:"projectId"`
	Name        string  `json:"name"`
	Description *string `json:"description,omitempty"`

	DueDateMs *int64 `json:"dueDateMs,omitempty"`

	ActiveStatus string `json:"activeStatus"`
}
