/*
Author: QuanTuanHuy
Description: Part of Serp Project - Optimization Response DTOs
*/

package optimization

// Assignment represents a scheduled time block (matches Java Assignment)
type Assignment struct {
	TaskID       int64    `json:"taskId"`
	DateMs       int64    `json:"dateMs"`
	StartMin     int      `json:"startMin"`
	EndMin       int      `json:"endMin"`
	PartIndex    int      `json:"partIndex"`
	TotalParts   int      `json:"totalParts"`
	UtilityScore *float64 `json:"utilityScore,omitempty"`
}

// Duration returns the duration in minutes
func (a *Assignment) Duration() int {
	return a.EndMin - a.StartMin
}

// UnscheduledTask represents a task that couldn't be scheduled
type UnscheduledTask struct {
	TaskID int64  `json:"taskId"`
	Reason string `json:"reason"`
}

// PlanResult represents the optimization result (matches Java PlanResult)
type PlanResult struct {
	Assignments []*Assignment      `json:"assignments"`
	UnScheduled []*UnscheduledTask `json:"unScheduled"`
}

// NewPlanResult creates an empty plan result
func NewPlanResult() *PlanResult {
	return &PlanResult{
		Assignments: make([]*Assignment, 0),
		UnScheduled: make([]*UnscheduledTask, 0),
	}
}

// IsFullyScheduled returns true if all tasks were scheduled
func (r *PlanResult) IsFullyScheduled() bool {
	return len(r.UnScheduled) == 0
}

// ScheduledCount returns the number of scheduled tasks
func (r *PlanResult) ScheduledCount() int {
	taskIDs := make(map[int64]bool)
	for _, a := range r.Assignments {
		taskIDs[a.TaskID] = true
	}
	return len(taskIDs)
}

// GeneralResponse wraps the API response from ptm_optimization
type GeneralResponse struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    *PlanResult `json:"data"`
}
