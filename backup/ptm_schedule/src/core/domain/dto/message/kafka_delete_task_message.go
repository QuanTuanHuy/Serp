/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package message

type KafkaDeleteTaskMessage struct {
	TaskID int64 `json:"taskId"`
}
