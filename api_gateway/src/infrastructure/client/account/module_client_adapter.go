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

type ModuleClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (m *ModuleClientAdapter) CreateModule(ctx context.Context, req *request.CreateModuleDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.POST(ctx, "/api/v1/modules", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create module API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to call create module API: %w", err)
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateModule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create module response: %w", err)
	}

	return &result, nil
}

func (m *ModuleClientAdapter) GetModuleById(ctx context.Context, moduleId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/modules/%d", moduleId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get module by id API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetModuleById API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get module by id response: %w", err)
	}

	return &result, nil
}

func (m *ModuleClientAdapter) GetRolesInModule(ctx context.Context, moduleId int64) (*response.BaseResponse, error) {
	headers := utils.BuildDefaultHeaders()

	path := fmt.Sprintf("/api/v1/modules/%d/roles", moduleId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get roles in module API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetRolesInModule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get roles in module response: %w", err)
	}

	return &result, nil
}

func (m *ModuleClientAdapter) UpdateModule(ctx context.Context, moduleId int64, req *request.UpdateModuleDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/modules/%d", moduleId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.PUT(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update module API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateModule API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update module response: %w", err)
	}

	return &result, nil
}

func (m *ModuleClientAdapter) GetAllModules(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, "/api/v1/modules", headers)
		if err != nil {
			return fmt.Errorf("failed to call get all modules API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAllModules API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get all modules response: %w", err)
	}

	return &result, nil
}

func (m *ModuleClientAdapter) GetMyModules(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, "/api/v1/modules/my-modules", headers)
		if err != nil {
			return fmt.Errorf("failed to call get my modules API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetMyModules API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get my modules response: %w", err)
	}

	return &result, nil
}

func NewModuleClientAdapter(authProps *properties.ExternalServiceProperties) port.IModuleClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ModuleClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
