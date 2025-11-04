/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateTaskRequest struct {
	Title              string              `json:"title"`
	Description        string              `json:"description"`
	StartDate          int64               `json:"startDate"`
	Deadline           int64               `json:"deadline"`
	Duration           float32             `json:"duration"`
	Status             string              `json:"status"`
	Priority           []string            `json:"priority"`
	TaskOrder          int32               `json:"taskOrder"`
	StopTime           float64             `json:"stopTime"`
	ScheduleTaskID     int64               `json:"scheduleTaskId"`
	ActiveStatus       *string             `json:"activeStatus"`
	PriorityDimensions []PriorityDimension `json:"priorityDimensions"`
}
