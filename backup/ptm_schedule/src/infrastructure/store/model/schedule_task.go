/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import (
	"time"
)

type ScheduleTaskModel struct {
	BaseModel
	TaskID               int64     `json:"taskId" gorm:"column:task_id;not null"`
	Title                string    `json:"title" gorm:"column:title;not null"`
	Priority             string    `json:"priority" gorm:"column:priority"`
	Status               string    `json:"status" gorm:"column:status;not null"`
	StartDate            time.Time `json:"startDate" gorm:"column:start_date;not null"`
	Deadline             time.Time `json:"deadline" gorm:"column:deadline;not null"`
	Duration             float64   `json:"duration" gorm:"column:duration;not null"`
	ActiveStatus         string    `json:"activeStatus" gorm:"column:active_status;not null"`
	IsSynchronizedWithWO bool      `json:"isSynchronizedWithWO" gorm:"column:is_synchronized_with_wo;not null"`
	PreferenceLevel      int32     `json:"preferenceLevel" gorm:"column:preference_level"`
	TaskOrder            int32     `json:"taskOrder" gorm:"column:task_order"`
	Weight               float64   `json:"weight" gorm:"column:weight"`
	StopTime             float64   `json:"stopTime" gorm:"column:stop_time"`
	TaskBatch            int32     `json:"taskBatch" gorm:"column:task_batch"`
	SchedulePlanID       int64     `json:"schedulePlanId" gorm:"column:schedule_plan_id;not null"`
	Repeat               string    `json:"repeat" gorm:"column:repeat"`
	ScheduleGroupID      int64     `json:"scheduleGroupId" gorm:"column:schedule_group_id;not null"`
}

func (ScheduleTaskModel) TableName() string {
	return "schedule_tasks"
}
