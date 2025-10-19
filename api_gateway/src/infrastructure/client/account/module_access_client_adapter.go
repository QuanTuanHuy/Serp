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

type ModuleAccessClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (m *ModuleAccessClientAdapter) CanOrganizationAccessModule(ctx context.Context, organizationId int64, moduleId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/module-access/organizations/%d/modules/%d/check", organizationId, moduleId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call can organization access module API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CanOrganizationAccessModule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal can organization access module response: %w", err)
	}

	return &result, nil
}

func (m *ModuleAccessClientAdapter) GetAccessibleModulesForOrganization(ctx context.Context, organizationId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/module-access/organizations/%d/modules", organizationId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get accessible modules API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAccessibleModulesForOrganization API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get accessible modules response: %w", err)
	}

	return &result, nil
}

func (m *ModuleAccessClientAdapter) AssignUserToModule(ctx context.Context, organizationId int64, req *request.AssignUserToModuleRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/module-access/organizations/%d/assign", organizationId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.POST(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call assign user to module API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("AssignUserToModule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal assign user to module response: %w", err)
	}

	return &result, nil
}

func (m *ModuleAccessClientAdapter) BulkAssignUsersToModule(ctx context.Context, req *request.BulkAssignUsersRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.POST(ctx, "/api/v1/module-access/bulk-assign", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call bulk assign users to module API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("BulkAssignUsersToModule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal bulk assign users to module response: %w", err)
	}

	return &result, nil
}

func (m *ModuleAccessClientAdapter) RevokeUserAccessToModule(ctx context.Context, organizationId int64, moduleId int64, userId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/module-access/organizations/%d/modules/%d/users/%d", organizationId, moduleId, userId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.DELETE(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call revoke user access API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("RevokeUserAccessToModule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal revoke user access response: %w", err)
	}

	return &result, nil
}

func (m *ModuleAccessClientAdapter) GetUsersWithAccessToModule(ctx context.Context, organizationId int64, moduleId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/module-access/organizations/%d/modules/%d/users", organizationId, moduleId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get users with access API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetUsersWithAccessToModule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get users with access response: %w", err)
	}

	return &result, nil
}

func (m *ModuleAccessClientAdapter) GetModulesAccessibleByUser(ctx context.Context, organizationId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/module-access/users/me/organizations/%d/modules", organizationId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get modules accessible by user API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetModulesAccessibleByUser API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get modules accessible by user response: %w", err)
	}

	return &result, nil
}

func NewModuleAccessClientAdapter(authProps *properties.ExternalServiceProperties) port.IModuleAccessClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ModuleAccessClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
