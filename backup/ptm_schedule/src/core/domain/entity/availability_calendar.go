package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type AvailabilityCalendarEntity struct {
	BaseEntity
	UserID       int64             `json:"userId"`
	DayOfWeek    int               `json:"dayOfWeek"`
	StartMin     int               `json:"startMin"`
	EndMin       int               `json:"endMin"`
	ActiveStatus enum.ActiveStatus `json:"activeStatus"`
}

func (e *AvailabilityCalendarEntity) IsNew() bool {
	return e.ID == 0
}

func (e *AvailabilityCalendarEntity) IsValid() bool {
	return e.DayOfWeek >= 0 && e.DayOfWeek <= 6 &&
		e.StartMin >= 0 && e.EndMin <= 24*60 &&
		e.StartMin < e.EndMin
}

func (e *AvailabilityCalendarEntity) BelongsToUser(userID int64) bool {
	return e.UserID == userID
}

func (e *AvailabilityCalendarEntity) OverlapsWith(other *AvailabilityCalendarEntity) bool {
	if e.DayOfWeek != other.DayOfWeek {
		return false
	}
	return e.StartMin < other.EndMin && other.StartMin < e.EndMin
}
