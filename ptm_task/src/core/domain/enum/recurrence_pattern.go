/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type RecurrencePattern string

const (
	RecurrenceNone    RecurrencePattern = "NONE"
	RecurrenceDaily   RecurrencePattern = "DAILY"
	RecurrenceWeekly  RecurrencePattern = "WEEKLY"
	RecurrenceMonthly RecurrencePattern = "MONTHLY"
	RecurrenceCustom  RecurrencePattern = "CUSTOM"
)

func (r RecurrencePattern) IsValid() bool {
	switch r {
	case RecurrenceNone, RecurrenceDaily, RecurrenceWeekly, RecurrenceMonthly, RecurrenceCustom:
		return true
	}
	return false
}

func (r RecurrencePattern) IsRecurring() bool {
	return r != RecurrenceNone
}
