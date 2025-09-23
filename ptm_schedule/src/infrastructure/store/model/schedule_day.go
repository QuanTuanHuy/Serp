/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type ScheduleDayModel struct {
	BaseModel
	UserID           int64   `gorm:"column:user_id;not_null;index" json:"userId"`
	StartTime        string  `gorm:"column:start_time;not_null" json:"startTime"`
	EndTime          string  `gorm:"column:end_time;not_null" json:"endTime"`
	PrimaryTaskID    *int64  `gorm:"column:primary_task_id" json:"primaryTaskId"`
	PrimaryTaskTitle *string `gorm:"column:primary_task_title" json:"primaryTaskTitle"`
	BackupTaskID     *int64  `gorm:"column:backup_task_id" json:"backupTaskId"`
	BackupTaskTitle  *string `gorm:"column:backup_task_title" json:"backupTaskTitle"`
	Tag              *string `gorm:"column:tag" json:"tag"`
	WeekDay          int     `gorm:"column:week_day;not_null" json:"weekDay"`
	TimeBubbleID     int64   `gorm:"column:time_bubble_id;not_null" json:"timeBubbleId"`
}

func (ScheduleDayModel) TableName() string {
	return "schedule_days"
}
