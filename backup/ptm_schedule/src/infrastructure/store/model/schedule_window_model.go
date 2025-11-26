/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import (
	"time"
)

type ScheduleWindowModel struct {
	BaseModel
	UserID   int64     `gorm:"index:idx_sw_user_date,priority:1;not null"`
	Date     time.Time `gorm:"type:date;index:idx_sw_user_date,priority:2;not null"`
	StartMin int       `gorm:"not null"`
	EndMin   int       `gorm:"not null"`
}

func (ScheduleWindowModel) TableName() string {
	return "schedule_windows"
}
