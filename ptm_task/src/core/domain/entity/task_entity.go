/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type TaskEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Title       string  `json:"title"`
	Description *string `json:"description,omitempty"`

	Priority      string   `json:"priority"`
	PriorityScore *float64 `json:"priorityScore"`

	EstimatedDurationMin *int `json:"estimatedDurationMin,omitempty"`
	ActualDurationMin    *int `json:"actualDurationMin,omitempty"`
	IsDurationLearned    bool `json:"isDurationLearned"`

	PreferredStartDateMs *int64 `json:"preferredStartDateMs,omitempty"`
	DeadlineMs           *int64 `json:"deadlineMs,omitempty"`
	EarliestStartMs      *int64 `json:"earliestStartMs,omitempty"`

	Category *string  `json:"category,omitempty"`
	Tags     []string `json:"tags,omitempty"`

	ParentTaskID          *int64 `json:"parentTaskId,omitempty"`
	HasSubtasks           bool   `json:"hasSubtasks"`
	TotalSubtaskCount     int    `json:"totalSubtaskCount"`
	CompletedSubtaskCount int    `json:"completedSubtaskCount"`

	ProjectID *int64 `json:"projectId,omitempty"`

	IsRecurring           bool    `json:"isRecurring"`
	RecurrencePattern     *string `json:"recurrencePattern,omitempty"`
	RecurrenceConfig      *string `json:"recurrenceConfig,omitempty"`
	ParentRecurringTaskID *int64  `json:"parentRecurringTaskId,omitempty"`

	IsDeepWork bool `json:"isDeepWork"`
	IsMeeting  bool `json:"isMeeting"`
	IsFlexible bool `json:"isFlexible"`

	Status       string `json:"status"`
	ActiveStatus string `json:"activeStatus"`

	ExternalID *string `json:"externalId,omitempty"`
	Source     string  `json:"source"`

	CompletedAt *int64 `json:"completedAt,omitempty"`

	SubTasks []*TaskEntity `json:"subTasks,omitempty"`
}

func NewTaskEntity() *TaskEntity {
	return &TaskEntity{
		Priority:     string(enum.PriorityMedium),
		IsFlexible:   true,
		ActiveStatus: string(enum.Active),
		Status:       string(enum.StatusTodo),
		Source:       "manual",
		Tags:         []string{},
	}
}

func (t *TaskEntity) IsOverdue(currentTimeMs int64) bool {
	if t.DeadlineMs == nil {
		return false
	}
	return currentTimeMs > *t.DeadlineMs && t.Status != "DONE" && t.Status != "CANCELLED"
}

func (t *TaskEntity) IsCompleted() bool {
	return enum.TaskStatus(t.Status).IsCompleted()
}

func (t *TaskEntity) CanBeScheduled(currentTimeMs int64) bool {
	if t.EarliestStartMs == nil {
		return true
	}
	return currentTimeMs >= *t.EarliestStartMs
}

func (t *TaskEntity) GetPriorityScore() float64 {
	if t.PriorityScore != nil {
		return *t.PriorityScore
	}

	priority := enum.TaskPriority(t.Priority)
	return priority.GetScore()
}

func (t *TaskEntity) IsLeafTask() bool {
	return !t.HasSubtasks
}

func (t *TaskEntity) RecalculateSubTaskCounts(subTasks []*TaskEntity) {
	t.TotalSubtaskCount = len(subTasks)
	t.CompletedSubtaskCount = 0
	for _, subTask := range subTasks {
		if subTask.IsCompleted() {
			t.CompletedSubtaskCount++
		}
	}
}
