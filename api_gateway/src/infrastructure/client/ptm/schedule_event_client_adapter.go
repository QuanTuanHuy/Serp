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

type ScheduleEventClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (s *ScheduleEventClientAdapter) ListEvents(ctx context.Context, planID, fromDateMs, toDateMs int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	queryParams := map[string]string{
		"planId":     fmt.Sprintf("%d", planID),
		"fromDateMs": fmt.Sprintf("%d", fromDateMs),
		"toDateMs":   fmt.Sprintf("%d", toDateMs),
	}

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GETWithQuery(ctx, "/api/v1/schedule-events", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call list events API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ListEvents API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *ScheduleEventClientAdapter) SaveEvents(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/schedule-events", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call save events API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("SaveEvents API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *ScheduleEventClientAdapter) ManuallyMoveEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/schedule-events/%d/move", eventID)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call manually move event API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ManuallyMoveEvent API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *ScheduleEventClientAdapter) CompleteEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/schedule-events/%d/complete", eventID)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call complete event API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CompleteEvent API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *ScheduleEventClientAdapter) SplitEvent(ctx context.Context, eventID int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/schedule-events/%d/split", eventID)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call split event API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("SplitEvent API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func NewScheduleEventClientAdapter(props *properties.ExternalServiceProperties) port.IScheduleEventClientPort {
	baseURL := "http://" + props.PTMSchedule.Host + ":" + props.PTMSchedule.Port + "/ptm-schedule"
	apiClient := utils.NewBaseAPIClient(baseURL, props.PTMSchedule.Timeout)
	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ScheduleEventClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
