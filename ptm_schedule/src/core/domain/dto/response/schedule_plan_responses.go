/*
Author: QuanTuanHuy
Description: Part of Serp Project - Schedule Plan DTOs
*/

package response

import "github.com/serp/ptm-schedule/src/core/domain/entity"

type PlanStats struct {
	TotalTasks       int     `json:"totalTasks"`
	ScheduledTasks   int     `json:"scheduledTasks"`
	UnscheduledTasks int     `json:"unscheduledTasks"`
	TotalDurationMin int     `json:"totalDurationMin"`
	ScheduledMin     int     `json:"scheduledMin"`
	UtilizationPct   float64 `json:"utilizationPct"`
	LastScheduledAt  *int64  `json:"lastScheduledAt,omitempty"`
}

type PlanDetailResponse struct {
	Plan   *entity.SchedulePlanEntity    `json:"plan"`
	Events []*entity.ScheduleEventEntity `json:"events"`
	Tasks  []*entity.ScheduleTaskEntity  `json:"tasks"`
	Stats  *PlanStats                    `json:"stats"`
}

type PlanSummaryResponse struct {
	Plan  *entity.SchedulePlanEntity `json:"plan"`
	Stats *PlanStats                 `json:"stats"`
}

type PlanHistoryResponse struct {
	Plans      []*entity.SchedulePlanEntity `json:"plans"`
	TotalCount int                          `json:"totalCount"`
}

// OptimizationResult represents the result of schedule optimization
type OptimizationResult struct {
	Success          bool    `json:"success"`
	Score            float64 `json:"score"`
	DurationMs       int64   `json:"durationMs"`
	TasksScheduled   int     `json:"tasksScheduled"`
	TasksUnscheduled int     `json:"tasksUnscheduled"`
	ErrorMessage     string  `json:"errorMessage,omitempty"`
}
