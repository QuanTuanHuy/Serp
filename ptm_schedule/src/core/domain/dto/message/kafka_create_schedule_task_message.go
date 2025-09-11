/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package message

type KafkaCreateScheduleTaskMessage struct {
	TaskID           int64  `json:"taskId"`
	ScheduleTaskID   int64  `json:"scheduleTaskId"`
	ScheduleTaskName string `json:"scheduleTaskName"`
}
