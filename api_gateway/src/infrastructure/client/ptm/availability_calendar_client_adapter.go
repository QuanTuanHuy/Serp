/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type AvailabilityCalendarClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (a *AvailabilityCalendarClientAdapter) GetAvailability(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.GET(ctx, "/api/v1/availability-calendar", headers)
		if err != nil {
			return fmt.Errorf("failed to call get availability API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAvailability API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (a *AvailabilityCalendarClientAdapter) SetAvailability(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.POST(ctx, "/api/v1/availability-calendar", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call set availability API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("SetAvailability API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (a *AvailabilityCalendarClientAdapter) ReplaceAvailability(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.PUT(ctx, "/api/v1/availability-calendar", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call replace availability API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ReplaceAvailability API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func NewAvailabilityCalendarClientAdapter(props *properties.ExternalServiceProperties) port.IAvailabilityCalendarClientPort {
	baseURL := "http://" + props.PTMSchedule.Host + ":" + props.PTMSchedule.Port + "/ptm-schedule"
	apiClient := utils.NewBaseAPIClient(baseURL, props.PTMSchedule.Timeout)
	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &AvailabilityCalendarClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
