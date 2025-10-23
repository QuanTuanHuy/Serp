/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package adapter

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type OrganizationClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (o *OrganizationClientAdapter) GetOrganizations(ctx context.Context, params *request.GetOrganizationParams) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := make(map[string]string)
	if params.Search != nil {
		queryParams["search"] = *params.Search
	}
	if params.Status != nil {
		queryParams["status"] = *params.Status
	}
	if params.OrganizationType != nil {
		queryParams["type"] = *params.OrganizationType
	}
	if params.Page != nil {
		queryParams["page"] = fmt.Sprintf("%d", *params.Page)
	}
	if params.PageSize != nil {
		queryParams["pageSize"] = fmt.Sprintf("%d", *params.PageSize)
	}

	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.GETWithQuery(ctx, "/api/v1/admin/organizations", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get organizations API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOrganizations API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get organizations response: %w", err)
	}
	return &result, nil
}

func (o *OrganizationClientAdapter) GetOrganizationById(ctx context.Context, organizationID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/admin/organizations/%d", organizationID)

	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get organization by id API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOrganizationById API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get organization by id response: %w", err)
	}
	return &result, nil
}

func (o *OrganizationClientAdapter) GetMyOrganization(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.GET(ctx, "/api/v1/organizations/me", headers)
		if err != nil {
			return fmt.Errorf("failed to call get my organization API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetMyOrganization API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get my organization response: %w", err)
	}
	return &result, nil
}

func (o *OrganizationClientAdapter) CreateUserForOrganization(ctx context.Context, organizationID int64, req *request.CreateUserForOrgRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/organizations/%d/users", organizationID)

	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.POST(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create user for organization API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateUserForOrganization API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create user for organization response: %w", err)
	}
	return &result, nil
}

func NewOrganizationClientAdapter(authProps *properties.ExternalServiceProperties) port.IOrganizationClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &OrganizationClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
