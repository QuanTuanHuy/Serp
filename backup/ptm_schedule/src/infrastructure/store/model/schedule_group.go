/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type ScheduleGroupModel struct {
	BaseModel
	SchedulePlanID  int64      `gorm:"not_null,column:schedule_plan_id"`
	ProjectID       *int64     `gorm:"column:project_id"`
	GroupTaskID     *int64     `gorm:"column:group_task_id"`
	Title           string     `gorm:"column:title"`
	Priority        string     `gorm:"column:priority"`
	Status          string     `gorm:"column:status"`
	StartHour       *int32     `gorm:"column:start_hour"`
	StartMinute     *int32     `gorm:"column:start_minute"`
	EndHour         *int32     `gorm:"column:end_hour"`
	EndMinute       *int32     `gorm:"column:end_minute"`
	Duration        float64    `gorm:"column:duration"`
	PreferenceLevel int32      `gorm:"column:preference_level"`
	Repeat          string     `gorm:"column:repeat"`
	IsNotify        bool       `gorm:"column:is_notify"`
	ActiveStatus    string     `gorm:"column:active_status"`
	IsFailed        bool       `gorm:"column:is_failed"`
	RotationDay     *time.Time `gorm:"column:rotation_day"`
}

func (g ScheduleGroupModel) TableName() string {
	return "schedule_groups"
}
