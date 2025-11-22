/*
Author: QuanTuanHuy
Description: Part of Serp Project - Base mapper utilities for Go services
*/

package mapper

import "time"

func TimeToEpochMilli(t time.Time) int64 {
	if t.IsZero() {
		return 0
	}
	return t.UnixMilli()
}

func EpochMilliToTime(epochMilli int64) time.Time {
	if epochMilli == 0 {
		return time.Time{}
	}
	return time.UnixMilli(epochMilli)
}

func GetCurrentEpochMilli() int64 {
	return time.Now().UnixMilli()
}
