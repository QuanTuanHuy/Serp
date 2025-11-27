package entity

type ScheduleWindowEntity struct {
	BaseEntity
	UserID   int64 `json:"userId"`
	DateMs   int64 `json:"dateMs"`
	StartMin int   `json:"startMin"`
	EndMin   int   `json:"endMin"`
}

func (w *ScheduleWindowEntity) IsNew() bool {
	return w.ID == 0
}

func (w *ScheduleWindowEntity) IsValid() bool {
	return w.DateMs > 0 &&
		w.StartMin >= 0 && w.EndMin <= 24*60 &&
		w.StartMin < w.EndMin
}

func (w *ScheduleWindowEntity) BelongsToUser(userID int64) bool {
	return w.UserID == userID
}

func (w *ScheduleWindowEntity) DurationMinutes() int {
	return w.EndMin - w.StartMin
}
