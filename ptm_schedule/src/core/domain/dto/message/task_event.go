package message

type TaskCreatedEvent struct {
	TaskID   int64 `json:"taskId"`
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Title    string `json:"title"`
	Priority string `json:"priority"`

	EstimatedDurationMin *int   `json:"estimatedDurationMin,omitempty"`
	PreferredStartDateMs *int64 `json:"preferredStartDateMs,omitempty"`
	DeadlineMs           *int64 `json:"deadlineMs,omitempty"`
	EarliestStartMs      *int64 `json:"earliestStartMs,omitempty"`

	Category *string  `json:"category,omitempty"`
	Tags     []string `json:"tags,omitempty"`

	IsDeepWork bool `json:"isDeepWork"`
	IsMeeting  bool `json:"isMeeting"`
	IsFlexible bool `json:"isFlexible"`

	Status string `json:"status"`
}

type TaskUpdatedEvent struct {
	TaskID   int64 `json:"taskId"`
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Title    *string `json:"title,omitempty"`
	Priority *string `json:"priority,omitempty"`

	EstimatedDurationMin *int   `json:"estimatedDurationMin,omitempty"`
	PreferredStartDateMs *int64 `json:"preferredStartDateMs,omitempty"`
	DeadlineMs           *int64 `json:"deadlineMs,omitempty"`
	EarliestStartMs      *int64 `json:"earliestStartMs,omitempty"`

	Category *string  `json:"category,omitempty"`
	Tags     []string `json:"tags,omitempty"`

	IsDeepWork *bool `json:"isDeepWork,omitempty"`
	IsMeeting  *bool `json:"isMeeting,omitempty"`
	IsFlexible *bool `json:"isFlexible,omitempty"`

	Status *string `json:"status,omitempty"`
}

type TaskDeletedEvent struct {
	TaskID   int64 `json:"taskId"`
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`
}

// TaskCompletedFromScheduleEvent is sent from ptm_schedule to ptm_task
// when all parts of a scheduled task are completed
type TaskCompletedFromScheduleEvent struct {
	ScheduleTaskID    int64 `json:"scheduleTaskId"`
	TaskID            int64 `json:"taskId"`
	UserID            int64 `json:"userId"`
	TenantID          int64 `json:"tenantId"`
	TotalActualMin    int   `json:"totalActualMin"`
	CompletedAtMs     int64 `json:"completedAtMs"`
	EstimatedDuration int   `json:"estimatedDuration"`
}

// ScheduleTaskPinnedEvent is sent when a user manually pins/moves a task
type ScheduleTaskPinnedEvent struct {
	ScheduleTaskID int64 `json:"scheduleTaskId"`
	TaskID         int64 `json:"taskId"`
	UserID         int64 `json:"userId"`
	PinnedDateMs   int64 `json:"pinnedDateMs"`
	PinnedStartMin int   `json:"pinnedStartMin"`
	PinnedEndMin   int   `json:"pinnedEndMin"`
}

// RippleEffectJobEvent is sent to trigger ripple effect after manual move
type RippleEffectJobEvent struct {
	SchedulePlanID   int64   `json:"schedulePlanId"`
	UserID           int64   `json:"userId"`
	TriggerEventID   int64   `json:"triggerEventId"`
	ConflictEventIDs []int64 `json:"conflictEventIds"`
	Priority         string  `json:"priority"`    // HIGH, NORMAL
	TriggerType      string  `json:"triggerType"` // MANUAL_DRAG, TASK_ADDED, TASK_UPDATED
}
