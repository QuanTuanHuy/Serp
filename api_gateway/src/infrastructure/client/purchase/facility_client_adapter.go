/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/purchase"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/purchase"
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
		httpResponse, err = f.apiClient.POST(ctx, "/api/v1/facility/create", req, headers)
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
	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/facility/update/%s", facilityId)
		httpResponse, err = f.apiClient.PATCH(ctx, url, req, headers)
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

func (f *FacilityClientAdapter) DeleteFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/facility/delete/%s", facilityId)
		httpResponse, err = f.apiClient.DELETE(ctx, url, headers)
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

func (f *FacilityClientAdapter) GetFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/facility/search/%s", facilityId)
		httpResponse, err = f.apiClient.GET(ctx, url, headers)
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

func (f *FacilityClientAdapter) GetFacilities(ctx context.Context, params *request.GetFacilityParams) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := make(map[string]string)
	if params.Page != nil {
		queryParams["page"] = fmt.Sprintf("%d", *params.Page)
	}
	if params.Size != nil {
		queryParams["size"] = fmt.Sprintf("%d", *params.Size)
	}
	if params.SortBy != nil {
		queryParams["sortBy"] = *params.SortBy
	}
	if params.SortDirection != nil {
		queryParams["sortDirection"] = *params.SortDirection
	}
	if params.Query != nil {
		queryParams["query"] = *params.Query
	}
	if params.StatusId != nil {
		queryParams["statusId"] = *params.StatusId
	}

	var httpResponse *utils.HTTPResponse
	err := f.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = f.apiClient.GETWithQuery(ctx, "/api/v1/facility/search", queryParams, headers)
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

func NewFacilityClientAdapter(serviceProps *properties.ExternalServiceProperties) port.IFacilityClientPort {
	baseUrl := "http://" + serviceProps.PurchaseService.Host + ":" + serviceProps.PurchaseService.Port + "/purchase-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, serviceProps.PurchaseService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &FacilityClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
