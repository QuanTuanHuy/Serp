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

type CategoryClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (c *CategoryClientAdapter) CreateCategory(ctx context.Context, req *request.CreateCategoryRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.POST(ctx, "/api/v1/category", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create category API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateCategory API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create category response: %w", err)
	}
	return &result, nil
}

func (c *CategoryClientAdapter) UpdateCategory(ctx context.Context, categoryId string, req *request.UpdateCategoryRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/category/%s", categoryId)
		httpResponse, err = c.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update category API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateCategory API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update category response: %w", err)
	}
	return &result, nil
}

func (c *CategoryClientAdapter) DeleteCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/category/%s", categoryId)
		httpResponse, err = c.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete category API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteCategory API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete category response: %w", err)
	}
	return &result, nil
}

func (c *CategoryClientAdapter) GetCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/category/%s", categoryId)
		httpResponse, err = c.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get category API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetCategory API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get category response: %w", err)
	}
	return &result, nil
}

func (c *CategoryClientAdapter) GetCategories(ctx context.Context, params *request.GetCategoryParams) (*response.BaseResponse, error) {
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
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.GETWithQuery(ctx, "/api/v1/category", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get categories API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetCategories API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get categories response: %w", err)
	}
	return &result, nil
}

func NewCategoryClientAdapter(serviceProps *properties.ExternalServiceProperties) port.ICategoryClientPort {
	baseUrl := "http://" + serviceProps.PurchaseService.Host + ":" + serviceProps.PurchaseService.Port + "/purchase-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, serviceProps.PurchaseService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &CategoryClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
