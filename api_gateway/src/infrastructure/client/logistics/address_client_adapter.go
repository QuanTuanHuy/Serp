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

type AddressClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (a *AddressClientAdapter) CreateAddress(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.POST(ctx, "/logistics/api/v1/address/create", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create address API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateAddress API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create address response: %w", err)
	}
	return &result, nil
}

func (a *AddressClientAdapter) UpdateAddress(ctx context.Context, addressId string, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/address/update/%s", addressId)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.PATCH(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update address API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateAddress API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update address response: %w", err)
	}
	return &result, nil
}

func (a *AddressClientAdapter) GetAddressesByEntityId(ctx context.Context, entityId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/address/search/by-entity/%s", entityId)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get addresses by entity ID API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAddressesByEntityId API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get addresses by entity ID response: %w", err)
	}
	return &result, nil
}

func NewAddressClientAdapter(props *properties.ExternalServiceProperties) port.IAddressClientPort {
	baseURL := fmt.Sprintf("http://%s:%s",
		props.LogisticsService.Host,
		props.LogisticsService.Port)

	return &AddressClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.LogisticsService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
