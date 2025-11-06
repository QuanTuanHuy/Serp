/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type ProjectEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Title       string  `json:"title"`
	Description *string `json:"description,omitempty"`

	Status   string `json:"status"`
	Priority string `json:"priority"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	DeadlineMs  *int64 `json:"deadlineMs,omitempty"`

	ProgressPercentage int `json:"progressPercentage"`

	// UI Customization
	Color      *string `json:"color,omitempty"`
	Icon       *string `json:"icon,omitempty"`
	IsFavorite bool    `json:"isFavorite"`

	// Computed stats
	TotalTasks     int     `json:"totalTasks,omitempty"`
	CompletedTasks int     `json:"completedTasks,omitempty"`
	EstimatedHours float64 `json:"estimatedHours,omitempty"`
	ActualHours    float64 `json:"actualHours,omitempty"`
}

func NewProjectEntity() *ProjectEntity {
	return &ProjectEntity{
		Status:             "ACTIVE",
		Priority:           "MEDIUM",
		IsFavorite:         false,
		ProgressPercentage: 0,
	}
}

func (p *ProjectEntity) IsOverdue(currentTimeMs int64) bool {
	if p.DeadlineMs == nil {
		return false
	}
	return currentTimeMs > *p.DeadlineMs && p.Status != "COMPLETED" && p.Status != "ARCHIVED"
}

func (p *ProjectEntity) IsCompleted() bool {
	return p.Status == "COMPLETED"
}

func (p *ProjectEntity) IsActive() bool {
	return p.Status == "ACTIVE"
}

func (p *ProjectEntity) GetDeadlineRemainingMs(currentTimeMs int64) *int64 {
	if p.DeadlineMs == nil {
		return nil
	}
	remaining := *p.DeadlineMs - currentTimeMs
	return &remaining
}

func (p *ProjectEntity) CalculateProgressPercentage(totalTasks, completedTasks int) int {
	if totalTasks == 0 {
		return 0
	}
	return int(float64(completedTasks) / float64(totalTasks) * 100)
}

func (p *ProjectEntity) UpdateStats(totalTasks, completedTasks int, estimatedMin, actualMin int) {
	p.TotalTasks = totalTasks
	p.CompletedTasks = completedTasks
	p.ProgressPercentage = p.CalculateProgressPercentage(totalTasks, completedTasks)
	p.EstimatedHours = float64(estimatedMin) / 60.0
	p.ActualHours = float64(actualMin) / 60.0
}
