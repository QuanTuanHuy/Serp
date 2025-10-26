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

type UserClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (u *UserClientAdapter) GetMyProfile(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = u.apiClient.GET(ctx, "/api/v1/users/profile/me", headers)
		if err != nil {
			return fmt.Errorf("failed to call get user profile API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetMyProfile API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := u.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get user profile response: %w", err)
	}
	return &result, nil
}

func (u *UserClientAdapter) GetUsers(ctx context.Context, params *request.GetUserParams) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := make(map[string]string)
	if params.Page != nil {
		queryParams["page"] = fmt.Sprintf("%d", *params.Page)
	}
	if params.PageSize != nil {
		queryParams["pageSize"] = fmt.Sprintf("%d", *params.PageSize)
	}
	if params.SortBy != nil {
		queryParams["sortBy"] = *params.SortBy
	}
	if params.SortDir != nil {
		queryParams["sortDir"] = *params.SortDir
	}
	if params.Search != nil {
		queryParams["search"] = *params.Search
	}
	if params.Status != nil {
		queryParams["status"] = *params.Status
	}
	if params.OrganizationID != nil {
		queryParams["organizationId"] = fmt.Sprintf("%d", *params.OrganizationID)
	}

	var httpResponse *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = u.apiClient.GETWithQuery(ctx, "/api/v1/users", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get users API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetUsers API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := u.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get users response: %w", err)
	}
	return &result, nil
}

func (u *UserClientAdapter) AssignRolesToUser(ctx context.Context, req *request.AssignRoleToUserDto) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = u.apiClient.POST(ctx, "/api/v1/users/assign-roles", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call assign roles API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("AssignRolesToUser API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := u.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal assign roles response: %w", err)
	}
	return &result, nil
}

func (u *UserClientAdapter) UpdateUserInfo(ctx context.Context, userId int64, req *request.UpdateUserInfoRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	path := fmt.Sprintf("/api/v1/users/%d/info", userId)

	var httpResponse *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = u.apiClient.PATCH(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call update user info API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateUserInfo API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := u.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update user info response: %w", err)
	}
	return &result, nil
}

func NewUserClientAdapter(authProps *properties.ExternalServiceProperties) port.IUserClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &UserClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
