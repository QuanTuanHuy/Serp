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

type RoleClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (r *RoleClientAdapter) CreateRole(ctx context.Context, req *request.CreateRoleDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := r.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = r.apiClient.POST(ctx, "/api/v1/roles", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create role API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to call create role API: %w", err)
	}

	if !r.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateRole API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := r.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create role response: %w", err)
	}

	return &result, nil
}

func (r *RoleClientAdapter) GetAllRoles(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := r.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = r.apiClient.GET(ctx, "/api/v1/roles", headers)
		if err != nil {
			return fmt.Errorf("failed to call get all roles API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !r.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAllRoles API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := r.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get all roles response: %w", err)
	}

	return &result, nil
}

func (r *RoleClientAdapter) AddPermissionsToRole(ctx context.Context, roleId int64, req *request.AddPermissionToRoleDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/roles/%d/permissions", roleId)
	var httpResponse *utils.HTTPResponse
	err := r.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = r.apiClient.POST(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call add permissions to role API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !r.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("AddPermissionsToRole API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := r.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal add permissions to role response: %w", err)
	}

	return &result, nil
}

func (r *RoleClientAdapter) UpdateRole(ctx context.Context, roleId int64, req *request.UpdateRoleDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/roles/%d", roleId)
	var httpResponse *utils.HTTPResponse
	err := r.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = r.apiClient.PATCH(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update role API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to call update role API: %w", err)
	}

	if !r.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateRole API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := r.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update role response: %w", err)
	}

	return &result, nil
}

func NewRoleClientAdapter(authProps *properties.ExternalServiceProperties) port.IRoleClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &RoleClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
