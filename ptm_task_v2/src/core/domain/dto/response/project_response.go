/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

type ProjectResponse struct {
	ID       int64 `json:"id"`
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Title       string  `json:"title"`
	Description *string `json:"description,omitempty"`

	Status   string `json:"status"`
	Priority string `json:"priority"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	DeadlineMs  *int64 `json:"deadlineMs,omitempty"`

	ProgressPercentage int `json:"progressPercentage"`

	Color      *string `json:"color,omitempty"`
	Icon       *string `json:"icon,omitempty"`
	IsFavorite bool    `json:"isFavorite"`

	ActiveStatus string `json:"activeStatus"`

	CreatedAt int64 `json:"createdAt"`
	UpdatedAt int64 `json:"updatedAt"`

	// Optional stats (populated when includeStats=true)
	TotalTasks     *int     `json:"totalTasks,omitempty"`
	CompletedTasks *int     `json:"completedTasks,omitempty"`
	EstimatedHours *float64 `json:"estimatedHours,omitempty"`
	ActualHours    *float64 `json:"actualHours,omitempty"`

	// Computed fields
	IsOverdue           *bool  `json:"isOverdue,omitempty"`
	DeadlineRemainingMs *int64 `json:"deadlineRemainingMs,omitempty"`
}

type ProjectStatsResponse struct {
	ProjectID int64 `json:"projectId"`

	TotalTasks      int `json:"totalTasks"`
	CompletedTasks  int `json:"completedTasks"`
	TodoTasks       int `json:"todoTasks"`
	InProgressTasks int `json:"inProgressTasks"`

	EstimatedDurationMin int `json:"estimatedDurationMin"`
	ActualDurationMin    int `json:"actualDurationMin"`

	EstimatedHours float64 `json:"estimatedHours"`
	ActualHours    float64 `json:"actualHours"`

	OverdueTasks int `json:"overdueTasks"`

	ProgressPercentage int `json:"progressPercentage"`
}

type ProjectListResponse struct {
	Projects   []*ProjectResponse `json:"projects"`
	TotalCount int64              `json:"totalCount"`
	Limit      int                `json:"limit"`
	Offset     int                `json:"offset"`
}
