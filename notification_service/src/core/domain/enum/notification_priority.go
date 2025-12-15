/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type NotificationPriority string

const (
	PriorityLow    NotificationPriority = "LOW"
	PriorityMedium NotificationPriority = "MEDIUM"
	PriorityHigh   NotificationPriority = "HIGH"
	PriorityUrgent NotificationPriority = "URGENT"
)

func (np NotificationPriority) String() string {
	return string(np)
}
