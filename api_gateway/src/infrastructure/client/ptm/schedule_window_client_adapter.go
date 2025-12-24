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

type ScheduleWindowClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (s *ScheduleWindowClientAdapter) ListAvailabilityWindows(ctx context.Context, fromDateMs, toDateMs int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	queryParams := map[string]string{
		"fromDateMs": fmt.Sprintf("%d", fromDateMs),
		"toDateMs":   fmt.Sprintf("%d", toDateMs),
	}

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GETWithQuery(ctx, "/api/v1/schedule-windows", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call list availability windows API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ListAvailabilityWindows API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *ScheduleWindowClientAdapter) MaterializeWindows(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/schedule-windows/materialize", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call materialize windows API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("MaterializeWindows API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func NewScheduleWindowClientAdapter(props *properties.ExternalServiceProperties) port.IScheduleWindowClientPort {
	baseURL := "http://" + props.PTMSchedule.Host + ":" + props.PTMSchedule.Port + "/ptm-schedule"
	apiClient := utils.NewBaseAPIClient(baseURL, props.PTMSchedule.Timeout)
	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ScheduleWindowClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
