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
	port "github.com/serp/api-gateway/src/core/port/client/logistics"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type InventoryItemClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (a *InventoryItemClientAdapter) CreateInventoryItem(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.POST(ctx, "/logistics/api/v1/inventory-item/create", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create inventory item API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateInventoryItem API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create inventory item response: %w", err)
	}
	return &result, nil
}

func (a *InventoryItemClientAdapter) UpdateInventoryItem(ctx context.Context, inventoryItemId string, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/inventory-item/update/%s", inventoryItemId)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.PATCH(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update inventory item API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateInventoryItem API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update inventory item response: %w", err)
	}
	return &result, nil
}

func (a *InventoryItemClientAdapter) DeleteInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/inventory-item/delete/%s", inventoryItemId)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.DELETE(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete inventory item API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteInventoryItem API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete inventory item response: %w", err)
	}
	return &result, nil
}

func (a *InventoryItemClientAdapter) GetInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/inventory-item/search/%s", inventoryItemId)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get inventory item API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetInventoryItem API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get inventory item response: %w", err)
	}
	return &result, nil
}

func (a *InventoryItemClientAdapter) GetInventoryItems(ctx context.Context, page, size int, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := "/logistics/api/v1/inventory-item/search"

	queryParams := map[string]string{
		"page":          fmt.Sprintf("%d", page),
		"size":          fmt.Sprintf("%d", size),
		"sortBy":        sortBy,
		"sortDirection": sortDirection,
	}

	if query != "" {
		queryParams["query"] = query
	}
	if productId != "" {
		queryParams["productId"] = productId
	}
	if facilityId != "" {
		queryParams["facilityId"] = facilityId
	}
	if expirationDateFrom != "" {
		queryParams["expirationDateFrom"] = expirationDateFrom
	}
	if expirationDateTo != "" {
		queryParams["expirationDateTo"] = expirationDateTo
	}
	if manufacturingDateFrom != "" {
		queryParams["manufacturingDateFrom"] = manufacturingDateFrom
	}
	if manufacturingDateTo != "" {
		queryParams["manufacturingDateTo"] = manufacturingDateTo
	}
	if statusId != "" {
		queryParams["statusId"] = statusId
	}

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.GETWithQuery(ctx, path, queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get inventory items API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetInventoryItems API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get inventory items response: %w", err)
	}
	return &result, nil
}

func NewInventoryItemClientAdapter(props *properties.ExternalServiceProperties) port.IInventoryItemClientPort {
	baseURL := fmt.Sprintf("http://%s:%s",
		props.LogisticsService.Host,
		props.LogisticsService.Port)

	return &InventoryItemClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.LogisticsService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
