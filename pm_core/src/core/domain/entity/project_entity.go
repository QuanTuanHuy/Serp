/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"errors"
	"strings"

	"github.com/serp/pm-core/src/core/domain/enum"
)

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

	// Denormalized
	TotalWorkItems     int `json:"totalWorkItems,omitempty"`
	CompletedWorkItems int `json:"completedWorkItems,omitempty"`
	ProgressPercentage int `json:"progressPercentage,omitempty"`
	TotalMembers       int `json:"totalMembers,omitempty"`

	CreatedBy int64 `json:"createdBy"`

	Labels []*LabelEntity `json:"labels,omitempty"`
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

func (p *ProjectEntity) Validate() error {
	if strings.TrimSpace(p.Name) == "" {
		return errors.New("project name is required")
	}
	if strings.TrimSpace(p.Key) == "" {
		return errors.New("project key is required")
	}
	if !enum.ProjectStatus(p.Status).IsValid() {
		return errors.New("invalid project status")
	}
	if !enum.ProjectPriority(p.Priority).IsValid() {
		return errors.New("invalid project priority")
	}
	if !enum.MethodologyType(p.MethodologyType).IsValid() {
		return errors.New("invalid methodology type")
	}
	if !enum.ProjectVisibility(p.Visibility).IsValid() {
		return errors.New("invalid project visibility")
	}
	if p.StartDateMs != nil && p.TargetEndDateMs != nil && *p.StartDateMs > *p.TargetEndDateMs {
		return errors.New("start date must be before target end date")
	}
	return nil
}

func (p *ProjectEntity) CanTransitionTo(targetStatus string) bool {
	return enum.ProjectStatus(p.Status).CanTransitionTo(enum.ProjectStatus(targetStatus))
}

func (p *ProjectEntity) IsArchived() bool {
	return enum.ProjectStatus(p.Status) == enum.ProjectArchived
}

func (p *ProjectEntity) IsActive() bool {
	return enum.ProjectStatus(p.Status).IsActive()
}

func (p *ProjectEntity) IsEditable() bool {
	return enum.ProjectStatus(p.Status).IsEditable()
}

func (p *ProjectEntity) IncrementItemNumber() int {
	current := p.NextItemNumber
	p.NextItemNumber++
	return current
}

func (p *ProjectEntity) CalculateProgressPercentage() {
	if p.TotalWorkItems == 0 {
		p.ProgressPercentage = 0
		return
	}
	p.ProgressPercentage = (p.CompletedWorkItems * 100) / p.TotalWorkItems
}

func (p *ProjectEntity) UpdateStatsForAddedWorkItem() {
	p.TotalWorkItems++
	p.CalculateProgressPercentage()
}

func (p *ProjectEntity) UpdateStatsForCompletedWorkItem() {
	p.CompletedWorkItems++
	p.CalculateProgressPercentage()
}

func (p *ProjectEntity) UpdateStatsForRemovedWorkItem(wasCompleted bool) {
	if p.TotalWorkItems > 0 {
		p.TotalWorkItems--
	}
	if wasCompleted && p.CompletedWorkItems > 0 {
		p.CompletedWorkItems--
	}
	p.CalculateProgressPercentage()
}

func (p *ProjectEntity) UpdateStatsForUncompleteWorkItem() {
	if p.CompletedWorkItems > 0 {
		p.CompletedWorkItems--
	}
	p.CalculateProgressPercentage()
}

func (p *ProjectEntity) IsPublic() bool {
	return enum.ProjectVisibility(p.Visibility) == enum.VisibilityPublic
}
