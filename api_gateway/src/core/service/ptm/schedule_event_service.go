/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
)

type IScheduleEventService interface {
	ListEvents(ctx context.Context, planID, fromDateMs, toDateMs int64) (*response.BaseResponse, error)
	SaveEvents(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	ManuallyMoveEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error)
	CompleteEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error)
	SplitEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error)
}

type ScheduleEventService struct {
	scheduleEventClient port.IScheduleEventClientPort
}

func (s *ScheduleEventService) ListEvents(ctx context.Context, planID, fromDateMs, toDateMs int64) (*response.BaseResponse, error) {
	res, err := s.scheduleEventClient.ListEvents(ctx, planID, fromDateMs, toDateMs)
	if err != nil {
		log.Error(ctx, "ScheduleEventService: ListEvents error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ScheduleEventService) SaveEvents(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := s.scheduleEventClient.SaveEvents(ctx, payload)
	if err != nil {
		log.Error(ctx, "ScheduleEventService: SaveEvents error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ScheduleEventService) ManuallyMoveEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := s.scheduleEventClient.ManuallyMoveEvent(ctx, eventID, payload)
	if err != nil {
		log.Error(ctx, "ScheduleEventService: ManuallyMoveEvent error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ScheduleEventService) CompleteEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := s.scheduleEventClient.CompleteEvent(ctx, eventID, payload)
	if err != nil {
		log.Error(ctx, "ScheduleEventService: CompleteEvent error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ScheduleEventService) SplitEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := s.scheduleEventClient.SplitEvent(ctx, eventID, payload)
	if err != nil {
		log.Error(ctx, "ScheduleEventService: SplitEvent error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewScheduleEventService(scheduleEventClient port.IScheduleEventClientPort) IScheduleEventService {
	return &ScheduleEventService{
		scheduleEventClient: scheduleEventClient,
	}
}
