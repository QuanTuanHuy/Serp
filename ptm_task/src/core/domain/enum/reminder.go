/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ReminderType string

const (
	ReminderDeadline  ReminderType = "deadline"
	ReminderStartTime ReminderType = "start_time"
	ReminderCustom    ReminderType = "custom"
)

func (r ReminderType) IsValid() bool {
	switch r {
	case ReminderDeadline, ReminderStartTime, ReminderCustom:
		return true
	}
	return false
}

type ReminderStatus string

const (
	ReminderPending   ReminderStatus = "pending"
	ReminderSent      ReminderStatus = "sent"
	ReminderSnoozed   ReminderStatus = "snoozed"
	ReminderDismissed ReminderStatus = "dismissed"
)

func (s ReminderStatus) IsValid() bool {
	switch s {
	case ReminderPending, ReminderSent, ReminderSnoozed, ReminderDismissed:
		return true
	}
	return false
}
