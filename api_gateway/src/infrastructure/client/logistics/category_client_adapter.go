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

type CategoryClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (c *CategoryClientAdapter) CreateCategory(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.POST(ctx, "/logistics/api/v1/category/create", req, headers)
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

func (c *CategoryClientAdapter) UpdateCategory(ctx context.Context, categoryId string, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/category/update/%s", categoryId)

	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.PATCH(ctx, path, req, headers)
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

func (c *CategoryClientAdapter) GetCategories(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
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
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.GETWithQuery(ctx, "/logistics/api/v1/category/search", queryParams, headers)
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

func (c *CategoryClientAdapter) GetCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/category/search/%s", categoryId)

	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.GET(ctx, path, headers)
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

func (c *CategoryClientAdapter) DeleteCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/category/delete/%s", categoryId)

	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.DELETE(ctx, path, headers)
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

func NewCategoryClientAdapter(props *properties.ExternalServiceProperties) port.ICategoryClientPort {
	baseURL := fmt.Sprintf("http://%s:%s",
		props.LogisticsService.Host,
		props.LogisticsService.Port)

	return &CategoryClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.LogisticsService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
