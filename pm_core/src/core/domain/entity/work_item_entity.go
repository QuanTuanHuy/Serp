/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"errors"
	"slices"
	"strings"

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

	Assignments  []*WorkItemAssignmentEntity `json:"assignments,omitempty"`
	Children     []*WorkItemEntity           `json:"children,omitempty"`
	Labels       []*WorkItemLabelEntity      `json:"labels,omitempty"`
	Dependencies []*WorkItemDependencyEntity `json:"dependencies,omitempty"`
}

func NewWorkItemEntity() *WorkItemEntity {
	return &WorkItemEntity{
		Status:       string(enum.WorkItemTodo),
		Priority:     string(enum.PriorityMedium),
		ActiveStatus: string(enum.Active),
	}
}

func (w *WorkItemEntity) Validate() error {
	if strings.TrimSpace(w.Title) == "" {
		return errors.New("work item title is required")
	}
	if !enum.WorkItemType(w.Type).IsValid() {
		return errors.New("invalid work item type")
	}
	if !enum.WorkItemPriority(w.Priority).IsValid() {
		return errors.New("invalid work item priority")
	}
	if w.StoryPoints != nil && *w.StoryPoints < 0 {
		return errors.New("story points must be non-negative")
	}
	if w.EstimatedHours != nil && *w.EstimatedHours < 0 {
		return errors.New("estimated hours must be non-negative")
	}
	if err := w.ValidateDateRange(); err != nil {
		return err
	}
	return nil
}

func (w *WorkItemEntity) ValidateDateRange() error {
	if w.StartDateMs != nil && w.DueDateMs != nil && *w.StartDateMs > *w.DueDateMs {
		return errors.New("start date must be before due date")
	}
	return nil
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

func (w *WorkItemEntity) CanHaveChildOfType(childType string) bool {
	return ValidateParentChildType(w.Type, childType)
}

func (w *WorkItemEntity) IsEpic() bool {
	return enum.WorkItemType(w.Type) == enum.WorkItemEpic
}

func (w *WorkItemEntity) IsStory() bool {
	return enum.WorkItemType(w.Type) == enum.WorkItemStory
}

func (w *WorkItemEntity) IsTask() bool {
	return enum.WorkItemType(w.Type) == enum.WorkItemTask
}

func (w *WorkItemEntity) IsBug() bool {
	return enum.WorkItemType(w.Type) == enum.WorkItemBug
}

func (w *WorkItemEntity) IsSubtask() bool {
	return enum.WorkItemType(w.Type) == enum.WorkItemSubtask
}

func (w *WorkItemEntity) IsTopLevel() bool {
	return w.ParentID == nil
}

func (w *WorkItemEntity) IsCompleted() bool {
	return enum.WorkItemStatus(w.Status).IsCompleted()
}

func (w *WorkItemEntity) IsCancelled() bool {
	return enum.WorkItemStatus(w.Status).IsCancelled()
}

func (w *WorkItemEntity) IsTerminal() bool {
	return enum.WorkItemStatus(w.Status).IsTerminal()
}

func (w *WorkItemEntity) IsOverdue(nowMs int64) bool {
	if w.DueDateMs == nil {
		return false
	}
	return *w.DueDateMs < nowMs && !w.IsCompleted() && !w.IsCancelled()
}

func (w *WorkItemEntity) GetPriorityScore() int {
	return enum.WorkItemPriority(w.Priority).GetScore()
}

func (w *WorkItemEntity) RecalculateChildCounts() {
	w.ChildCount = len(w.Children)
	w.HasChildren = w.ChildCount > 0

	completed := 0
	for _, child := range w.Children {
		if child.IsCompleted() {
			completed++
		}
	}
	w.CompletedChildCount = completed
}

func (w *WorkItemEntity) IncrementCommentCount() {
	w.CommentCount++
}

func (w *WorkItemEntity) DecrementCommentCount() {
	if w.CommentCount > 0 {
		w.CommentCount--
	}
}

func (w *WorkItemEntity) IsInBacklog() bool {
	return w.SprintID == nil
}

func (w *WorkItemEntity) GetStoryPointsValue() int {
	if w.StoryPoints == nil {
		return 0
	}
	return *w.StoryPoints
}
