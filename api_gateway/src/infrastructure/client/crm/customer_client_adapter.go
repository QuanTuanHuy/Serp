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
	port "github.com/serp/api-gateway/src/core/port/client/crm"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type CustomerClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (c *CustomerClientAdapter) CreateCustomer(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.POST(ctx, "/api/v1/customers", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call create customer API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateCustomer API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create customer response: %w", err)
	}
	return &result, nil
}

func (c *CustomerClientAdapter) UpdateCustomer(ctx context.Context, customerId int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/customers/%d", customerId)
		httpResponse, err = c.apiClient.PATCH(ctx, path, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call update customer API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateCustomer API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update customer response: %w", err)
	}
	return &result, nil
}

func (c *CustomerClientAdapter) GetCustomerByID(ctx context.Context, customerId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/customers/%d", customerId)
		httpResponse, err = c.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get customer API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetCustomerByID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get customer response: %w", err)
	}
	return &result, nil
}

func (c *CustomerClientAdapter) GetCustomers(ctx context.Context, page, size int) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	queryParams := map[string]string{
		"page": fmt.Sprintf("%d", page),
		"size": fmt.Sprintf("%d", size),
	}

	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.GETWithQuery(ctx, "/api/v1/customers", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get customers API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetCustomers API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get customers response: %w", err)
	}
	return &result, nil
}

func (c *CustomerClientAdapter) FilterCustomers(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.POST(ctx, "/api/v1/customers/search", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call filter customers API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("FilterCustomers API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal filter customers response: %w", err)
	}
	return &result, nil
}

func (c *CustomerClientAdapter) DeleteCustomer(ctx context.Context, customerId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/customers/%d", customerId)
		httpResponse, err = c.apiClient.DELETE(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete customer API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteCustomer API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete customer response: %w", err)
	}
	return &result, nil
}

func NewCustomerClientAdapter(props *properties.ExternalServiceProperties) port.ICustomerClientPort {
	baseURL := fmt.Sprintf("http://%s:%s/crm", props.CrmService.Host, props.CrmService.Port)

	return &CustomerClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.CrmService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
