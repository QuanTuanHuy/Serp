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

type ShipmentClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (s *ShipmentClientAdapter) CreateShipment(ctx context.Context, req *request.CreateShipmentRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/shipment", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create shipment API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateShipment API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create shipment response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) UpdateShipment(ctx context.Context, shipmentId string, req *request.UpdateShipmentRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/shipment/%s", shipmentId)
		httpResponse, err = s.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update shipment API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateShipment API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update shipment response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/shipment/%s", shipmentId)
		httpResponse, err = s.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete shipment API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteShipment API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete shipment response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) GetShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/shipment/%s", shipmentId)
		httpResponse, err = s.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get shipment API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetShipment API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get shipment response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/shipment/%s/import", shipmentId)
		httpResponse, err = s.apiClient.PATCH(ctx, url, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call import shipment API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ImportShipment API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal import shipment response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) AddItemToShipment(ctx context.Context, shipmentId string, req *request.AddItemToShipmentRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/shipment/%s/add", shipmentId)
		httpResponse, err = s.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call add item to shipment API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("AddItemToShipment API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal add item to shipment response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) UpdateItemInShipment(ctx context.Context, shipmentId string, itemId string, req *request.UpdateItemInShipmentRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/shipment/%s/update/%s", shipmentId, itemId)
		httpResponse, err = s.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update item in shipment API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateItemInShipment API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update item in shipment response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) DeleteItemFromShipment(ctx context.Context, shipmentId string, itemId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/shipment/%s/delete/%s", shipmentId, itemId)
		httpResponse, err = s.apiClient.PATCH(ctx, url, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete item from shipment API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteItemFromShipment API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete item from shipment response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) UpdateShipmentFacility(ctx context.Context, shipmentId string, req *request.UpdateShipmentFacilityRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/shipment/%s/facility", shipmentId)
		httpResponse, err = s.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update shipment facility API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateShipmentFacility API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update shipment facility response: %w", err)
	}
	return &result, nil
}

func NewShipmentClientAdapter(serviceProps *properties.ExternalServiceProperties) port.IShipmentClientPort {
	baseUrl := "http://" + serviceProps.PurchaseService.Host + ":" + serviceProps.PurchaseService.Port + "/purchase-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, serviceProps.PurchaseService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ShipmentClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
