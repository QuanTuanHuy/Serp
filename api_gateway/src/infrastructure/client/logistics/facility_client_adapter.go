/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package adapter

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/logistics"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type FacilityClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (f *FacilityClientAdapter) CreateFacility(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = f.apiClient.POST(ctx, "/logistics/api/v1/facility/create", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create facility API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !f.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateFacility API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := f.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create facility response: %w", err)
	}
	return &result, nil
}

func (f *FacilityClientAdapter) UpdateFacility(ctx context.Context, facilityId string, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/facility/update/%s", facilityId)

	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = f.apiClient.PATCH(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update facility API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !f.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateFacility API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := f.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update facility response: %w", err)
	}
	return &result, nil
}

func (f *FacilityClientAdapter) GetFacilities(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := make(map[string]string)
	queryParams["page"] = fmt.Sprintf("%d", page)
	queryParams["size"] = fmt.Sprintf("%d", size)
	if sortBy != "" {
		queryParams["sortBy"] = sortBy
	}
	if sortDirection != "" {
		queryParams["sortDirection"] = sortDirection
	}
	if query != "" {
		queryParams["query"] = query
	}
	if statusId != "" {
		queryParams["statusId"] = statusId
	}

	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = f.apiClient.GETWithQuery(ctx, "/logistics/api/v1/facility/search", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get facilities API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !f.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetFacilities API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := f.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get facilities response: %w", err)
	}
	return &result, nil
}

func (f *FacilityClientAdapter) GetFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/facility/search/%s", facilityId)

	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = f.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get facility API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !f.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetFacility API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := f.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get facility response: %w", err)
	}
	return &result, nil
}

func (f *FacilityClientAdapter) DeleteFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/facility/delete/%s", facilityId)

	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = f.apiClient.DELETE(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete facility API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !f.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteFacility API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := f.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete facility response: %w", err)
	}
	return &result, nil
}

func NewFacilityClientAdapter(props *properties.ExternalServiceProperties) port.IFacilityClientPort {
	baseURL := fmt.Sprintf("http://%s:%s",
		props.LogisticsService.Host,
		props.LogisticsService.Port)

	return &FacilityClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.LogisticsService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
