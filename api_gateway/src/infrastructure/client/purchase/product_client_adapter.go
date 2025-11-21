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

type ProductClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (p *ProductClientAdapter) CreateProduct(ctx context.Context, req *request.CreateProductRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.POST(ctx, "/api/v1/product/create", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create product API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateProduct API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create product response: %w", err)
	}
	return &result, nil
}

func (p *ProductClientAdapter) UpdateProduct(ctx context.Context, productId string, req *request.UpdateProductRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/product/update/%s", productId)
		httpResponse, err = p.apiClient.PATCH(ctx, url, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update product API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateProduct API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update product response: %w", err)
	}
	return &result, nil
}

func (p *ProductClientAdapter) DeleteProduct(ctx context.Context, productId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/product/delete/%s", productId)
		httpResponse, err = p.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete product API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteProduct API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete product response: %w", err)
	}
	return &result, nil
}

func (p *ProductClientAdapter) GetProduct(ctx context.Context, productId string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/product/search/%s", productId)
		httpResponse, err = p.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get product API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetProduct API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get product response: %w", err)
	}
	return &result, nil
}

func (p *ProductClientAdapter) GetProducts(ctx context.Context, params *request.GetProductParams) (*response.BaseResponse, error) {
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
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.GETWithQuery(ctx, "/api/v1/product/search", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get products API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetProducts API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get products response: %w", err)
	}
	return &result, nil
}

func NewProductClientAdapter(serviceProps *properties.ExternalServiceProperties) port.IProductClientPort {
	baseUrl := "http://" + serviceProps.PurchaseService.Host + ":" + serviceProps.PurchaseService.Port + "/purchase-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, serviceProps.PurchaseService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ProductClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
