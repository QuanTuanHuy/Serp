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

type SubscriptionPlanClientAdapter struct {
apiClient      *utils.BaseAPIClient
circuitBreaker *utils.CircuitBreaker
}

func (s *SubscriptionPlanClientAdapter) CreatePlan(ctx context.Context, req *request.CreateSubscriptionPlanRequest) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.POST(ctx, "/api/v1/subscription-plans", req, headers)
if err != nil {
return fmt.Errorf("failed to call create plan API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("CreatePlan API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal create plan response: %w", err)
}

return &result, nil
}

func (s *SubscriptionPlanClientAdapter) UpdatePlan(ctx context.Context, planId int64, req *request.UpdateSubscriptionPlanRequest) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

path := fmt.Sprintf("/api/v1/subscription-plans/%d", planId)
var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.PUT(ctx, path, req, headers)
if err != nil {
return fmt.Errorf("failed to call update plan API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("UpdatePlan API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal update plan response: %w", err)
}

return &result, nil
}

func (s *SubscriptionPlanClientAdapter) DeletePlan(ctx context.Context, planId int64) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

path := fmt.Sprintf("/api/v1/subscription-plans/%d", planId)
var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.DELETE(ctx, path, headers)
if err != nil {
return fmt.Errorf("failed to call delete plan API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("DeletePlan API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal delete plan response: %w", err)
}

return &result, nil
}

func (s *SubscriptionPlanClientAdapter) GetPlanById(ctx context.Context, planId int64) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

path := fmt.Sprintf("/api/v1/subscription-plans/%d", planId)
var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.GET(ctx, path, headers)
if err != nil {
return fmt.Errorf("failed to call get plan by id API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("GetPlanById API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal get plan by id response: %w", err)
}

return &result, nil
}

func (s *SubscriptionPlanClientAdapter) GetPlanByCode(ctx context.Context, planCode string) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

path := fmt.Sprintf("/api/v1/subscription-plans/code/%s", planCode)
var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.GET(ctx, path, headers)
if err != nil {
return fmt.Errorf("failed to call get plan by code API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("GetPlanByCode API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal get plan by code response: %w", err)
}

return &result, nil
}

func (s *SubscriptionPlanClientAdapter) GetAllPlans(ctx context.Context) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.GET(ctx, "/api/v1/subscription-plans", headers)
if err != nil {
return fmt.Errorf("failed to call get all plans API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("GetAllPlans API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal get all plans response: %w", err)
}

return &result, nil
}

func (s *SubscriptionPlanClientAdapter) AddModuleToPlan(ctx context.Context, planId int64, req *request.AddModuleToPlanRequest) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

path := fmt.Sprintf("/api/v1/subscription-plans/%d/modules", planId)
var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.POST(ctx, path, req, headers)
if err != nil {
return fmt.Errorf("failed to call add module to plan API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("AddModuleToPlan API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal add module to plan response: %w", err)
}

return &result, nil
}

func (s *SubscriptionPlanClientAdapter) RemoveModuleFromPlan(ctx context.Context, planId int64, moduleId int64) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

path := fmt.Sprintf("/api/v1/subscription-plans/%d/modules/%d", planId, moduleId)
var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.DELETE(ctx, path, headers)
if err != nil {
return fmt.Errorf("failed to call remove module from plan API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("RemoveModuleFromPlan API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal remove module from plan response: %w", err)
}

return &result, nil
}

func (s *SubscriptionPlanClientAdapter) GetPlanModules(ctx context.Context, planId int64) (*response.BaseResponse, error) {
headers := utils.BuildHeadersFromContext(ctx)

path := fmt.Sprintf("/api/v1/subscription-plans/%d/modules", planId)
var httpResponse *utils.HTTPResponse
err := s.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
var err error
httpResponse, err = s.apiClient.GET(ctx, path, headers)
if err != nil {
return fmt.Errorf("failed to call get plan modules API: %w", err)
}
return nil
})

if err != nil {
return nil, err
}

if !s.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
log.Error(ctx, fmt.Sprintf("GetPlanModules API returned error status: %d", httpResponse.StatusCode))
}

var result response.BaseResponse
if err := s.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
return nil, fmt.Errorf("failed to unmarshal get plan modules response: %w", err)
}

return &result, nil
}

func NewSubscriptionPlanClientAdapter(authProps *properties.ExternalServiceProperties) port.ISubscriptionPlanClientPort {
baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
apiClient := utils.NewBaseAPIClient(baseUrl, authProps.AccountService.Timeout)

circuitBreaker := utils.NewDefaultCircuitBreaker()

return &SubscriptionPlanClientAdapter{
apiClient:      apiClient,
circuitBreaker: circuitBreaker,
}
}
