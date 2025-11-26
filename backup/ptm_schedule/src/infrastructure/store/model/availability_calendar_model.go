/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type AvailabilityCalendarModel struct {
	BaseModel
	UserID       int64  `gorm:"index:idx_ac_user_day,priority:1;not null"`
	DayOfWeek    int    `gorm:"index:idx_ac_user_day,priority:2;not null"`
	StartMin     int    `gorm:"not null"`
	EndMin       int    `gorm:"not null"`
	ActiveStatus string `gorm:"type:varchar(20);not null"`
}

func (AvailabilityCalendarModel) TableName() string {
	return "availability_calendars"
}
