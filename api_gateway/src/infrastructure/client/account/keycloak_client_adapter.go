/*
Author: QuanTuanHuy
Description: Part of Serp Project
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

type KeycloakClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (k *KeycloakClientAdapter) GetKeycloakClientSecret(ctx context.Context, clientId string) (*response.BaseResponse, error) {
	headers := utils.BuildDefaultHeaders()

	var httpResponse *utils.HTTPResponse
	err := k.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = k.apiClient.GET(ctx, fmt.Sprintf("/api/v1/keycloak/clients/%s/client-secret", clientId), headers)
		if err != nil {
			return fmt.Errorf("failed to call GetKeycloakClientSecret API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}
	if !k.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetKeycloakClientSecret API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := k.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal GetKeycloakClientSecret response: %w", err)
	}
	return &result, nil
}

func NewKeycloakClientAdapter(keycloakProps *properties.ExternalServiceProperties) port.IKeycloakClientPort {
	baseUrl := "http://" + keycloakProps.AccountService.Host + ":" + keycloakProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, keycloakProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &KeycloakClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
