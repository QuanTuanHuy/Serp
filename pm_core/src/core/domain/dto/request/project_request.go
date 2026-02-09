/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateProjectRequest struct {
	Name            string  `json:"name" validate:"required,max=200"`
	Key             string  `json:"key" validate:"required,max=10"`
	Description     *string `json:"description,omitempty"`
	Visibility      *string `json:"visibility,omitempty"`
	StartDateMs     *int64  `json:"startDateMs,omitempty"`
	TargetEndDateMs *int64  `json:"targetEndDateMs,omitempty"`
}

type UpdateProjectRequest struct {
	Name            *string `json:"name,omitempty" validate:"omitempty,max=200"`
	Description     *string `json:"description,omitempty"`
	Status          *string `json:"status,omitempty"`
	Visibility      *string `json:"visibility,omitempty"`
	StartDateMs     *int64  `json:"startDateMs,omitempty"`
	TargetEndDateMs *int64  `json:"targetEndDateMs,omitempty"`
}

type ProjectFilterRequest struct {
	BaseFilterRequest
	Statuses   []string `form:"statuses"`
	Visibility *string  `form:"visibility"`
}
