/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package message

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type KafkaUpdateTaskMessage struct {
	UserID      int64         `json:"userId"`
	TenantID    int64         `json:"tenantId"`
	TaskID      int64         `json:"taskId"`
	Title       string        `json:"title"`
	Priority    enum.Priority `json:"priority"`
	DeadlineMs  *int64        `json:"deadlineMs"`
	DurationMin int           `json:"durationMin"`
}
