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

type IAvailabilityCalendarService interface {
	GetAvailability(ctx context.Context) (*response.BaseResponse, error)
	SetAvailability(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	ReplaceAvailability(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
}

type AvailabilityCalendarService struct {
	availabilityClient port.IAvailabilityCalendarClientPort
}

func (a *AvailabilityCalendarService) GetAvailability(ctx context.Context) (*response.BaseResponse, error) {
	res, err := a.availabilityClient.GetAvailability(ctx)
	if err != nil {
		log.Error(ctx, "AvailabilityCalendarService: GetAvailability error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (a *AvailabilityCalendarService) SetAvailability(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := a.availabilityClient.SetAvailability(ctx, payload)
	if err != nil {
		log.Error(ctx, "AvailabilityCalendarService: SetAvailability error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (a *AvailabilityCalendarService) ReplaceAvailability(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := a.availabilityClient.ReplaceAvailability(ctx, payload)
	if err != nil {
		log.Error(ctx, "AvailabilityCalendarService: ReplaceAvailability error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewAvailabilityCalendarService(availabilityClient port.IAvailabilityCalendarClientPort) IAvailabilityCalendarService {
	return &AvailabilityCalendarService{
		availabilityClient: availabilityClient,
	}
}
