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

type OrderClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (o *OrderClientAdapter) CreateOrder(ctx context.Context, req *request.CreateOrderRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.POST(ctx, "/api/v1/order/create", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) UpdateOrder(ctx context.Context, orderId string, req *request.UpdateOrderRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/update/%s", orderId)
		httpResponse, err = o.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) DeleteOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/delete/%s", orderId)
		httpResponse, err = o.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/search/%s", orderId)
		httpResponse, err = o.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) GetOrders(ctx context.Context, params *request.GetOrderParams) (*response.BaseResponse, error) {
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
	if params.FromSupplierId != nil {
		queryParams["fromSupplierId"] = *params.FromSupplierId
	}
	if params.OrderDateAfter != nil {
		queryParams["orderDateAfter"] = *params.OrderDateAfter
	}
	if params.OrderDateBefore != nil {
		queryParams["orderDateBefore"] = *params.OrderDateBefore
	}
	if params.DeliveryBefore != nil {
		queryParams["deliveryBefore"] = *params.DeliveryBefore
	}
	if params.DeliveryAfter != nil {
		queryParams["deliveryAfter"] = *params.DeliveryAfter
	}

	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.GETWithQuery(ctx, "/api/v1/order/search", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get orders API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOrders API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get orders response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) AddProductToOrder(ctx context.Context, orderId string, req *request.AddOrderItemRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/create/%s/add", orderId)
		httpResponse, err = o.apiClient.POST(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call add product to order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("AddProductToOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal add product to order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) DeleteProductFromOrder(ctx context.Context, orderId string, orderItemId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/update/%s/delete/%s", orderId, orderItemId)
		httpResponse, err = o.apiClient.PATCH(ctx, url, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete product from order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteProductFromOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete product from order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) UpdateProductInOrder(ctx context.Context, orderId string, orderItemId string, req *request.UpdateOrderItemRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/update/%s/update/%s", orderId, orderItemId)
		httpResponse, err = o.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update product in order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateProductInOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update product in order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) ApproveOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/manage/%s/approve", orderId)
		httpResponse, err = o.apiClient.PATCH(ctx, url, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call approve order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ApproveOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal approve order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) CancelOrder(ctx context.Context, orderId string, req *request.CancelOrderRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/manage/%s/cancel", orderId)
		httpResponse, err = o.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call cancel order API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CancelOrder API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal cancel order response: %w", err)
	}
	return &result, nil
}

func (o *OrderClientAdapter) MarkOrderAsReady(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/order/update/%s/ready", orderId)
		httpResponse, err = o.apiClient.PATCH(ctx, url, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call mark order as ready API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("MarkOrderAsReady API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal mark order as ready response: %w", err)
	}
	return &result, nil
}

func NewOrderClientAdapter(serviceProps *properties.ExternalServiceProperties) port.IOrderClientPort {
	baseUrl := "http://" + serviceProps.PurchaseService.Host + ":" + serviceProps.PurchaseService.Port + "/purchase-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, serviceProps.PurchaseService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &OrderClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
