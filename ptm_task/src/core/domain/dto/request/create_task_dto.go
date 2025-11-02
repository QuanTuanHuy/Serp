/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type PriorityDimensionDTO struct {
	Key   string `json:"key" validate:"required"`
	Value int32  `json:"value" validate:"required"`
}

type CreateTaskDTO struct {
	Title              string                 `json:"title" validate:"required,min=1,max=200"`
	Description        *string                `json:"description" validate:"omitempty,max=500"`
	Priority           []*string              `json:"priority" validate:"omitempty,dive,oneof=LOW MEDIUM HIGH STAR CUSTOM"`
	Status             *string                `json:"status" validate:"omitempty,oneof=TODO IN_PROGRESS DONE PENDING"`
	StartDate          *int64                 `json:"startDate" validate:"omitempty"`
	Deadline           *int64                 `json:"deadline" validate:"omitempty"`
	Duration           *float64               `json:"duration" validate:"omitempty,min=0"`
	GroupTaskID        int64                  `json:"groupTaskId" validate:"required,min=1"`
	ParentTaskID       *int64                 `json:"parentTaskId" validate:"omitempty,min=1"`
	PriorityDimensions []PriorityDimensionDTO `json:"priorityDimensions" validate:"omitempty,dive"`
}
