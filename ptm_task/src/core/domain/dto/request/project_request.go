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
	Status      *string `json:"status,omitempty" validate:"omitempty,oneof=NEW IN_PROGRESS COMPLETED ARCHIVED ON_HOLD"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	DeadlineMs  *int64 `json:"deadlineMs,omitempty"`

	Color      *string `json:"color,omitempty" validate:"omitempty,hexcolor"`
	Icon       *string `json:"icon,omitempty" validate:"omitempty,max=50"`
	IsFavorite *bool   `json:"isFavorite,omitempty"`
}

type UpdateProjectStatusRequest struct {
	Status string `json:"status" validate:"required,oneof=NEW IN_PROGRESS COMPLETED ARCHIVED ON_HOLD"`
}

type ProjectFilterRequest struct {
	BaseFilterRequest
	Status   *string `form:"status,omitempty" validate:"omitempty,oneof=NEW IN_PROGRESS COMPLETED ARCHIVED ON_HOLD"`
	Priority *string `form:"priority,omitempty" validate:"omitempty,oneof=LOW MEDIUM HIGH"`
}

type AddTaskToProjectRequest struct {
	TaskID int64 `json:"taskId" validate:"required"`
}

type MoveTaskToProjectRequest struct {
	TaskID       int64 `json:"taskId" validate:"required"`
	NewProjectID int64 `json:"newProjectId" validate:"required"`
}
