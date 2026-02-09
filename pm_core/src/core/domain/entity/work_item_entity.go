/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type WorkItemEntity struct {
	BaseEntity

	ProjectID   int64   `json:"projectId"`
	ItemNumber  int     `json:"itemNumber"`
	Title       string  `json:"title"`
	Description *string `json:"description,omitempty"`

	Type     string `json:"type"`
	Status   string `json:"status"`
	Priority string `json:"priority"`

	ParentID      *int64 `json:"parentId,omitempty"`
	SprintID      *int64 `json:"sprintId,omitempty"`
	MilestoneID   *int64 `json:"milestoneId,omitempty"`
	BoardColumnID *int64 `json:"boardColumnId,omitempty"`

	ReporterID int64 `json:"reporterId"`

	StoryPoints    *int     `json:"storyPoints,omitempty"`
	EstimatedHours *float64 `json:"estimatedHours,omitempty"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	DueDateMs   *int64 `json:"dueDateMs,omitempty"`

	ActiveStatus string `json:"activeStatus"`
	Position     int    `json:"position"`
}
