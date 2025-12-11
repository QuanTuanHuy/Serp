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

type ContactClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (c *ContactClientAdapter) CreateContact(ctx context.Context, customerId int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/customers/%d/contacts", customerId)
		httpResponse, err = c.apiClient.POST(ctx, path, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call create contact API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateContact API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create contact response: %w", err)
	}
	return &result, nil
}

func (c *ContactClientAdapter) UpdateContact(ctx context.Context, customerId int64, contactId int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/customers/%d/contacts/%d", customerId, contactId)
		httpResponse, err = c.apiClient.PATCH(ctx, path, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call update contact API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateContact API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update contact response: %w", err)
	}
	return &result, nil
}

func (c *ContactClientAdapter) GetContactByID(ctx context.Context, customerId int64, contactId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/customers/%d/contacts/%d", customerId, contactId)
		httpResponse, err = c.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get contact API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetContactByID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get contact response: %w", err)
	}
	return &result, nil
}

func (c *ContactClientAdapter) GetContactsByCustomer(ctx context.Context, customerId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/customers/%d/contacts", customerId)
		httpResponse, err = c.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get contacts by customer API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetContactsByCustomer API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get contacts by customer response: %w", err)
	}
	return &result, nil
}

func (c *ContactClientAdapter) GetContacts(ctx context.Context, page, size int) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	queryParams := map[string]string{
		"page": fmt.Sprintf("%d", page),
		"size": fmt.Sprintf("%d", size),
	}

	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = c.apiClient.GETWithQuery(ctx, "/api/v1/contacts", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get contacts API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetContacts API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get contacts response: %w", err)
	}
	return &result, nil
}

func (c *ContactClientAdapter) DeleteContact(ctx context.Context, customerId int64, contactId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/customers/%d/contacts/%d", customerId, contactId)
		httpResponse, err = c.apiClient.DELETE(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete contact API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteContact API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := c.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete contact response: %w", err)
	}
	return &result, nil
}

func NewContactClientAdapter(props *properties.ExternalServiceProperties) port.IContactClientPort {
	baseURL := fmt.Sprintf("http://%s:%s/crm", props.CrmService.Host, props.CrmService.Port)

	return &ContactClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.CrmService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
