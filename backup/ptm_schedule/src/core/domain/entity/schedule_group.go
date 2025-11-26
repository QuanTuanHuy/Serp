package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type ScheduleGroupEntity struct {
	BaseEntity
	SchedulePlanID  int64             `json:"schedulePlanId"`
	ProjectID       *int64            `json:"projectId"`
	GroupTaskID     *int64            `json:"groupTaskId"`
	Title           string            `json:"title"`
	Priority        []enum.Priority   `json:"priority"`
	Status          enum.Status       `json:"status"`
	StartHour       *int32            `json:"startHour"`
	StartMinute     *int32            `json:"startMinute"`
	EndHour         *int32            `json:"endHour"`
	EndMinute       *int32            `json:"endMinute"`
	Duration        float64           `json:"duration"`
	PreferenceLevel int32             `json:"preferenceLevel"`
	Repeat          []int32           `json:"repeat"`
	IsNotify        bool              `json:"isNotify"`
	ActiveStatus    enum.ActiveStatus `json:"activeStatus"`
	IsFailed        bool              `json:"isFailed"`
	RotationDay     *int64            `json:"rotationDay"`
}
