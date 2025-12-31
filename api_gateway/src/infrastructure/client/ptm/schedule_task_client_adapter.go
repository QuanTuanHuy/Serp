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

type ScheduleTaskClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (s *ScheduleTaskClientAdapter) GetScheduleTasksByPlanID(ctx context.Context, params map[string]string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GETWithQuery(ctx, "/api/v1/schedule-tasks", params, headers)
		if err != nil {
			return fmt.Errorf("failed to call get schedule tasks by plan ID API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetScheduleTasksByPlanID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}
	return &result, nil
}

func NewScheduleTaskClientAdapter(props *properties.ExternalServiceProperties) port.IScheduleTaskClientPort {
	baseURL := "http://" + props.PTMSchedule.Host + ":" + props.PTMSchedule.Port + "/ptm-schedule"
	apiClient := utils.NewBaseAPIClient(baseURL, props.PTMSchedule.Timeout)
	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ScheduleTaskClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
