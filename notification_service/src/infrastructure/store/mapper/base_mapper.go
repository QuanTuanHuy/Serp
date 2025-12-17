/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import "time"

func TimeMsFromTime(t time.Time) int64 {
	return t.UTC().UnixMilli()
}

func TimeFromMs(ms int64) time.Time {
	return time.UnixMilli(ms).UTC()
}

func TimePtrFromMs(ms *int64) *time.Time {
	if ms == nil {
		return nil
	}
	t := TimeFromMs(*ms)
	return &t
}

func MsPtrFromTime(t *time.Time) *int64 {
	if t == nil {
		return nil
	}
	v := TimeMsFromTime(*t)
	return &v
}
