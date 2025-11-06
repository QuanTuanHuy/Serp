/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type TaskCompletionHistoryEntity struct {
	BaseEntity

	TaskID int64 `json:"taskId"`
	UserID int64 `json:"userId"`

	ScheduledDateMs   *int64 `json:"scheduledDateMs,omitempty"`
	ScheduledStartMin *int   `json:"scheduledStartMin,omitempty"`
	ScheduledEndMin   *int   `json:"scheduledEndMin,omitempty"`

	ActualStartMs     int64 `json:"actualStartMs"`
	ActualEndMs       int64 `json:"actualEndMs"`
	ActualDurationMin int   `json:"actualDurationMin"`

	WasInterrupted    bool `json:"wasInterrupted"`
	InterruptionCount int  `json:"interruptionCount"`
	CompletionQuality *int `json:"completionQuality,omitempty"`

	TaskCategory         *string `json:"taskCategory,omitempty"`
	TaskPriority         *string `json:"taskPriority,omitempty"`
	EstimatedDurationMin *int    `json:"estimatedDurationMin,omitempty"`

	TimeOfDay *string `json:"timeOfDay,omitempty"`
	DayOfWeek *int    `json:"dayOfWeek,omitempty"`
}

func NewTaskCompletionHistoryEntity(taskID, userID int64, actualStartMs, actualEndMs int64) *TaskCompletionHistoryEntity {
	durationMin := int((actualEndMs - actualStartMs) / 60000)

	return &TaskCompletionHistoryEntity{
		TaskID:            taskID,
		UserID:            userID,
		ActualStartMs:     actualStartMs,
		ActualEndMs:       actualEndMs,
		ActualDurationMin: durationMin,
		InterruptionCount: 0,
		WasInterrupted:    false,
	}
}

func (t *TaskCompletionHistoryEntity) ComputeTimeOfDay() string {
	hour := (t.ActualStartMs / 3600000) % 24

	if hour >= 6 && hour < 12 {
		return "morning"
	} else if hour >= 12 && hour < 18 {
		return "afternoon"
	} else if hour >= 18 && hour < 22 {
		return "evening"
	} else {
		return "night"
	}
}

func (t *TaskCompletionHistoryEntity) ComputeDayOfWeek() int {
	days := t.ActualStartMs / 86400000
	return int((days + 4) % 7)
}

func (t *TaskCompletionHistoryEntity) GetDurationAccuracy() *float64 {
	if t.EstimatedDurationMin == nil || *t.EstimatedDurationMin == 0 {
		return nil
	}

	diff := float64(t.ActualDurationMin - *t.EstimatedDurationMin)
	accuracy := (diff / float64(*t.EstimatedDurationMin)) * 100
	return &accuracy
}
