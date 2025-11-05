/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import "time"

func DayStartUTC(ms int64) time.Time {
	t := time.UnixMilli(ms).UTC()
	y, mon, d := t.Date()
	return time.Date(y, mon, d, 0, 0, 0, 0, time.UTC)
}

func DateMsFromDate(d time.Time) int64 {
	dt := time.Date(d.Year(), d.Month(), d.Day(), 0, 0, 0, 0, time.UTC)
	return dt.UnixMilli()
}

func TimeMsFromTime(t time.Time) int64 {
	return t.UTC().UnixMilli()
}

func TimeFromMs(ms int64) time.Time {
	return time.UnixMilli(ms).UTC()
}
