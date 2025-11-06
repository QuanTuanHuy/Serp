/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type TaskChangeLogEntity struct {
	BaseEntity

	TaskID int64 `json:"taskId"`
	UserID int64 `json:"userId"`

	ChangeType string  `json:"changeType"`
	FieldName  *string `json:"fieldName,omitempty"`
	OldValue   *string `json:"oldValue,omitempty"`
	NewValue   *string `json:"newValue,omitempty"`

	ChangeSource *string `json:"changeSource,omitempty"`
	ChangeReason *string `json:"changeReason,omitempty"`

	IPAddress *string `json:"ipAddress,omitempty"`
	UserAgent *string `json:"userAgent,omitempty"`
}

func NewTaskChangeLogEntity(taskID, userID int64, changeType string) *TaskChangeLogEntity {
	return &TaskChangeLogEntity{
		TaskID:     taskID,
		UserID:     userID,
		ChangeType: changeType,
	}
}

func (t *TaskChangeLogEntity) WithFieldChange(fieldName, oldValue, newValue string) *TaskChangeLogEntity {
	t.FieldName = &fieldName
	t.OldValue = &oldValue
	t.NewValue = &newValue
	return t
}

func (t *TaskChangeLogEntity) WithContext(source, reason string) *TaskChangeLogEntity {
	t.ChangeSource = &source
	t.ChangeReason = &reason
	return t
}

func (t *TaskChangeLogEntity) WithMetadata(ipAddress, userAgent string) *TaskChangeLogEntity {
	t.IPAddress = &ipAddress
	t.UserAgent = &userAgent
	return t
}

func (t *TaskChangeLogEntity) GetChangeDescription(taskTitle string) string {
	switch t.ChangeType {
	case "created":
		return "Task \"" + taskTitle + "\" created"
	case "status_changed":
		if t.OldValue != nil && t.NewValue != nil {
			return "Task \"" + taskTitle + "\" status changed: " + *t.OldValue + " → " + *t.NewValue
		}
		return "Task \"" + taskTitle + "\" status changed"
	case "rescheduled":
		if t.ChangeReason != nil {
			return "Task \"" + taskTitle + "\" rescheduled. Reason: " + *t.ChangeReason
		}
		return "Task \"" + taskTitle + "\" rescheduled"
	case "deleted":
		return "Task \"" + taskTitle + "\" deleted"
	case "updated":
		if t.FieldName != nil {
			return "Task \"" + taskTitle + "\" " + *t.FieldName + " updated"
		}
		return "Task \"" + taskTitle + "\" updated"
	default:
		return "Task \"" + taskTitle + "\" " + t.ChangeType
	}
}
