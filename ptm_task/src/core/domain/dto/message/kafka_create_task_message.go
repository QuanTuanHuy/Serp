/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package message

import "github.com/serp/ptm-task/src/core/domain/enum"

type KafkaCreateTaskMessage struct {
	ProjectID     int64             `json:"projectId"`
	GroupTaskID   int64             `json:"groupTaskId"`
	TaskID        int64             `json:"taskId"`
	UserID        int64             `json:"userId"`
	Title         string            `json:"title"`
	Description   string            `json:"description"`
	Priority      []enum.Priority   `json:"priority"`
	PriorityScore *float64          `json:"priorityScore"`
	Status        enum.Status       `json:"status"`
	StartDate     *int64            `json:"startDate"`
	Deadline      *int64            `json:"deadline"`
	Duration      float64           `json:"duration"`
	ActiveStatus  enum.ActiveStatus `json:"activeStatus"`
}
