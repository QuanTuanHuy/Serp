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

type MilestoneEntity struct {
	BaseEntity

	ProjectID   int64   `json:"projectId"`
	Name        string  `json:"name"`
	Description *string `json:"description,omitempty"`
	Status      string  `json:"status"`

	TargetDateMs *int64 `json:"targetDateMs,omitempty"`

	// Denormalized
	TotalWorkItems     int `json:"totalWorkItems,omitempty"`
	CompletedWorkItems int `json:"completedWorkItems,omitempty"`
	ProgressPercentage int `json:"progressPercentage,omitempty"`

	ActiveStatus string `json:"activeStatus"`
}

func NewMilestoneEntity() *MilestoneEntity {
	return &MilestoneEntity{
		Status:       string(enum.MilestonePending),
		ActiveStatus: string(enum.Active),
	}
}

func (m *MilestoneEntity) Validate() error {
	if strings.TrimSpace(m.Name) == "" {
		return errors.New("milestone name is required")
	}
	return nil
}

func (m *MilestoneEntity) CalculateProgressPercentage() {
	if m.TotalWorkItems == 0 {
		m.ProgressPercentage = 0
		return
	}
	m.ProgressPercentage = (m.CompletedWorkItems * 100) / m.TotalWorkItems
}

func (m *MilestoneEntity) CanTransitionTo(targetStatus string) bool {
	return enum.MilestoneStatus(m.Status).CanTransitionTo(enum.MilestoneStatus(targetStatus))
}

func (m *MilestoneEntity) IsCompleted() bool {
	return enum.MilestoneStatus(m.Status) == enum.MilestoneCompleted
}

func (m *MilestoneEntity) IsMissed() bool {
	return enum.MilestoneStatus(m.Status) == enum.MilestoneMissed
}

func (m *MilestoneEntity) IsPending() bool {
	return enum.MilestoneStatus(m.Status) == enum.MilestonePending
}

func (m *MilestoneEntity) IsInProgress() bool {
	return enum.MilestoneStatus(m.Status) == enum.MilestoneInProgress
}

func (m *MilestoneEntity) IsTerminal() bool {
	return enum.MilestoneStatus(m.Status).IsTerminal()
}

func (m *MilestoneEntity) IsOverdue(nowMs int64) bool {
	if m.TargetDateMs == nil {
		return false
	}
	return *m.TargetDateMs < nowMs && !m.IsCompleted() && !m.IsMissed()
}

func (m *MilestoneEntity) UpdateStatsForAddedWorkItem() {
	m.TotalWorkItems++
	m.CalculateProgressPercentage()
}

func (m *MilestoneEntity) UpdateStatsForCompletedWorkItem() {
	m.CompletedWorkItems++
	m.CalculateProgressPercentage()
}

func (m *MilestoneEntity) UpdateStatsForRemovedWorkItem(wasCompleted bool) {
	if m.TotalWorkItems > 0 {
		m.TotalWorkItems--
	}
	if wasCompleted && m.CompletedWorkItems > 0 {
		m.CompletedWorkItems--
	}
	m.CalculateProgressPercentage()
}
