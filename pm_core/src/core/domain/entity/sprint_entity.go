/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"errors"
	"strings"

	"github.com/serp/pm-core/src/core/domain/enum"
)

type SprintEntity struct {
	BaseEntity

	ProjectID int64   `json:"projectId"`
	Name      string  `json:"name"`
	Goal      *string `json:"goal,omitempty"`

	Status string `json:"status"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	EndDateMs   *int64 `json:"endDateMs,omitempty"`

	SprintOrder int `json:"sprintOrder"`

	// Denormalized
	TotalWorkItems     int `json:"totalWorkItems,omitempty"`
	TotalPoints        int `json:"totalPoints,omitempty"`
	CompletedWorkItems int `json:"completedWorkItems,omitempty"`
	CompletedPoints    int `json:"completedPoints,omitempty"`

	ActiveStatus string `json:"activeStatus"`
}

func NewSprintEntity() *SprintEntity {
	return &SprintEntity{
		Status:       string(enum.SprintPlanning),
		ActiveStatus: string(enum.Active),
	}
}

func (s *SprintEntity) Validate() error {
	if strings.TrimSpace(s.Name) == "" {
		return errors.New("sprint name is required")
	}
	if err := s.ValidateDateRange(); err != nil {
		return err
	}
	return nil
}

func (s *SprintEntity) ValidateDateRange() error {
	if s.StartDateMs != nil && s.EndDateMs != nil && *s.StartDateMs > *s.EndDateMs {
		return errors.New("sprint start date must be before end date")
	}
	return nil
}

func (s *SprintEntity) CanTransitionTo(targetStatus string) bool {
	return enum.SprintStatus(s.Status).CanTransitionTo(enum.SprintStatus(targetStatus))
}

func (s *SprintEntity) IsActive() bool {
	return enum.SprintStatus(s.Status).IsActive()
}

func (s *SprintEntity) IsPlanning() bool {
	return enum.SprintStatus(s.Status).IsPlanning()
}

func (s *SprintEntity) IsCompleted() bool {
	return enum.SprintStatus(s.Status).IsCompleted()
}

func (s *SprintEntity) IsCancelled() bool {
	return enum.SprintStatus(s.Status).IsCancelled()
}

func (s *SprintEntity) IsTerminal() bool {
	return enum.SprintStatus(s.Status).IsTerminal()
}

func (s *SprintEntity) CanStart() error {
	if !s.IsPlanning() {
		return errors.New("only sprints in PLANNING status can be started")
	}
	if s.StartDateMs == nil {
		return errors.New("sprint must have a start date to be started")
	}
	if s.EndDateMs == nil {
		return errors.New("sprint must have an end date to be started")
	}
	return nil
}

func (s *SprintEntity) CanComplete() error {
	if !s.IsActive() {
		return errors.New("only active sprints can be completed")
	}
	return nil
}

func (s *SprintEntity) RecalculateStats(workItems []*WorkItemEntity) {
	s.TotalWorkItems = len(workItems)
	s.TotalPoints = 0
	s.CompletedWorkItems = 0
	s.CompletedPoints = 0

	for _, wi := range workItems {
		points := wi.GetStoryPointsValue()
		s.TotalPoints += points

		if wi.IsCompleted() {
			s.CompletedWorkItems++
			s.CompletedPoints += points
		}
	}
}

func (s *SprintEntity) GetCompletionRate() int {
	if s.TotalWorkItems == 0 {
		return 0
	}
	return (s.CompletedWorkItems * 100) / s.TotalWorkItems
}

func (s *SprintEntity) GetVelocity() int {
	return s.CompletedPoints
}
