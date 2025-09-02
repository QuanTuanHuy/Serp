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

type AuthClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (a *AuthClientAdapter) Login(ctx context.Context, req *request.LoginDTO) (*response.BaseResponse, error) {
	headers := utils.BuildDefaultHeaders()

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.POST(ctx, "/api/v1/auth/login", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call login API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to call login API: %w", err)
	}

	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("Login API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal login response: %w", err)
	}

	return &result, nil
}

func (a *AuthClientAdapter) Register(ctx context.Context, req *request.RegisterDTO) (*response.BaseResponse, error) {
	headers := utils.BuildDefaultHeaders()

	var httpResponse *utils.HTTPResponse
	err := a.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = a.apiClient.POST(ctx, "/api/v1/auth/register", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call register API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to call register API: %w", err)
	}

	if !a.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("Register API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := a.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal register response: %w", err)
	}

	return &result, nil
}

func NewAuthClientAdapter(authProps *properties.ExternalServiceProperties) port.IAuthClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &AuthClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
