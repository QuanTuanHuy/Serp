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

type AddressClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (a *AddressClientAdapter) CreateAddress(ctx context.Context, req *request.CreateAddressRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.POST(ctx, "/api/v1/address/create", req, headers)
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

func (a *AddressClientAdapter) UpdateAddress(ctx context.Context, addressId string, req *request.UpdateAddressRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/address/update/%s", addressId)
		httpResponse, err = a.apiClient.PATCH(ctx, url, req, headers)
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

func NewAddressClientAdapter(serviceProps *properties.ExternalServiceProperties) port.IAddressClientPort {
	baseUrl := "http://" + serviceProps.PurchaseService.Host + ":" + serviceProps.PurchaseService.Port + "/purchase-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, serviceProps.PurchaseService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &AddressClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
