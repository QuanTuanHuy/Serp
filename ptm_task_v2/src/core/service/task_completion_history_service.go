/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"time"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type ITaskCompletionHistoryService interface {
	// Validation
	ValidateCompletionData(history *entity.TaskCompletionHistoryEntity) error

	// History operations
	RecordCompletion(ctx context.Context, tx *gorm.DB, history *entity.TaskCompletionHistoryEntity) error

	// Query operations
	GetCompletionHistoryByTaskID(ctx context.Context, taskID int64) ([]*entity.TaskCompletionHistoryEntity, error)
	GetCompletionHistoryByUserID(ctx context.Context, userID int64, filter *store.CompletionHistoryFilter) ([]*entity.TaskCompletionHistoryEntity, error)

	// Analytics operations
	GetAverageDurationByCategory(ctx context.Context, userID int64, category string, priority string) (*store.DurationStats, error)
	GetCompletionStatsByTimeOfDay(ctx context.Context, userID int64, fromTimeMs int64) ([]*store.TimeOfDayStats, error)
	GetAccuracyMetrics(ctx context.Context, userID int64, fromTimeMs int64) (*store.AccuracyMetrics, error)
	GetProductivityPatterns(ctx context.Context, userID int64, fromTimeMs int64) ([]*store.ProductivityPattern, error)
}

type taskCompletionHistoryService struct {
	historyPort store.ITaskCompletionHistoryPort
}

func NewTaskCompletionHistoryService(historyPort store.ITaskCompletionHistoryPort) ITaskCompletionHistoryService {
	return &taskCompletionHistoryService{
		historyPort: historyPort,
	}
}

func (s *taskCompletionHistoryService) ValidateCompletionData(history *entity.TaskCompletionHistoryEntity) error {
	if history.TaskID == 0 {
		return errors.New(constant.CompletionTaskIDRequired)
	}
	if history.UserID == 0 {
		return errors.New(constant.CompletionUserIDRequired)
	}
	if history.ActualDurationMin <= 0 {
		return errors.New(constant.CompletionInvalidDuration)
	}
	if history.CompletionQuality != nil && (*history.CompletionQuality < 1 || *history.CompletionQuality > 5) {
		return errors.New(constant.CompletionInvalidQuality)
	}
	return nil
}

func (s *taskCompletionHistoryService) RecordCompletion(ctx context.Context, tx *gorm.DB, history *entity.TaskCompletionHistoryEntity) error {
	now := time.Now().UnixMilli()
	history.CreatedAt = now
	history.UpdatedAt = now

	timeOfDay := history.ComputeTimeOfDay()
	history.TimeOfDay = &timeOfDay
	dayOfWeek := history.ComputeDayOfWeek()
	history.DayOfWeek = &dayOfWeek

	if err := s.ValidateCompletionData(history); err != nil {
		return err
	}
	return s.historyPort.CreateCompletionHistory(ctx, tx, history)
}

func (s *taskCompletionHistoryService) GetCompletionHistoryByTaskID(ctx context.Context, taskID int64) ([]*entity.TaskCompletionHistoryEntity, error) {
	return s.historyPort.GetCompletionHistoriesByTaskID(ctx, taskID)
}

func (s *taskCompletionHistoryService) GetCompletionHistoryByUserID(ctx context.Context, userID int64, filter *store.CompletionHistoryFilter) ([]*entity.TaskCompletionHistoryEntity, error) {
	return s.historyPort.GetCompletionHistoriesByUserID(ctx, userID, filter)
}

func (s *taskCompletionHistoryService) GetAverageDurationByCategory(ctx context.Context, userID int64, category string, priority string) (*store.DurationStats, error) {
	return s.historyPort.GetAverageDurationByCategory(ctx, userID, category, priority)
}

func (s *taskCompletionHistoryService) GetCompletionStatsByTimeOfDay(ctx context.Context, userID int64, fromTimeMs int64) ([]*store.TimeOfDayStats, error) {
	return s.historyPort.GetCompletionStatsByTimeOfDay(ctx, userID, fromTimeMs)
}

func (s *taskCompletionHistoryService) GetAccuracyMetrics(ctx context.Context, userID int64, fromTimeMs int64) (*store.AccuracyMetrics, error) {
	return s.historyPort.GetAccuracyMetrics(ctx, userID, fromTimeMs)
}

func (s *taskCompletionHistoryService) GetProductivityPatterns(ctx context.Context, userID int64, fromTimeMs int64) ([]*store.ProductivityPattern, error) {
	return s.historyPort.GetProductivityPatterns(ctx, userID, fromTimeMs)
}
