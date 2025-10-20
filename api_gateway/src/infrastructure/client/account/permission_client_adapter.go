/*
Author: QuanTuanHuy
Description: Part of Serp Project
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

type PermissionClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (p *PermissionClientAdapter) CreatePermission(ctx context.Context, req *request.CreatePermissionDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.POST(ctx, "/api/v1/permissions", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create permission API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to call create permission API: %w", err)
	}

	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreatePermission API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create permission response: %w", err)
	}

	return &result, nil
}

func (p *PermissionClientAdapter) GetAllPermissions(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.GET(ctx, "/api/v1/permissions", headers)
		if err != nil {
			return fmt.Errorf("failed to call get all permissions API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAllPermissions API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get all permissions response: %w", err)
	}

	return &result, nil
}

func NewPermissionClientAdapter(authProps *properties.ExternalServiceProperties) port.IPermissionClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &PermissionClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
