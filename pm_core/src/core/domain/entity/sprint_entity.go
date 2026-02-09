/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type SprintEntity struct {
	BaseEntity

	ProjectID int64   `json:"projectId"`
	Name      string  `json:"name"`
	Goal      *string `json:"goal,omitempty"`

	Status string `json:"status"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	EndDateMs   *int64 `json:"endDateMs,omitempty"`

	SprintOrder int `json:"sprintOrder"`

	// Denormalized stats
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
