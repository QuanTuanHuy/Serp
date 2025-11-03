/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type PriorityDimension struct {
	Key   string `json:"key"`
	Value int32  `json:"value"`
}

type CreateTaskRequest struct {
	Title              string              `json:"title"`
	Description        *string             `json:"description"`
	Priority           []*string           `json:"priority"`
	Status             *string             `json:"status"`
	StartDate          *int64              `json:"startDate"`
	Deadline           *int64              `json:"deadline"`
	Duration           *float32            `json:"duration"`
	GroupTaskID        int64               `json:"groupTaskId"`
	ParentTaskID       *int64              `json:"parentTaskId"`
	PriorityDimensions []PriorityDimension `json:"priorityDimensions"`
}
