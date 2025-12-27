/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateTaskRequest struct {
	Title       string  `json:"title" validate:"required,max=500"`
	Description *string `json:"description,omitempty"`

	Priority             string `json:"priority" validate:"required,oneof=LOW MEDIUM HIGH URGENT"`
	EstimatedDurationMin *int   `json:"estimatedDurationMin,omitempty" validate:"omitempty,min=1"`
	PreferredStartDateMs *int64 `json:"preferredStartDateMs,omitempty"`
	DeadlineMs           *int64 `json:"deadlineMs,omitempty"`
	EarliestStartMs      *int64 `json:"earliestStartMs,omitempty"`

	Category *string  `json:"category,omitempty" validate:"omitempty,max=100"`
	Tags     []string `json:"tags,omitempty"`

	ParentTaskID *int64 `json:"parentTaskId,omitempty"`
	ProjectID    *int64 `json:"projectId,omitempty"`

	IsRecurring       bool    `json:"isRecurring"`
	RecurrencePattern *string `json:"recurrencePattern,omitempty" validate:"omitempty,max=100"`
	RecurrenceConfig  *string `json:"recurrenceConfig,omitempty"`

	IsDeepWork bool `json:"isDeepWork"`
	IsMeeting  bool `json:"isMeeting"`
	IsFlexible bool `json:"isFlexible"`

	ExternalID *string `json:"externalId,omitempty" validate:"omitempty,max=255"`
	Source     *string `json:"source,omitempty" validate:"omitempty,oneof=manual email calendar integration"`
}

type UpdateTaskRequest struct {
	Title       *string `json:"title,omitempty" validate:"omitempty,max=500"`
	Description *string `json:"description,omitempty"`

	Priority             *string  `json:"priority,omitempty" validate:"omitempty,oneof=LOW MEDIUM HIGH URGENT"`
	PriorityScore        *float64 `json:"priorityScore,omitempty"`
	EstimatedDurationMin *int     `json:"estimatedDurationMin,omitempty" validate:"omitempty,min=1"`
	ActualDurationMin    *int     `json:"actualDurationMin,omitempty" validate:"omitempty,min=0"`
	PreferredStartDateMs *int64   `json:"preferredStartDateMs,omitempty"`
	DeadlineMs           *int64   `json:"deadlineMs,omitempty"`
	EarliestStartMs      *int64   `json:"earliestStartMs,omitempty"`

	Category *string  `json:"category,omitempty" validate:"omitempty,max=100"`
	Tags     []string `json:"tags,omitempty"`

	ParentTaskID *int64 `json:"parentTaskId,omitempty"`
	ProjectID    *int64 `json:"projectId,omitempty"`

	IsRecurring       *bool   `json:"isRecurring,omitempty"`
	RecurrencePattern *string `json:"recurrencePattern,omitempty" validate:"omitempty,max=100"`
	RecurrenceConfig  *string `json:"recurrenceConfig,omitempty"`

	IsDeepWork *bool `json:"isDeepWork,omitempty"`
	IsMeeting  *bool `json:"isMeeting,omitempty"`
	IsFlexible *bool `json:"isFlexible,omitempty"`

	Status *string `json:"status,omitempty" validate:"omitempty,oneof=TODO IN_PROGRESS DONE CANCELLED ARCHIVED"`

	// Use internal only
	HasSubtasks           *bool `json:"hasSubtasks,omitempty"`
	TotalSubtaskCount     *int  `json:"totalSubtaskCount,omitempty"`
	CompletedSubtaskCount *int  `json:"completedSubtaskCount,omitempty"`
}

type UpdateTaskStatusRequest struct {
	Status string `json:"status" validate:"required,oneof=TODO IN_PROGRESS DONE CANCELLED ARCHIVED"`
}

type CompleteTaskRequest struct {
	ActualDurationMin int `json:"actualDurationMin" validate:"required,min=1"`
	Quality           int `json:"quality" validate:"required,min=1,max=5"`
}

type TaskFilterRequest struct {
	BaseFilterRequest

	Status       *string  `form:"status,omitempty" validate:"omitempty,oneof=TODO IN_PROGRESS DONE CANCELLED ARCHIVED"`
	Priority     *string  `form:"priority,omitempty" validate:"omitempty,oneof=LOW MEDIUM HIGH"`
	ProjectID    *int64   `form:"projectId,omitempty"`
	ParentTaskID *int64   `form:"parentTaskId,omitempty"`
	Category     *string  `form:"category,omitempty"`
	Tags         []string `form:"tags,omitempty"`

	IsDeepWork  *bool `form:"isDeepWork,omitempty"`
	IsMeeting   *bool `form:"isMeeting,omitempty"`
	IsRecurring *bool `form:"isRecurring,omitempty"`

	DeadlineFrom *int64 `form:"deadlineFrom,omitempty"`
	DeadlineTo   *int64 `form:"deadlineTo,omitempty"`
}

type CreateTaskFromTemplateRequest struct {
	TemplateID int64             `json:"templateId" validate:"required"`
	Variables  map[string]string `json:"variables,omitempty"`
}

type CreateRecurringTaskRequest struct {
	CreateTaskRequest
	RecurrencePattern string `json:"recurrencePattern" validate:"required,max=100"`
	RecurrenceConfig  string `json:"recurrenceConfig" validate:"required"`
}

type BulkDeleteTasksRequest struct {
	TaskIDs []int64 `json:"taskIds" validate:"required,min=1,dive,required"`
}
