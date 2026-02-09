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

	Status          string `json:"status"`
	ActiveStatus    string `json:"activeStatus"`
	Visibility      string `json:"visibility"`
	MethodologyType string `json:"methodologyType"`
	Priority        string `json:"priority"`

	StartDateMs     *int64 `json:"startDateMs,omitempty"`
	TargetEndDateMs *int64 `json:"targetEndDateMs,omitempty"`

	Color *string `json:"color,omitempty"`
	Icon  *string `json:"icon,omitempty"`

	DefaultBoardID    *int64 `json:"defaultBoardId,omitempty"`
	DefaultWorkflowID *int64 `json:"defaultWorkflowId,omitempty"`

	NextItemNumber int `json:"nextItemNumber"`

	// Denormalized stats
	TotalWorkItems     int `json:"totalWorkItems,omitempty"`
	CompletedWorkItems int `json:"completedWorkItems,omitempty"`
	ProgressPercentage int `json:"progressPercentage,omitempty"`
	TotalMembers       int `json:"totalMembers,omitempty"`

	CreatedBy int64 `json:"createdBy"`
}

func NewProjectEntity() *ProjectEntity {
	return &ProjectEntity{
		Status:          string(enum.ProjectPlanning),
		ActiveStatus:    string(enum.Active),
		Visibility:      string(enum.VisibilityPrivate),
		MethodologyType: string(enum.MethodologyKanban),
		Priority:        string(enum.ProjectPriorityMedium),
		NextItemNumber:  1,
	}
}

func (p *ProjectEntity) CalculateProgressPercentage() {
	if p.TotalWorkItems == 0 {
		p.ProgressPercentage = 0
		return
	}
	p.ProgressPercentage = (p.CompletedWorkItems * 100) / p.TotalWorkItems
}
