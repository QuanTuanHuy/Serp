/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type ProjectEntity struct {
	BaseEntity

	TenantID int64 `json:"tenantId"`

	Name        string  `json:"name"`
	Key         string  `json:"key"`
	Description *string `json:"description,omitempty"`

	Status       string `json:"status"`
	ActiveStatus string `json:"activeStatus"`
	Visibility   string `json:"visibility"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	DeadlineMs  *int64 `json:"deadlineMs,omitempty"`

	NextItemNumber int `json:"nextItemNumber"`

	// Computed stats
	TotalWorkItems     int `json:"totalWorkItems,omitempty"`
	CompletedWorkItems int `json:"completedWorkItems,omitempty"`
}

func NewProjectEntity() *ProjectEntity {
	return &ProjectEntity{
		Status:         string(enum.ProjectPlanning),
		ActiveStatus:   string(enum.Active),
		Visibility:     string(enum.VisibilityPrivate),
		NextItemNumber: 1,
	}
}
