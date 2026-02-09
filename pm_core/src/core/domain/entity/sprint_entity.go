/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type SprintEntity struct {
	BaseEntity

	ProjectID int64   `json:"projectId"`
	Name      string  `json:"name"`
	Goal      *string `json:"goal,omitempty"`

	Status string `json:"status"`

	StartDateMs *int64 `json:"startDateMs,omitempty"`
	EndDateMs   *int64 `json:"endDateMs,omitempty"`

	ActiveStatus string `json:"activeStatus"`
}
