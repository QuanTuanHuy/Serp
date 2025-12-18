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

type ShipmentClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func NewShipmentClientAdapter(props *properties.ExternalServiceProperties) port.IShipmentClientPort {
	baseURL := fmt.Sprintf("http://%s:%s",
		props.LogisticsService.Host,
		props.LogisticsService.Port)

	return &ShipmentClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.LogisticsService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}

func (s *ShipmentClientAdapter) CreateShipment(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/logistics/api/v1/shipment/create", req, headers)
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

func (s *ShipmentClientAdapter) UpdateShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/logistics/api/v1/shipment/update/%s", shipmentId)
		httpResponse, err = s.apiClient.PATCH(ctx, path, req, headers)
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

func (s *ShipmentClientAdapter) ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/logistics/api/v1/shipment/manage/%s/import", shipmentId)
		httpResponse, err = s.apiClient.PATCH(ctx, path, nil, headers)
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

func (s *ShipmentClientAdapter) DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/logistics/api/v1/shipment/delete/%s", shipmentId)
		httpResponse, err = s.apiClient.DELETE(ctx, path, headers)
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

func (s *ShipmentClientAdapter) AddItemToShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/logistics/api/v1/shipment/create/%s/add", shipmentId)
		httpResponse, err = s.apiClient.POST(ctx, path, req, headers)
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

func (s *ShipmentClientAdapter) UpdateItemInShipment(ctx context.Context, shipmentId, itemId string, req map[string]interface{}) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/logistics/api/v1/shipment/update/%s/update/%s", shipmentId, itemId)
		httpResponse, err = s.apiClient.PATCH(ctx, path, req, headers)
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

func (s *ShipmentClientAdapter) DeleteItemFromShipment(ctx context.Context, shipmentId, itemId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/logistics/api/v1/shipment/update/%s/delete/%s", shipmentId, itemId)
		httpResponse, err = s.apiClient.PATCH(ctx, path, nil, headers)
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

func (s *ShipmentClientAdapter) GetShipmentDetail(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/logistics/api/v1/shipment/search/%s", shipmentId)
		httpResponse, err = s.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get shipment detail API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetShipmentDetail API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get shipment detail response: %w", err)
	}
	return &result, nil
}

func (s *ShipmentClientAdapter) GetShipments(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		queryParams := map[string]string{
			"page":          fmt.Sprintf("%d", page),
			"size":          fmt.Sprintf("%d", size),
			"sortBy":        sortBy,
			"sortDirection": sortDirection,
		}
		if query != "" {
			queryParams["query"] = query
		}
		if statusId != "" {
			queryParams["statusId"] = statusId
		}
		if shipmentTypeId != "" {
			queryParams["shipmentTypeId"] = shipmentTypeId
		}
		if toCustomerId != "" {
			queryParams["toCustomerId"] = toCustomerId
		}
		if fromSupplierId != "" {
			queryParams["fromSupplierId"] = fromSupplierId
		}
		if orderId != "" {
			queryParams["orderId"] = orderId
		}
		httpResponse, err = s.apiClient.GETWithQuery(ctx, "/logistics/api/v1/shipment/search", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get shipments API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetShipments API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get shipments response: %w", err)
	}
	return &result, nil
}
