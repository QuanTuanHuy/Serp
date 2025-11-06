/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateProjectRequest struct {
	Title       string  `json:"title" validate:"required,max=500"`
	Description *string `json:"description,omitempty"`
	Priority    string  `json:"priority" validate:"required,oneof=LOW MEDIUM HIGH"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	DeadlineMs  *int64 `json:"deadlineMs,omitempty"`

	Color      *string `json:"color,omitempty" validate:"omitempty,hexcolor"`
	Icon       *string `json:"icon,omitempty" validate:"omitempty,max=50"`
	IsFavorite *bool   `json:"isFavorite,omitempty"`
}

type UpdateProjectRequest struct {
	Title       *string `json:"title,omitempty" validate:"omitempty,max=500"`
	Description *string `json:"description,omitempty"`
	Priority    *string `json:"priority,omitempty" validate:"omitempty,oneof=LOW MEDIUM HIGH"`
	Status      *string `json:"status,omitempty" validate:"omitempty,oneof=ACTIVE COMPLETED ARCHIVED ON_HOLD"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	DeadlineMs  *int64 `json:"deadlineMs,omitempty"`

	Color      *string `json:"color,omitempty" validate:"omitempty,hexcolor"`
	Icon       *string `json:"icon,omitempty" validate:"omitempty,max=50"`
	IsFavorite *bool   `json:"isFavorite,omitempty"`
}

type UpdateProjectStatusRequest struct {
	Status string `json:"status" validate:"required,oneof=ACTIVE COMPLETED ARCHIVED ON_HOLD"`
}

type ProjectFilterRequest struct {
	Statuses   []string `json:"statuses,omitempty" validate:"omitempty,dive,oneof=ACTIVE COMPLETED ARCHIVED ON_HOLD"`
	Priorities []string `json:"priorities,omitempty" validate:"omitempty,dive,oneof=LOW MEDIUM HIGH"`

	DeadlineFrom *int64 `json:"deadlineFrom,omitempty"`
	DeadlineTo   *int64 `json:"deadlineTo,omitempty"`
	CreatedFrom  *int64 `json:"createdFrom,omitempty"`
	CreatedTo    *int64 `json:"createdTo,omitempty"`

	IsFavorite *bool `json:"isFavorite,omitempty"`

	MinProgress *int `json:"minProgress,omitempty" validate:"omitempty,min=0,max=100"`
	MaxProgress *int `json:"maxProgress,omitempty" validate:"omitempty,min=0,max=100"`

	IncludeStats *bool `json:"includeStats,omitempty"`

	SortBy    *string `json:"sortBy,omitempty" validate:"omitempty,oneof=created_at deadline_ms progress_percentage title"`
	SortOrder *string `json:"sortOrder,omitempty" validate:"omitempty,oneof=ASC DESC"`

	Limit  *int `json:"limit,omitempty" validate:"omitempty,min=1,max=100"`
	Offset *int `json:"offset,omitempty" validate:"omitempty,min=0"`
}

type AddTaskToProjectRequest struct {
	TaskID int64 `json:"taskId" validate:"required"`
}

type MoveTaskToProjectRequest struct {
	TaskID       int64 `json:"taskId" validate:"required"`
	NewProjectID int64 `json:"newProjectId" validate:"required"`
}
