/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"sort"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/kernel/utils"
	"gorm.io/gorm"
)

type IScheduleWindowService interface {
	ListAvailabilityWindows(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleWindowEntity, error)
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleWindowEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleWindowEntity) error
	DeleteByDateRange(ctx context.Context, tx *gorm.DB, userID int64, fromDateMs, toDateMs int64) error
	// Business logic methods
	ExpandAvailabilityToWindows(availCalendar []*dom.AvailabilityCalendarEntity, fromDateMs, toDateMs int64) []*dom.ScheduleWindowEntity
	SubtractExceptions(windows []*dom.ScheduleWindowEntity, exceptions []*dom.CalendarExceptionEntity) []*dom.ScheduleWindowEntity
}

type ScheduleWindowService struct {
	store port.IScheduleWindowStorePort
}

func (s *ScheduleWindowService) DeleteByDateRange(ctx context.Context, tx *gorm.DB, userID int64, fromDateMs int64, toDateMs int64) error {
	return s.store.DeleteByDateRange(ctx, tx, userID, fromDateMs, toDateMs)
}

func (s *ScheduleWindowService) ListAvailabilityWindows(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleWindowEntity, error) {
	return s.store.ListAvailabilityWindows(ctx, userID, fromDateMs, toDateMs)
}

func (s *ScheduleWindowService) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleWindowEntity) error {
	return s.store.CreateBatch(ctx, tx, items)
}

func (s *ScheduleWindowService) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleWindowEntity) error {
	return s.store.UpdateBatch(ctx, tx, items)
}

func (s *ScheduleWindowService) ExpandAvailabilityToWindows(
	availCalendar []*dom.AvailabilityCalendarEntity,
	fromDateMs, toDateMs int64,
) []*dom.ScheduleWindowEntity {
	byDayOfWeek := make(map[int][]*dom.AvailabilityCalendarEntity)
	for _, avail := range availCalendar {
		byDayOfWeek[avail.DayOfWeek] = append(byDayOfWeek[avail.DayOfWeek], avail)
	}

	var windows []*dom.ScheduleWindowEntity

	currentDateMs := utils.DayStartUTC(fromDateMs)
	endDateMs := utils.DayStartUTC(toDateMs)

	for currentDateMs <= endDateMs {
		dayOfWeek := utils.GetDayOfWeek(currentDateMs)

		if availList, ok := byDayOfWeek[dayOfWeek]; ok {
			for _, avail := range availList {
				windows = append(windows, &dom.ScheduleWindowEntity{
					UserID:   avail.UserID,
					DateMs:   currentDateMs,
					StartMin: avail.StartMin,
					EndMin:   avail.EndMin,
				})
			}
		}

		currentDateMs += 24 * 60 * 60 * 1000
	}

	return windows
}

func (s *ScheduleWindowService) SubtractExceptions(
	windows []*dom.ScheduleWindowEntity,
	exceptions []*dom.CalendarExceptionEntity,
) []*dom.ScheduleWindowEntity {
	if len(exceptions) == 0 {
		return windows
	}

	exceptionsByDate := make(map[int64][]*dom.CalendarExceptionEntity)
	for _, ex := range exceptions {
		dateKey := utils.DayStartUTC(ex.DateMs)
		exceptionsByDate[dateKey] = append(exceptionsByDate[dateKey], ex)
	}
	for _, exList := range exceptionsByDate {
		sort.Slice(exList, func(i, j int) bool {
			return exList[i].StartMin < exList[j].StartMin
		})
	}

	var result []*dom.ScheduleWindowEntity

	for _, win := range windows {
		dateKey := utils.DayStartUTC(win.DateMs)
		exList, hasExceptions := exceptionsByDate[dateKey]

		if !hasExceptions {
			result = append(result, win)
			continue
		}

		remaining := s.subtractIntervalsFromWindow(win, exList)
		result = append(result, remaining...)
	}

	return result
}

func (s *ScheduleWindowService) subtractIntervalsFromWindow(
	win *dom.ScheduleWindowEntity,
	exceptions []*dom.CalendarExceptionEntity,
) []*dom.ScheduleWindowEntity {
	var result []*dom.ScheduleWindowEntity
	currentStart := win.StartMin
	currentEnd := win.EndMin

	for _, ex := range exceptions {
		if ex.EndMin <= currentStart || ex.StartMin >= currentEnd {
			continue
		}

		if ex.StartMin > currentStart {
			result = append(result, &dom.ScheduleWindowEntity{
				UserID:   win.UserID,
				DateMs:   win.DateMs,
				StartMin: currentStart,
				EndMin:   utils.MinInt(ex.StartMin, currentEnd),
			})
		}

		if ex.EndMin < currentEnd {
			currentStart = ex.EndMin
		} else {
			currentStart = currentEnd
			break
		}
	}

	if currentStart < currentEnd {
		result = append(result, &dom.ScheduleWindowEntity{
			UserID:   win.UserID,
			DateMs:   win.DateMs,
			StartMin: currentStart,
			EndMin:   currentEnd,
		})
	}

	return result
}

func NewScheduleWindowService(store port.IScheduleWindowStorePort) IScheduleWindowService {
	return &ScheduleWindowService{store: store}
}
