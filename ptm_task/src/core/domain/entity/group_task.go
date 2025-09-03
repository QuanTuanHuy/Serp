/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-task/src/core/domain/enum"

type GroupTaskEntity struct {
	BaseEntity
	Title          string            `json:"title"`
	Description    string            `json:"description"`
	Priority       []enum.Priority   `json:"priority"`
	Status         enum.Status       `json:"status"`
	TotalTasks     int               `json:"totalTasks"`
	CompletedTasks int               `json:"completedTasks"`
	OrdinalNumber  int               `json:"ordinalNumber"`
	ActiveStatus   enum.ActiveStatus `json:"activeStatus"`
	IsDefault      bool              `json:"isDefault"`
	ProjectID      int64             `json:"projectId"`

	Tasks []*TaskEntity `json:"tasks,omitempty"`
}
