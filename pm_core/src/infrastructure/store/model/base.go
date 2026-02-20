/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type BaseModel struct {
	ID        int64     `gorm:"primaryKey;autoIncrement"`
	CreatedAt time.Time `gorm:"not null;autoCreateTime"`
	CreatedBy int64     `gorm:"not null"`
	UpdatedAt time.Time `gorm:"not null;autoUpdateTime"`
	UpdatedBy int64     `gorm:"not null"`
}
