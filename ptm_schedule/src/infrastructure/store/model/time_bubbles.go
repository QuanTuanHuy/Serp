/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type TimeBubblesModel struct {
	BaseModel
	UserID       int64  `gorm:"column:user_id;not null;index" json:"userId"`
	DayOfWeek    int    `gorm:"column:day_of_week;not null" json:"dayOfWeek"`
	DayOfWeekStr string `gorm:"column:day_of_week_str;not null" json:"dayOfWeekStr"`
	StartTime    string `gorm:"column:start_time;not null" json:"startTime"`
	EndTime      string `gorm:"column:end_time;not null" json:"endTime"`
	Tag          string `gorm:"column:tag;not null" json:"tag"`
	Status       string `gorm:"column:status;not null" json:"status"`
}

func (TimeBubblesModel) TableName() string {
	return "time_bubbles"
}
