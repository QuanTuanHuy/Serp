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

func (u *UserClientAdapter) GetUsers(ctx context.Context, page *int, pageSize *int, sortBy *string, sortDir *string, search *string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := make(map[string]string)
	if page != nil {
		queryParams["page"] = fmt.Sprintf("%d", *page)
	}
	if pageSize != nil {
		queryParams["pageSize"] = fmt.Sprintf("%d", *pageSize)
	}
	if sortBy != nil {
		queryParams["sortBy"] = *sortBy
	}
	if sortDir != nil {
		queryParams["sortDir"] = *sortDir
	}
	if search != nil {
		queryParams["search"] = *search
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

func NewUserClientAdapter(authProps *properties.ExternalServiceProperties) port.IUserClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &UserClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
