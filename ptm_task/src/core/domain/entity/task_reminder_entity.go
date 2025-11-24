/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type TaskReminderEntity struct {
	BaseEntity

	TaskID int64 `json:"taskId"`
	UserID int64 `json:"userId"`

	ReminderType  string `json:"reminderType"`
	TriggerTimeMs int64  `json:"triggerTimeMs"`

	AdvanceNoticeMin *int   `json:"advanceNoticeMin,omitempty"`
	IsRecurring      bool   `json:"isRecurring"`
	SnoozeUntilMs    *int64 `json:"snoozeUntilMs,omitempty"`

	NotificationChannels []string `json:"notificationChannels"`
	MessageTemplate      *string  `json:"messageTemplate,omitempty"`

	Status string `json:"status"`
	SentAt *int64 `json:"sentAt,omitempty"`
}

func NewTaskReminderEntity(taskID, userID int64, reminderType string, triggerTimeMs int64) *TaskReminderEntity {
	return &TaskReminderEntity{
		TaskID:               taskID,
		UserID:               userID,
		ReminderType:         reminderType,
		TriggerTimeMs:        triggerTimeMs,
		Status:               "pending",
		IsRecurring:          false,
		NotificationChannels: []string{"push"},
	}
}

func (t *TaskReminderEntity) ShouldTrigger(currentTimeMs int64) bool {
	if t.Status != "pending" {
		return false
	}

	if t.SnoozeUntilMs != nil && currentTimeMs < *t.SnoozeUntilMs {
		return false
	}

	return currentTimeMs >= t.TriggerTimeMs
}

func (t *TaskReminderEntity) Snooze(snoozeUntilMs int64) {
	t.Status = "snoozed"
	t.SnoozeUntilMs = &snoozeUntilMs
}

func (t *TaskReminderEntity) MarkAsSent(sentAtMs int64) {
	t.Status = "sent"
	t.SentAt = &sentAtMs
}

func (t *TaskReminderEntity) Dismiss() {
	t.Status = "dismissed"
}

func (t *TaskReminderEntity) ResetIfSnoozed(currentTimeMs int64) {
	if t.Status == "snoozed" && t.SnoozeUntilMs != nil && currentTimeMs >= *t.SnoozeUntilMs {
		t.Status = "pending"
		t.SnoozeUntilMs = nil
	}
}

func CreateDeadlineReminder(taskID, userID int64, deadlineMs int64, advanceNoticeMin int) *TaskReminderEntity {
	triggerTimeMs := deadlineMs - int64(advanceNoticeMin*60*1000)

	reminder := NewTaskReminderEntity(taskID, userID, "deadline", triggerTimeMs)
	reminder.AdvanceNoticeMin = &advanceNoticeMin

	return reminder
}

func CreateStartTimeReminder(taskID, userID int64, startTimeMs int64, advanceNoticeMin int) *TaskReminderEntity {
	triggerTimeMs := startTimeMs - int64(advanceNoticeMin*60*1000)

	reminder := NewTaskReminderEntity(taskID, userID, "start_time", triggerTimeMs)
	reminder.AdvanceNoticeMin = &advanceNoticeMin

	return reminder
}
