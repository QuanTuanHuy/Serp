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

func NewUserClientAdapter(authProps *properties.ExternalServiceProperties) port.IUserClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &UserClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
