/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type ITaskCompletionHistoryPort interface {
	CreateCompletionHistory(ctx context.Context, tx *gorm.DB, history *entity.TaskCompletionHistoryEntity) error
	CreateCompletionHistories(ctx context.Context, tx *gorm.DB, histories []*entity.TaskCompletionHistoryEntity) error

	GetCompletionHistoryByID(ctx context.Context, id int64) (*entity.TaskCompletionHistoryEntity, error)
	GetCompletionHistoriesByTaskID(ctx context.Context, taskID int64) ([]*entity.TaskCompletionHistoryEntity, error)
	GetCompletionHistoriesByUserID(ctx context.Context, userID int64, filter *CompletionHistoryFilter) ([]*entity.TaskCompletionHistoryEntity, error)
	GetLatestCompletionByTaskID(ctx context.Context, taskID int64) (*entity.TaskCompletionHistoryEntity, error)

	GetAverageDurationByCategory(ctx context.Context, userID int64, category string, priority string) (*DurationStats, error)
	GetCompletionStatsByTimeOfDay(ctx context.Context, userID int64, fromTimeMs int64) ([]*TimeOfDayStats, error)
	GetAccuracyMetrics(ctx context.Context, userID int64, fromTimeMs int64) (*AccuracyMetrics, error)
	GetProductivityPatterns(ctx context.Context, userID int64, fromTimeMs int64) ([]*ProductivityPattern, error)
}

type CompletionHistoryFilter struct {
	Category *string
	Priority *string

	CompletedFrom *int64
	CompletedTo   *int64

	TimeOfDay      *string
	DayOfWeek      *int
	WasInterrupted *bool

	MinQuality *int

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

type DurationStats struct {
	Category       string  `json:"category"`
	Priority       string  `json:"priority"`
	AvgDuration    float64 `json:"avgDuration"`
	MedianDuration float64 `json:"medianDuration"`
	StdDeviation   float64 `json:"stdDeviation"`
	SampleCount    int64   `json:"sampleCount"`
}

type TimeOfDayStats struct {
	TimeOfDay      string  `json:"timeOfDay"`
	AvgDuration    float64 `json:"avgDuration"`
	AvgQuality     float64 `json:"avgQuality"`
	CompletionRate float64 `json:"completionRate"`
	TaskCount      int64   `json:"taskCount"`
}

type AccuracyMetrics struct {
	TotalTasks          int64   `json:"totalTasks"`
	TasksWithEstimate   int64   `json:"tasksWithEstimate"`
	AvgAccuracy         float64 `json:"avgAccuracy"`
	MedianAccuracy      float64 `json:"medianAccuracy"`
	OverestimatedCount  int64   `json:"overestimatedCount"`
	UnderestimatedCount int64   `json:"underestimatedCount"`
}

type ProductivityPattern struct {
	Category    string  `json:"category"`
	DayOfWeek   int     `json:"dayOfWeek"`
	TimeOfDay   string  `json:"timeOfDay"`
	AvgDuration float64 `json:"avgDuration"`
	AvgQuality  float64 `json:"avgQuality"`
	TaskCount   int64   `json:"taskCount"`
	Confidence  float64 `json:"confidence"`
}

func NewCompletionHistoryFilter() *CompletionHistoryFilter {
	return &CompletionHistoryFilter{
		SortBy:    "created_at",
		SortOrder: "DESC",
		Limit:     100,
		Offset:    0,
	}
}
