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

	ParentTaskID          *int64 `json:"parentTaskId,omitempty"`
	HasSubtasks           bool   `json:"hasSubtasks"`
	TotalSubtaskCount     int    `json:"totalSubtaskCount"`
	CompletedSubtaskCount int    `json:"completedSubtaskCount"`

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

	ParentTaskID          *int64 `json:"parentTaskId,omitempty"`
	HasSubtasks           *bool  `json:"hasSubtasks,omitempty"`
	TotalSubtaskCount     *int   `json:"totalSubtaskCount,omitempty"`
	CompletedSubtaskCount *int   `json:"completedSubtaskCount,omitempty"`

	IsDeepWork *bool `json:"isDeepWork,omitempty"`
	IsMeeting  *bool `json:"isMeeting,omitempty"`
	IsFlexible *bool `json:"isFlexible,omitempty"`

	Status *string `json:"status,omitempty"`
}

type TaskDeletedEvent struct {
	TaskID int64 `json:"taskId"`
	UserID int64 `json:"userId"`
}

type BulkTaskDeletedEvent struct {
	TaskIDs []int64 `json:"taskIds"`
	UserID  int64   `json:"userId"`
}
