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

type MoveEventResult struct {
	Event             *dom.ScheduleEventEntity
	HasConflicts      bool
	ConflictingEvents []*dom.ScheduleEventEntity
}

type CompleteEventResult struct {
	Event             *dom.ScheduleEventEntity
	AllPartsCompleted bool
	RemainingParts    int
	ScheduleTaskID    int64
	TotalActualMin    int
}

type SplitEventResult struct {
	OriginalEvent *dom.ScheduleEventEntity
	NewEvent      *dom.ScheduleEventEntity
	TotalParts    int
}

type IScheduleEventService interface {
	GetByID(ctx context.Context, id int64) (*dom.ScheduleEventEntity, error)
	ListEventsByPlanAndDateRange(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error)
	GetEventsByScheduleTaskID(ctx context.Context, scheduleTaskID int64) ([]*dom.ScheduleEventEntity, error)
	GetPinnedEvents(ctx context.Context, planID int64) ([]*dom.ScheduleEventEntity, error)

	ValidateEvents(planID int64, events []*dom.ScheduleEventEntity) error
	ValidateNoOverlapWithExisting(ctx context.Context, planID int64, events []*dom.ScheduleEventEntity) error
	ValidateStatusUpdate(status enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error
	FindConflictingEvents(ctx context.Context, planID int64, dateMs int64, startMin, endMin int, excludeEventID int64) ([]*dom.ScheduleEventEntity, error)

	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error
	UpdateEventStatus(ctx context.Context, tx *gorm.DB, eventID int64, newStatus enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error
	DeleteByPlanID(ctx context.Context, tx *gorm.DB, planID int64) error

	MoveAndPinEvent(ctx context.Context, tx *gorm.DB, eventID int64, newDateMs int64, newStartMin, newEndMin int) (*MoveEventResult, error)
	CompleteEvent(ctx context.Context, tx *gorm.DB, eventID int64, actualStartMin, actualEndMin int) (*CompleteEventResult, error)
	SplitEvent(ctx context.Context, tx *gorm.DB, eventID int64, splitPointMin int, minSplitDuration int) (*SplitEventResult, error)
	SkipEvent(ctx context.Context, tx *gorm.DB, eventID int64) error

	CloneEventsForPlan(ctx context.Context, tx *gorm.DB, newPlanID int64, events []*dom.ScheduleEventEntity,
		taskIDMapping map[int64]int64) ([]*dom.ScheduleEventEntity, error)
}

type ScheduleEventService struct {
	store port.IScheduleEventPort
}

func (s *ScheduleEventService) GetByID(ctx context.Context, id int64) (*dom.ScheduleEventEntity, error) {
	event, err := s.store.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if event == nil {
		return nil, errors.New(constant.EventNotFound)
	}
	return event, nil
}

func (s *ScheduleEventService) ListEventsByPlanAndDateRange(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error) {
	return s.store.ListEventsByPlanAndDateRange(ctx, planID, fromDateMs, toDateMs)
}

func (s *ScheduleEventService) GetEventsByScheduleTaskID(ctx context.Context, scheduleTaskID int64) ([]*dom.ScheduleEventEntity, error) {
	return s.store.GetByScheduleTaskID(ctx, scheduleTaskID)
}

func (s *ScheduleEventService) GetPinnedEvents(ctx context.Context, planID int64) ([]*dom.ScheduleEventEntity, error) {
	return s.store.GetPinnedEvents(ctx, planID)
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
			ev.Status = enum.ScheduleEventPlanned
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
		enum.ScheduleEventPlanned: true,
		enum.ScheduleEventDone:    true,
		enum.ScheduleEventSkipped: true,
	}
	if !validStatuses[status] {
		return errors.New(constant.EventInvalidStatus)
	}

	if status == enum.ScheduleEventDone {
		if actualStartMin == nil || actualEndMin == nil {
			return errors.New(constant.EventInvalidActualTime)
		}
		if *actualStartMin < 0 || *actualEndMin > 24*60 || *actualStartMin >= *actualEndMin {
			return errors.New(constant.EventInvalidActualTimeRange)
		}
	}

	return nil
}

func (s *ScheduleEventService) FindConflictingEvents(ctx context.Context, planID int64, dateMs int64, startMin, endMin int, excludeEventID int64) ([]*dom.ScheduleEventEntity, error) {
	events, err := s.store.ListEventsByPlanAndDateRange(ctx, planID, dateMs, dateMs)
	if err != nil {
		return nil, err
	}

	var conflicts []*dom.ScheduleEventEntity
	for _, ev := range events {
		if ev.ID == excludeEventID {
			continue
		}
		// if ev.Status == enum.ScheduleEventDone || ev.Status == enum.ScheduleEventSkipped {
		// 	continue
		// }
		if startMin < ev.EndMin && endMin > ev.StartMin {
			conflicts = append(conflicts, ev)
		}
	}
	return conflicts, nil
}

func (s *ScheduleEventService) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error {
	return s.store.CreateBatch(ctx, tx, items)
}

func (s *ScheduleEventService) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error {
	return s.store.UpdateBatch(ctx, tx, items)
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

func (s *ScheduleEventService) MoveAndPinEvent(ctx context.Context, tx *gorm.DB, eventID int64, newDateMs int64, newStartMin, newEndMin int) (*MoveEventResult, error) {
	event, err := s.GetByID(ctx, eventID)
	if err != nil {
		return nil, err
	}

	if !event.CanBeModified() {
		return nil, errors.New(constant.EventCannotBeModified)
	}

	conflicts, err := s.FindConflictingEvents(ctx, event.SchedulePlanID, newDateMs, newStartMin, newEndMin, eventID)
	if err != nil {
		return nil, err
	}

	if err := event.MoveAndPin(newDateMs, newStartMin, newEndMin); err != nil {
		return nil, err
	}

	if err := s.store.UpdateBatch(ctx, tx, []*dom.ScheduleEventEntity{event}); err != nil {
		return nil, err
	}

	return &MoveEventResult{
		Event:             event,
		HasConflicts:      len(conflicts) > 0,
		ConflictingEvents: conflicts,
	}, nil
}

func (s *ScheduleEventService) CompleteEvent(ctx context.Context, tx *gorm.DB, eventID int64, actualStartMin, actualEndMin int) (*CompleteEventResult, error) {
	event, err := s.GetByID(ctx, eventID)
	if err != nil {
		return nil, err
	}

	if err := event.MarkDone(actualStartMin, actualEndMin); err != nil {
		return nil, err
	}

	if err := s.store.UpdateBatch(ctx, tx, []*dom.ScheduleEventEntity{event}); err != nil {
		return nil, err
	}

	pendingCount, err := s.store.CountPendingEventsByScheduleTaskID(ctx, event.ScheduleTaskID)
	if err != nil {
		return nil, err
	}

	allEvents, err := s.store.GetByScheduleTaskID(ctx, event.ScheduleTaskID)
	if err != nil {
		return nil, err
	}

	totalActualMin := 0
	for _, ev := range allEvents {
		if ev.IsDone() && ev.HasActualTimes() {
			totalActualMin += ev.GetActualDuration()
		}
	}

	return &CompleteEventResult{
		Event:             event,
		AllPartsCompleted: pendingCount == 0,
		RemainingParts:    int(pendingCount),
		ScheduleTaskID:    event.ScheduleTaskID,
		TotalActualMin:    totalActualMin,
	}, nil
}

func (s *ScheduleEventService) SplitEvent(ctx context.Context, tx *gorm.DB, eventID int64, splitPointMin int, minSplitDuration int) (*SplitEventResult, error) {
	if minSplitDuration <= 0 {
		minSplitDuration = dom.DefaultMinSplitDuration
	}

	event, err := s.GetByID(ctx, eventID)
	if err != nil {
		return nil, err
	}

	if !event.CanSplit(minSplitDuration) {
		return nil, errors.New(constant.EventCannotBeSplit)
	}

	newPart, err := event.Split(splitPointMin, minSplitDuration)
	if err != nil {
		return nil, errors.New(constant.EventInvalidSplitPoint)
	}

	newTotalParts := event.TotalParts + 1

	if err := s.store.IncrementPartIndexAfter(ctx, tx, event.ScheduleTaskID, event.PartIndex); err != nil {
		return nil, err
	}

	if err := s.store.UpdateTotalPartsForTask(ctx, tx, event.ScheduleTaskID, newTotalParts); err != nil {
		return nil, err
	}

	event.TotalParts = newTotalParts
	newPart.PartIndex = event.PartIndex + 1
	newPart.TotalParts = newTotalParts

	if err := s.store.UpdateBatch(ctx, tx, []*dom.ScheduleEventEntity{event}); err != nil {
		return nil, err
	}

	if err := s.store.CreateBatch(ctx, tx, []*dom.ScheduleEventEntity{newPart}); err != nil {
		return nil, err
	}

	return &SplitEventResult{
		OriginalEvent: event,
		NewEvent:      newPart,
		TotalParts:    newTotalParts,
	}, nil
}

func (s *ScheduleEventService) SkipEvent(ctx context.Context, tx *gorm.DB, eventID int64) error {
	event, err := s.GetByID(ctx, eventID)
	if err != nil {
		return err
	}

	if err := event.MarkSkipped(); err != nil {
		return err
	}

	return s.store.UpdateBatch(ctx, tx, []*dom.ScheduleEventEntity{event})
}

func (s *ScheduleEventService) CloneEventsForPlan(ctx context.Context, tx *gorm.DB, newPlanID int64, events []*dom.ScheduleEventEntity,
	taskIDMapping map[int64]int64) ([]*dom.ScheduleEventEntity, error) {
	clonedEvents := make([]*dom.ScheduleEventEntity, 0, len(events))
	for _, ev := range events {
		clone := ev.Clone()
		clone.SchedulePlanID = newPlanID
		clone.ID = 0
		if newTaskID, ok := taskIDMapping[ev.ScheduleTaskID]; ok {
			clone.ScheduleTaskID = newTaskID
		}
		clonedEvents = append(clonedEvents, clone)
	}
	if err := s.store.CreateBatch(ctx, tx, clonedEvents); err != nil {
		return nil, err
	}
	return clonedEvents, nil
}

func (s *ScheduleEventService) DeleteByPlanID(ctx context.Context, tx *gorm.DB, planID int64) error {
	return s.store.DeleteByPlanID(ctx, tx, planID)
}

func NewScheduleEventService(store port.IScheduleEventPort) IScheduleEventService {
	return &ScheduleEventService{store: store}
}
