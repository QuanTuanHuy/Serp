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

type IScheduleWindowService interface {
	ListAvailabilityWindows(ctx context.Context, fromDateMs, toDateMs int64) (*response.BaseResponse, error)
	MaterializeWindows(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
}

type ScheduleWindowService struct {
	scheduleWindowClient port.IScheduleWindowClientPort
}

func (s *ScheduleWindowService) ListAvailabilityWindows(ctx context.Context, fromDateMs, toDateMs int64) (*response.BaseResponse, error) {
	res, err := s.scheduleWindowClient.ListAvailabilityWindows(ctx, fromDateMs, toDateMs)
	if err != nil {
		log.Error(ctx, "ScheduleWindowService: ListAvailabilityWindows error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ScheduleWindowService) MaterializeWindows(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := s.scheduleWindowClient.MaterializeWindows(ctx, payload)
	if err != nil {
		log.Error(ctx, "ScheduleWindowService: MaterializeWindows error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewScheduleWindowService(scheduleWindowClient port.IScheduleWindowClientPort) IScheduleWindowService {
	return &ScheduleWindowService{
		scheduleWindowClient: scheduleWindowClient,
	}
}
