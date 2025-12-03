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

type CustomerClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (c *CustomerClientAdapter) GetCustomers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
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
		httpResponse, err = c.apiClient.GETWithQuery(ctx, "/logistics/api/v1/customer/search", queryParams, headers)
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

func NewCustomerClientAdapter(props *properties.ExternalServiceProperties) port.ICustomerClientPort {
	baseURL := fmt.Sprintf("http://%s:%s",
		props.LogisticsService.Host,
		props.LogisticsService.Port)

	return &CustomerClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.LogisticsService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
