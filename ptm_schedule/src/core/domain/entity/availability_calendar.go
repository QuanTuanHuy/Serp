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

// DefaultWorkdayAvailability returns default 9-17 availability for weekdays (Mon-Fri)
// Used when user hasn't configured their availability yet
func DefaultWorkdayAvailability(userID int64) []*AvailabilityCalendarEntity {
	defaults := make([]*AvailabilityCalendarEntity, 0, 5)
	// Monday (1) to Friday (5)
	for day := 1; day <= 5; day++ {
		defaults = append(defaults, &AvailabilityCalendarEntity{
			UserID:       userID,
			DayOfWeek:    day,
			StartMin:     9 * 60,
			EndMin:       17 * 60,
			ActiveStatus: enum.Active,
		})
	}
	return defaults
}

func DefaultFullDayAvailability(userID int64) []*AvailabilityCalendarEntity {
	defaults := make([]*AvailabilityCalendarEntity, 0, 7)
	for day := 0; day <= 6; day++ {
		defaults = append(defaults, &AvailabilityCalendarEntity{
			UserID:       userID,
			DayOfWeek:    day,
			StartMin:     8 * 60,
			EndMin:       22 * 60,
			ActiveStatus: enum.Active,
		})
	}
	return defaults
}
