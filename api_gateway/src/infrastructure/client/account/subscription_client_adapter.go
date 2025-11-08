/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"
	"strconv"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/account"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type SubscriptionClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (s *SubscriptionClientAdapter) GetAllSubscriptions(ctx context.Context, params *request.GetSubscriptionParams) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := map[string]string{}
	if params != nil {
		if params.Page != nil {
			queryParams["page"] = strconv.Itoa(*params.Page)
		}
		if params.PageSize != nil {
			queryParams["pageSize"] = strconv.Itoa(*params.PageSize)
		}
		if params.SortBy != nil && *params.SortBy != "" {
			queryParams["sortBy"] = *params.SortBy
		}
		if params.SortDir != nil && *params.SortDir != "" {
			queryParams["sortDir"] = *params.SortDir
		}
		if params.OrganizationID != nil && *params.OrganizationID > 0 {
			queryParams["organizationId"] = strconv.FormatInt(*params.OrganizationID, 10)
		}
		if params.Status != nil && *params.Status != "" {
			queryParams["status"] = *params.Status
		}
		if params.BillingCycle != nil && *params.BillingCycle != "" {
			queryParams["billingCycle"] = *params.BillingCycle
		}
	}

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GETWithQuery(ctx, "/api/v1/admin/subscriptions", queryParams, headers)
		if err != nil {
			log.Error(ctx, fmt.Sprintf("GetAllSubscriptions API call failed: %v", err))
			return err
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAllSubscriptions API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		log.Error(ctx, fmt.Sprintf("Failed to unmarshal GetAllSubscriptions response: %v", err))
		return nil, err
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) Subscribe(ctx context.Context, req *request.SubscribeRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/subscriptions/subscribe", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call subscribe API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("Subscribe API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal subscribe response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) SubscribeCustomPlan(ctx context.Context, req *request.SubscribeCustomPlanRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/subscriptions/subscribe-custom-plan", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call subscribe custom plan API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("SubscribeCustomPlan API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal subscribe custom plan response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) RequestMoreModules(ctx context.Context, req *request.RequestMoreModulesRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POST(ctx, "/api/v1/subscriptions/request-more-modules", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call request more modules API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("RequestMoreModules API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal request more modules response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) StartTrial(ctx context.Context, planId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := map[string]string{"planId": fmt.Sprintf("%d", planId)}

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.POSTWithQuery(ctx, "/api/v1/subscriptions/trial", queryParams, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call start trial API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("StartTrial API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal start trial response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) ActivateSubscription(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/admin/subscriptions/%d/activate", subscriptionId)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.PUT(ctx, path, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call activate subscription API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ActivateSubscription API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal activate subscription response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) RejectSubscription(ctx context.Context, subscriptionId int64, req *request.RejectSubscriptionRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/admin/subscriptions/%d/reject", subscriptionId)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.PUT(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call reject subscription API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("RejectSubscription API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal reject subscription response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) UpgradeSubscription(ctx context.Context, req *request.UpgradeSubscriptionRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.PUT(ctx, "/api/v1/subscriptions/upgrade", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call upgrade subscription API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpgradeSubscription API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal upgrade subscription response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) DowngradeSubscription(ctx context.Context, req *request.DowngradeSubscriptionRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.PUT(ctx, "/api/v1/subscriptions/downgrade", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call downgrade subscription API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DowngradeSubscription API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal downgrade subscription response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) CancelSubscription(ctx context.Context, req *request.CancelSubscriptionRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.PUT(ctx, "/api/v1/subscriptions/cancel", req, headers)
		if err != nil {
			return fmt.Errorf("failed to call cancel subscription API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CancelSubscription API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal cancel subscription response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) RenewSubscription(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.PUT(ctx, "/api/v1/subscriptions/renew", nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call renew subscription API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("RenewSubscription API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal renew subscription response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) ExtendTrial(ctx context.Context, subscriptionId int64, req *request.ExtendTrialRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/subscriptions/%d/extend-trial", subscriptionId)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.PUT(ctx, path, req, headers)
		if err != nil {
			return fmt.Errorf("failed to call extend trial API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ExtendTrial API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal extend trial response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) ExpireSubscription(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/admin/subscriptions/%d/expire", subscriptionId)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.PUT(ctx, path, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call expire subscription API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ExpireSubscription API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal expire subscription response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) GetActiveSubscription(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GET(ctx, "/api/v1/subscriptions/me/active", headers)
		if err != nil {
			return fmt.Errorf("failed to call get active subscription API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetActiveSubscription API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get active subscription response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) GetSubscriptionById(ctx context.Context, subscriptionId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	path := fmt.Sprintf("/api/v1/subscriptions/%d", subscriptionId)
	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GET(ctx, path, headers)
		if err != nil {
			return fmt.Errorf("failed to call get subscription by id API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetSubscriptionById API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get subscription by id response: %w", err)
	}

	return &result, nil
}

func (s *SubscriptionClientAdapter) GetSubscriptionHistory(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = s.apiClient.GET(ctx, "/api/v1/subscriptions/me/history", headers)
		if err != nil {
			return fmt.Errorf("failed to call get subscription history API: %w", err)
		}
		return nil
	})

	if err != nil {
		return nil, err
	}

	if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetSubscriptionHistory API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get subscription history response: %w", err)
	}

	return &result, nil
}

func NewSubscriptionClientAdapter(authProps *properties.ExternalServiceProperties) port.ISubscriptionClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &SubscriptionClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
