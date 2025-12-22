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

type SchedulePlanClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (s *SchedulePlanClientAdapter) GetOrCreateActivePlan(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/schedule-plans", nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call get or create active plan API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOrCreateActivePlan API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) GetActivePlan(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GET(ctx, "/api/v1/schedule-plans/active", headers)
		if err != nil {
			return fmt.Errorf("failed to call get active plan API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetActivePlan API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) GetActivePlanDetail(ctx context.Context, fromDateMs, toDateMs int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	queryParams := map[string]string{
		"fromDateMs": fmt.Sprintf("%d", fromDateMs),
		"toDateMs":   fmt.Sprintf("%d", toDateMs),
	}

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GETWithQuery(ctx, "/api/v1/schedule-plans/active/detail", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get active plan detail API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetActivePlanDetail API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) GetPlanHistory(ctx context.Context, page, pageSize int) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	queryParams := map[string]string{
		"page":     fmt.Sprintf("%d", page),
		"pageSize": fmt.Sprintf("%d", pageSize),
	}

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GETWithQuery(ctx, "/api/v1/schedule-plans/history", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get plan history API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetPlanHistory API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) TriggerReschedule(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/schedule-plans/reschedule", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call trigger reschedule API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("TriggerReschedule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) GetPlanByID(ctx context.Context, planID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/schedule-plans/%d", planID)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get plan by ID API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetPlanByID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) GetPlanWithEvents(ctx context.Context, planID int64, fromDateMs, toDateMs int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/schedule-plans/%d/events", planID)
	queryParams := map[string]string{
		"fromDateMs": fmt.Sprintf("%d", fromDateMs),
		"toDateMs":   fmt.Sprintf("%d", toDateMs),
	}

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GETWithQuery(ctx, url, queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get plan with events API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetPlanWithEvents API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) ApplyProposedPlan(ctx context.Context, planID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/schedule-plans/%d/apply", planID)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, url, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call apply proposed plan API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ApplyProposedPlan API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) RevertToPlan(ctx context.Context, planID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/schedule-plans/%d/revert", planID)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, url, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call revert to plan API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("RevertToPlan API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func (s *SchedulePlanClientAdapter) DiscardProposedPlan(ctx context.Context, planID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/schedule-plans/%d", planID)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call discard proposed plan API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DiscardProposedPlan API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func NewSchedulePlanClientAdapter(props *properties.ExternalServiceProperties) port.ISchedulePlanClientPort {
	baseURL := "http://" + props.PTMSchedule.Host + ":" + props.PTMSchedule.Port + "/ptm-schedule"
	apiClient := utils.NewBaseAPIClient(baseURL, props.PTMSchedule.Timeout)
	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &SchedulePlanClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
