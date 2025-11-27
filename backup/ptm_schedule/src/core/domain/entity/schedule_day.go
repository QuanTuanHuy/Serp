/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type ScheduleDayEntity struct {
	BaseEntity
	UserID           int64     `json:"userId"`
	StartTime        string    `json:"startTime"` // Format: HH:MM:SS
	EndTime          string    `json:"endTime"`
	PrimaryTaskID    *int64    `json:"primaryTaskId"`
	PrimaryTaskTitle *string   `json:"primaryTaskTitle"`
	BackupTaskID     *int64    `json:"backupTaskId"`
	BackupTaskTitle  *string   `json:"backupTaskTitle"`
	Tag              *enum.Tag `json:"tag"`
	WeekDay          int       `json:"weekDay"`
	TimeBubbleID     int64     `json:"timeBubbleId"`
}
