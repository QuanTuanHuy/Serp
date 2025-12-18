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

type SupplierClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (s *SupplierClientAdapter) CreateSupplier(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/supplier/create", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create supplier API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateSupplier API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create supplier response: %w", err)
	}
	return &result, nil
}

func (s *SupplierClientAdapter) UpdateSupplier(ctx context.Context, supplierId string, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/supplier/update/%s", supplierId)
		httpResponse, err = s.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update supplier API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateSupplier API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update supplier response: %w", err)
	}
	return &result, nil
}

func (s *SupplierClientAdapter) DeleteSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/supplier/delete/%s", supplierId)
		httpResponse, err = s.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete supplier API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteSupplier API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete supplier response: %w", err)
	}
	return &result, nil
}

func (s *SupplierClientAdapter) GetSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/supplier/search/%s", supplierId)
		httpResponse, err = s.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get supplier API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetSupplier API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get supplier response: %w", err)
	}
	return &result, nil
}

func (s *SupplierClientAdapter) GetSuppliers(ctx context.Context, params *request.GetSupplierParams) (*response.BaseResponse, error) {
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
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GETWithQuery(ctx, "/api/v1/supplier/search", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get suppliers API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetSuppliers API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get suppliers response: %w", err)
	}
	return &result, nil
}

func NewSupplierClientAdapter(serviceProps *properties.ExternalServiceProperties) port.ISupplierClientPort {
	baseUrl := "http://" + serviceProps.PurchaseService.Host + ":" + serviceProps.PurchaseService.Port + "/purchase-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, serviceProps.PurchaseService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &SupplierClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
