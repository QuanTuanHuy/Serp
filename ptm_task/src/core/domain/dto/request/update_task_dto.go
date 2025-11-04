/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateTaskDTO struct {
	Title              string                 `json:"title" validate:"required,min=1,max=200"`
	Description        string                 `json:"description" validate:"max=500"`
	StartDate          int64                  `json:"startDate" validate:""`
	Deadline           int64                  `json:"deadline" validate:""`
	Duration           float64                `json:"duration" validate:"min=0"`
	Status             string                 `json:"status" validate:"required,oneof=TODO IN_PROGRESS DONE PENDING"`
	Priority           []string               `json:"priority" validate:"required,dive,oneof=LOW MEDIUM HIGH STAR CUSTOM"`
	TaskOrder          int32                  `json:"taskOrder" validate:"required"`
	StopTime           float64                `json:"stopTime" validate:"required"`
	ScheduleTaskID     int64                  `json:"scheduleTaskId" validate:"required"`
	ActiveStatus       *string                `json:"activeStatus" validate:"omitempty,oneof=ACTIVE INACTIVE"`
	PriorityDimensions []PriorityDimensionDTO `json:"priorityDimensions" validate:"omitempty,dive"`
}
