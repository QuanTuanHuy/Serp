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

type OrderClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (a *OrderClientAdapter) GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/logistics/api/v1/order/search/%s", orderId)

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get order response: %w", err)
	}
	return &result, nil
}

func (a *OrderClientAdapter) GetOrders(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := "/logistics/api/v1/order/search"

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
	if orderTypeId != "" {
		queryParams["orderTypeId"] = orderTypeId
	}
	if toCustomerId != "" {
		queryParams["toCustomerId"] = toCustomerId
	}
	if fromSupplierId != "" {
		queryParams["fromSupplierId"] = fromSupplierId
	}
	if saleChannelId != "" {
		queryParams["saleChannelId"] = saleChannelId
	}
	if orderDateAfter != "" {
		queryParams["orderDateAfter"] = orderDateAfter
	}
	if orderDateBefore != "" {
		queryParams["orderDateBefore"] = orderDateBefore
	}
	if deliveryBefore != "" {
		queryParams["deliveryBefore"] = deliveryBefore
	}
	if deliveryAfter != "" {
		queryParams["deliveryAfter"] = deliveryAfter
	}

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.GETWithQuery(ctx, path, queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get orders API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOrders API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get orders response: %w", err)
	}
	return &result, nil
}

func NewOrderClientAdapter(props *properties.ExternalServiceProperties) port.IOrderClientPort {
	baseURL := fmt.Sprintf("http://%s:%s",
		props.LogisticsService.Host,
		props.LogisticsService.Port)

	return &OrderClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.LogisticsService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
