/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type ScheduleEventModel struct {
	BaseModel
	SchedulePlanID int64     `gorm:"index:idx_ev_plan_date,priority:1;not null"`
	ScheduleTaskID int64     `gorm:"index;not null"`
	Date           time.Time `gorm:"type:date;index:idx_ev_plan_date,priority:2;not null"`
	StartMin       int       `gorm:"not null"`
	EndMin         int       `gorm:"not null"`
	Status         string    `gorm:"type:varchar(20);not null"`
	ActualStartMin *int      `gorm:""`
	ActualEndMin   *int      `gorm:""`
}

func (ScheduleEventModel) TableName() string {
	return "schedule_events"
}
