package entity

import "errors"

type CalendarExceptionEntity struct {
	BaseEntity
	UserID   int64  `json:"userId"`
	DateMs   int64  `json:"dateMs"`
	StartMin int    `json:"startMin"`
	EndMin   int    `json:"endMin"`
	Type     string `json:"type"`
}

func (c *CalendarExceptionEntity) IsNew() bool {
	return c.ID == 0
}

func (c *CalendarExceptionEntity) IsValid() bool {
	return c.DateMs > 0 &&
		c.StartMin >= 0 && c.EndMin <= 24*60 &&
		c.StartMin < c.EndMin
}

func (c *CalendarExceptionEntity) BelongsToUser(userID int64) bool {
	return c.UserID == userID
}

func (c *CalendarExceptionEntity) ValidateItem(userID int64) error {
	if !c.BelongsToUser(userID) {
		return errors.New("userId mismatch in exception item")
	}
	if !c.IsValid() {
		return errors.New("invalid time range")
	}
	return nil
}

func (c *CalendarExceptionEntity) OverlapsWith(other *CalendarExceptionEntity) bool {
	if c.DateMs != other.DateMs {
		return false
	}
	return c.StartMin < other.EndMin && other.StartMin < c.EndMin
}
