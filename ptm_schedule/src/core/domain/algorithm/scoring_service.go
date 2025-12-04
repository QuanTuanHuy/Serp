/*
Author: QuanTuanHuy
Description: Part of Serp Project - Scoring Service
*/

package algorithm

import (
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

const (
	WeightPriority    = 10.0
	WeightDeadline    = 5.0
	WeightDeepWork    = 2.0
	CriticalThreshold = 80.0
	CriticalHours     = 24
)

type ScoringService struct{}

func NewScoringService() *ScoringService {
	return &ScoringService{}
}

func (s *ScoringService) CalculateTaskScore(task *TaskInput) float64 {
	priorityScore := s.getPriorityBaseScore(task)
	urgencyScore := s.getUrgencyScore(task)
	deepWorkBonus := s.getDeepWorkBonus(task)

	return (priorityScore * WeightPriority) + (urgencyScore * WeightDeadline) + (deepWorkBonus * WeightDeepWork)
}

func (s *ScoringService) getPriorityBaseScore(task *TaskInput) float64 {
	switch task.Priority {
	case enum.PriorityHigh:
		return 100.0
	case enum.PriorityMedium:
		return 50.0
	case enum.PriorityLow:
		return 10.0
	default:
		return 0.0
	}
}

func (s *ScoringService) getUrgencyScore(task *TaskInput) float64 {
	if task.DeadlineMs == nil {
		return 0
	}

	nowMs := time.Now().UnixMilli()
	timeRemainingMs := *task.DeadlineMs - nowMs
	hoursRemaining := float64(timeRemainingMs) / 3600000.0

	if hoursRemaining <= 0 {
		return 100.0 // Overdue - highest urgency
	}
	if hoursRemaining < 24 {
		return 80.0 // Within 24 hours
	}
	if hoursRemaining < 72 {
		return 50.0 // Within 3 days
	}
	if hoursRemaining < 168 {
		return 20.0 // Within 1 week
	}
	return 5.0 // More than 1 week
}

func (s *ScoringService) getDeepWorkBonus(task *TaskInput) float64 {
	if task.IsDeepWork {
		return 5.0
	}
	return 0
}

func (s *ScoringService) IsCriticalTask(task *TaskInput) bool {
	score := s.CalculateTaskScore(task)
	if score >= CriticalThreshold {
		return true
	}

	if task.DeadlineMs != nil {
		nowMs := time.Now().UnixMilli()
		hoursRemaining := float64(*task.DeadlineMs-nowMs) / 3600000.0
		if hoursRemaining < CriticalHours {
			return true
		}
	}

	return false
}

func (s *ScoringService) ScoreWindow(window *Window, task *TaskInput) float64 {
	score := 0.0

	// Prefer earlier windows for urgent tasks
	if task.DeadlineMs != nil && window.DateMs <= *task.DeadlineMs {
		score += 10.0
	}

	// Deep work matching
	if task.IsDeepWork && window.IsDeepWork {
		score += 20.0
	}

	// Prefer larger windows for large tasks (less fragmentation)
	if window.Duration() >= task.DurationMin {
		score += 15.0
	}

	// Prefer preferred start time
	if task.EarliestStartMs != nil && window.DateMs >= *task.EarliestStartMs {
		score += 5.0
	}

	return score + window.WindowScore
}
