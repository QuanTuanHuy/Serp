package model

import "time"

type SchedulePlanModel struct {
	BaseModel
	UserID            int64      `json:"userId" gorm:"not null"`
	StartDate         time.Time  `json:"startDate" gorm:"not null"`
	EndDate           *time.Time `json:"endDate" gorm:""`
	ActiveStatus      string     `json:"activeStatus" gorm:"not null"`
	ActiveTaskBatch   int32      `json:"activeTaskBatch" gorm:"not null"`
	IsActiveTaskBatch bool       `json:"isActiveTaskBatch" gorm:"not null"`
}

func (SchedulePlanModel) TableName() string {
	return "schedule_plans"
}
