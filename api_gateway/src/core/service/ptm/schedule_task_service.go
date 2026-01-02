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

type IScheduleTaskService interface {
	GetScheduleTasksByPlanID(ctx context.Context, params map[string]string) (*response.BaseResponse, error)
}

type ScheduleTaskService struct {
	scheduleTaskClient port.IScheduleTaskClientPort
}

func (s *ScheduleTaskService) GetScheduleTasksByPlanID(ctx context.Context, params map[string]string) (*response.BaseResponse, error) {
	res, err := s.scheduleTaskClient.GetScheduleTasksByPlanID(ctx, params)
	if err != nil {
		log.Error(ctx, "ScheduleTaskService: GetScheduleTasksByPlanID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewScheduleTaskService(scheduleTaskClient port.IScheduleTaskClientPort) IScheduleTaskService {
	return &ScheduleTaskService{
		scheduleTaskClient: scheduleTaskClient,
	}
}
