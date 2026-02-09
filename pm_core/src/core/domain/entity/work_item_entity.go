/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"slices"

	"github.com/serp/pm-core/src/core/domain/enum"
)

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

	// Denormalized
	HasChildren         bool `json:"hasChildren"`
	ChildCount          int  `json:"childCount,omitempty"`
	CompletedChildCount int  `json:"completedChildCount,omitempty"`
	CommentCount        int  `json:"commentCount,omitempty"`
}

func AllowedChildTypes(parentType string) []string {
	switch parentType {
	case string(enum.WorkItemEpic):
		return []string{string(enum.WorkItemStory)}
	case string(enum.WorkItemStory):
		return []string{string(enum.WorkItemTask), string(enum.WorkItemBug)}
	case string(enum.WorkItemTask), string(enum.WorkItemBug):
		return []string{string(enum.WorkItemSubtask)}
	default:
		return nil
	}
}

func ValidateParentChildType(parentType, childType string) bool {
	allowed := AllowedChildTypes(parentType)
	return slices.Contains(allowed, childType)
}
