/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"

	"github.com/serp/ptm-schedule/src/core/domain/constant"
	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type IScheduleEventService interface {
	GetByID(ctx context.Context, id int64) (*dom.ScheduleEventEntity, error)
	ListEventsByPlanAndDateRange(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error)
	ValidateEvents(planID int64, events []*dom.ScheduleEventEntity) error
	ValidateNoOverlapWithExisting(ctx context.Context, planID int64, events []*dom.ScheduleEventEntity) error
	ValidateStatusUpdate(status enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error
	UpdateEventStatus(ctx context.Context, tx *gorm.DB, eventID int64, newStatus enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error
}

type ScheduleEventService struct {
	store port.IScheduleEventStorePort
}

func (s *ScheduleEventService) GetByID(ctx context.Context, id int64) (*dom.ScheduleEventEntity, error) {
	return s.store.GetByID(ctx, id)
}

func (s *ScheduleEventService) ListEventsByPlanAndDateRange(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error) {
	return s.store.ListEventsByPlanAndDateRange(ctx, planID, fromDateMs, toDateMs)
}

func (s *ScheduleEventService) ValidateEvents(planID int64, events []*dom.ScheduleEventEntity) error {
	for _, ev := range events {
		if !ev.BelongsToPlan(planID) {
			return errors.New(constant.EventPlanIDMismatch)
		}
		if !ev.IsValid() {
			return errors.New(constant.EventInvalidItem)
		}
		if ev.Status == "" {
			ev.Status = enum.PLANNED
		}
	}

	for i := 0; i < len(events); i++ {
		for j := i + 1; j < len(events); j++ {
			if events[i].OverlapsWith(events[j]) {
				return errors.New(constant.EventItemsOverlap)
			}
		}
	}

	return nil
}

func (s *ScheduleEventService) ValidateNoOverlapWithExisting(ctx context.Context, planID int64, events []*dom.ScheduleEventEntity) error {
	if len(events) == 0 {
		return nil
	}

	minDate := events[0].DateMs
	maxDate := events[0].DateMs
	for _, ev := range events {
		if ev.DateMs < minDate {
			minDate = ev.DateMs
		}
		if ev.DateMs > maxDate {
			maxDate = ev.DateMs
		}
	}
	existing, err := s.ListEventsByPlanAndDateRange(ctx, planID, minDate, maxDate)
	if err != nil {
		return err
	}

	for _, newEvent := range events {
		for _, existingEvent := range existing {
			if newEvent.ID > 0 && newEvent.ID == existingEvent.ID {
				continue
			}
			if newEvent.OverlapsWith(existingEvent) {
				return errors.New(constant.EventOverlapWithExisting)
			}
		}
	}

	return nil
}

func (s *ScheduleEventService) ValidateStatusUpdate(status enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error {
	validStatuses := map[enum.ScheduleEventStatus]bool{
		enum.PLANNED: true,
		enum.DONE:    true,
		enum.SKIPPED: true,
	}
	if !validStatuses[status] {
		return errors.New(constant.EventInvalidStatus)
	}

	if status == enum.DONE {
		if actualStartMin == nil || actualEndMin == nil {
			return errors.New(constant.EventInvalidActualTime)
		}
		if *actualStartMin < 0 || *actualEndMin > 24*60 || *actualStartMin >= *actualEndMin {
			return errors.New(constant.EventInvalidActualTimeRange)
		}
	}

	return nil
}

func (s *ScheduleEventService) UpdateEventStatus(ctx context.Context, tx *gorm.DB, eventID int64, newStatus enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error {
	event, err := s.GetByID(ctx, eventID)
	if err != nil {
		return err
	}

	if !event.CanTransitionTo(newStatus) {
		return errors.New(constant.EventInvalidStatusTransition)
	}
	if !event.SetStatus(newStatus, actualStartMin, actualEndMin) {
		return errors.New(constant.EventStatusUpdateFailed)
	}

	return s.store.UpdateBatch(ctx, tx, []*dom.ScheduleEventEntity{event})
}

func (s *ScheduleEventService) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error {
	return s.store.CreateBatch(ctx, tx, items)
}

func (s *ScheduleEventService) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error {
	return s.store.UpdateBatch(ctx, tx, items)
}

func NewScheduleEventService(store port.IScheduleEventStorePort) IScheduleEventService {
	return &ScheduleEventService{store: store}
}
