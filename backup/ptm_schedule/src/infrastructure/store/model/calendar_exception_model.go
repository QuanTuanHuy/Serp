/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type CalendarExceptionModel struct {
	BaseModel
	UserID   int64     `gorm:"index:idx_ce_user_date,priority:1;not null"`
	Date     time.Time `gorm:"type:date;index:idx_ce_user_date,priority:2;not null"`
	StartMin int       `gorm:"not null"`
	EndMin   int       `gorm:"not null"`
	Type     string    `gorm:"type:varchar(30)"`
}

func (CalendarExceptionModel) TableName() string {
	return "calendar_exceptions"
}
