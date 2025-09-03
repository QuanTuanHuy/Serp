/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateGroupTaskDTO struct {
	ProjectID   int64     `json:"projectId" validate:"required"`
	Title       string    `json:"title" validate:"required,min=1,max=200"`
	Description *string   `json:"description" validate:"omitempty,max=500"`
	Priority    []*string `json:"priority" validate:"omitempty,dive,oneof=LOW MEDIUM HIGH STAR"`
	Status      *string   `json:"status" validate:"omitempty,oneof=TODO IN_PROGRESS DONE PENDING"`
}
