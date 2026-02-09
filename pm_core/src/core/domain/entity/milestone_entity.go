/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type MilestoneEntity struct {
	BaseEntity

	ProjectID   int64   `json:"projectId"`
	Name        string  `json:"name"`
	Description *string `json:"description,omitempty"`
	Status      string  `json:"status"`

	TargetDateMs *int64 `json:"targetDateMs,omitempty"`

	// Denormalized stats
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

func (m *MilestoneEntity) CalculateProgressPercentage() {
	if m.TotalWorkItems == 0 {
		m.ProgressPercentage = 0
		return
	}
	m.ProgressPercentage = (m.CompletedWorkItems * 100) / m.TotalWorkItems
}
