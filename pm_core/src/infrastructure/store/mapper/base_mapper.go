/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"time"
)

type BaseMapper struct{}

func NewBaseMapper() *BaseMapper {
	return &BaseMapper{}
}

func (m *BaseMapper) UnixMilliToTime(unixMilli int64) time.Time {
	return time.UnixMilli(unixMilli)
}

func (m *BaseMapper) UnixMilliToTimePtr(unixMilli *int64) *time.Time {
	if unixMilli == nil {
		return nil
	}
	t := time.UnixMilli(*unixMilli)
	return &t
}

func (m *BaseMapper) TimeToUnixMilli(t time.Time) int64 {
	return t.UnixMilli()
}

func (m *BaseMapper) TimePtrToUnixMilli(t *time.Time) *int64 {
	if t == nil {
		return nil
	}
	unixMilli := t.UnixMilli()
	return &unixMilli
}
