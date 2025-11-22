/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import (
	"time"

	"gorm.io/datatypes"
)

type TaskModel struct {
	BaseModel
	Title         string         `gorm:"not null" json:"title"`
	Description   string         `gorm:"not null" json:"description"`
	Priority      string         `gorm:"type:text" json:"priority"`
	Status        string         `gorm:"not null" json:"status"`
	StartDate     *time.Time     `json:"startDate"`
	Deadline      *time.Time     `json:"deadline"`
	Duration      float64        `gorm:"default:0" json:"duration"`
	ActiveStatus  string         `gorm:"default:ACTIVE" json:"activeStatus"`
	GroupTaskID   int64          `gorm:"not null;index" json:"groupTaskId"`
	UserID        int64          `gorm:"not null;index" json:"userId"`
	ParentTaskID  *int64         `gorm:"index" json:"parentTaskId"`
	PriorityScore *float64       `gorm:"type:double precision" json:"priorityScore"`
	PriorityDims  datatypes.JSON `gorm:"type:jsonb" json:"priorityDimensions"`
}

func (TaskModel) TableName() string {
	return "tasks"
}
