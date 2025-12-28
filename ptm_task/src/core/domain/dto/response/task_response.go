/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

type TaskResponse struct {
	ID       int64 `json:"id"`
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Title       string  `json:"title"`
	Description *string `json:"description,omitempty"`

	Priority      string   `json:"priority"`
	PriorityScore *float64 `json:"priorityScore,omitempty"`

	EstimatedDurationMin *int `json:"estimatedDurationMin,omitempty"`
	ActualDurationMin    *int `json:"actualDurationMin,omitempty"`
	IsDurationLearned    bool `json:"isDurationLearned"`

	PreferredStartDateMs *int64 `json:"preferredStartDateMs,omitempty"`
	DeadlineMs           *int64 `json:"deadlineMs,omitempty"`
	EarliestStartMs      *int64 `json:"earliestStartMs,omitempty"`

	Category *string  `json:"category,omitempty"`
	Tags     []string `json:"tags,omitempty"`

	ParentTaskID          *int64  `json:"parentTaskId,omitempty"`
	HasSubtasks           bool    `json:"hasSubtasks"`
	TotalSubtaskCount     int     `json:"totalSubtaskCount"`
	CompletedSubtaskCount int     `json:"completedSubtaskCount"`
	DependentTaskIDs      []int64 `json:"dependentTaskIds,omitempty"`

	ProjectID *int64 `json:"projectId,omitempty"`

	IsRecurring           bool    `json:"isRecurring"`
	RecurrencePattern     *string `json:"recurrencePattern,omitempty"`
	RecurrenceConfig      *string `json:"recurrenceConfig,omitempty"`
	ParentRecurringTaskID *int64  `json:"parentRecurringTaskId,omitempty"`

	IsDeepWork bool `json:"isDeepWork"`
	IsMeeting  bool `json:"isMeeting"`
	IsFlexible bool `json:"isFlexible"`

	Status       string `json:"status"`
	ActiveStatus string `json:"activeStatus"`

	ExternalID *string `json:"externalId,omitempty"`
	Source     string  `json:"source"`

	CompletedAt *int64 `json:"completedAt,omitempty"`

	CreatedAt int64 `json:"createdAt"`
	UpdatedAt int64 `json:"updatedAt"`

	SubTasks []*TaskResponse `json:"subTasks,omitempty"`

	// Computed fields
	IsOverdue           *bool  `json:"isOverdue,omitempty"`
	CanBeScheduled      *bool  `json:"canBeScheduled,omitempty"`
	DeadlineRemainingMs *int64 `json:"deadlineRemainingMs,omitempty"`
}

type TaskStatsResponse struct {
	TotalTasks      int `json:"totalTasks"`
	TodoTasks       int `json:"todoTasks"`
	InProgressTasks int `json:"inProgressTasks"`
	CompletedTasks  int `json:"completedTasks"`
	OverdueTasks    int `json:"overdueTasks"`

	TotalEstimatedMin int `json:"totalEstimatedMin"`
	TotalActualMin    int `json:"totalActualMin"`

	DeepWorkTasks     int `json:"deepWorkTasks"`
	MeetingTasks      int `json:"meetingTasks"`
	RecurringTasks    int `json:"recurringTasks"`
	TasksWithDeadline int `json:"tasksWithDeadline"`
}
