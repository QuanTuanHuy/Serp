/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"
	"strings"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type MenuDisplayClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (m *MenuDisplayClientAdapter) CreateMenuDisplay(ctx context.Context, req *request.CreateMenuDisplayDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.POST(ctx, "/api/v1/menu-displays", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call create menu display API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateMenuDisplay API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create menu display response: %w", err)
	}

	return &result, nil
}

func (m *MenuDisplayClientAdapter) UpdateMenuDisplay(ctx context.Context, id int64, req *request.UpdateMenuDisplayDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/menu-displays/%d", id)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.PUT(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update menu display API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateMenuDisplay API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update menu display response: %w", err)
	}

	return &result, nil
}

func (m *MenuDisplayClientAdapter) DeleteMenuDisplay(ctx context.Context, id int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/menu-displays/%d", id)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.DELETE(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete menu display API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteMenuDisplay API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete menu display response: %w", err)
	}

	return &result, nil
}

func (m *MenuDisplayClientAdapter) GetMenuDisplaysByModuleId(ctx context.Context, moduleId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/menu-displays/get-by-module/%d", moduleId)
	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get menu displays by module API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetMenuDisplaysByModuleId API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get menu displays by module response: %w", err)
	}

	return &result, nil
}

func (m *MenuDisplayClientAdapter) AssignMenuDisplaysToRole(ctx context.Context, req *request.AssignMenuDisplayToRoleDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.POST(ctx, "/api/v1/menu-displays/assign-to-role", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call assign menu displays to role API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("AssignMenuDisplaysToRole API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal assign menu displays to role response: %w", err)
	}

	return &result, nil
}

func (m *MenuDisplayClientAdapter) UnassignMenuDisplaysFromRole(ctx context.Context, req *request.AssignMenuDisplayToRoleDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.POST(ctx, "/api/v1/menu-displays/unassign-from-role", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call unassign menu displays from role API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UnassignMenuDisplaysFromRole API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal unassign menu displays from role response: %w", err)
	}

	return &result, nil
}

func (m *MenuDisplayClientAdapter) GetMenuDisplaysByRoleIds(ctx context.Context, roleIds []int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	// Convert roleIds to comma-separated string
	roleIdsStr := make([]string, len(roleIds))
	for i, id := range roleIds {
		roleIdsStr[i] = fmt.Sprintf("%d", id)
	}
	queryParams := map[string]string{"roleIds": strings.Join(roleIdsStr, ",")}

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GETWithQuery(ctx, "/api/v1/menu-displays/get-by-role-ids", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get menu displays by role ids API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetMenuDisplaysByRoleIds API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get menu displays by role ids response: %w", err)
	}

	return &result, nil
}

func (m *MenuDisplayClientAdapter) GetMenuDisplaysByModuleIdAndUserId(ctx context.Context, moduleId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := map[string]string{"moduleId": fmt.Sprintf("%d", moduleId)}

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GETWithQuery(ctx, "/api/v1/menu-displays/get-by-module-and-user", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get menu displays by module and user API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetMenuDisplaysByModuleIdAndUserId API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get menu displays by module and user response: %w", err)
	}

	return &result, nil
}

func (m *MenuDisplayClientAdapter) GetAllMenuDisplays(ctx context.Context, params *request.GetMenuDisplayParams) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := make(map[string]string)
	if params != nil {
		if params.Page != nil {
			queryParams["page"] = fmt.Sprintf("%d", *params.Page)
		}
		if params.PageSize != nil {
			queryParams["pageSize"] = fmt.Sprintf("%d", *params.PageSize)
		}
		if params.SortBy != nil {
			queryParams["sortBy"] = *params.SortBy
		}
		if params.SortDirection != nil {
			queryParams["sortDir"] = *params.SortDirection
		}
		if params.ModuleId != nil {
			queryParams["moduleId"] = fmt.Sprintf("%d", *params.ModuleId)
		}
		if params.Search != nil {
			queryParams["search"] = *params.Search
		}
	}

	var httpResponse *utils.HTTPResponse
	err := m.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = m.apiClient.GETWithQuery(ctx, "/api/v1/menu-displays", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get all menu displays API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !m.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAllMenuDisplays API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := m.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get all menu displays response: %w", err)
	}

	return &result, nil
}

func NewMenuDisplayClientAdapter(authProps *properties.ExternalServiceProperties) port.IMenuDisplayClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &MenuDisplayClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
