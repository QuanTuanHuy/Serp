/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package message

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type KafkaCreateTaskMessage struct {
	TaskID   int64 `json:"taskId"`
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Title      string        `json:"title"`
	Priority   enum.Priority `json:"priority"`
	Category   *string       `json:"category"`
	IsDeepWork bool          `json:"isDeepWork"`

	EstimatedDurationMin int `json:"estimatedDurationMin"`

	EarliestStartMs  *int64 `json:"earliestStartMs"`
	DeadlineMs       *int64 `json:"deadlineMs"`
	PreferredStartMs *int64 `json:"preferredStartMs"`

	DependentTaskIDs []int64 `json:"dependentTaskIds"`
}
