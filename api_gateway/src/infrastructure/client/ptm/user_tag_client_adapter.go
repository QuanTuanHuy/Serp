/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/ptm"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type UserTagClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (u *UserTagClientAdapter) CreateUserTag(ctx context.Context, req *request.CreateUserTagRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = u.apiClient.POST(ctx, "/api/v1/tags", req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateUserTag API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = u.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (u *UserTagClientAdapter) GetUserTags(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = u.apiClient.GET(ctx, "/api/v1/tags", headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetUserTags API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = u.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (u *UserTagClientAdapter) GetUserTagByID(ctx context.Context, tagID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = u.apiClient.GET(ctx, fmt.Sprintf("/api/v1/tags/%d", tagID), headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetUserTagByID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = u.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (u *UserTagClientAdapter) UpdateUserTag(ctx context.Context, tagID int64, req *request.UpdateUserTagRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = u.apiClient.PUT(ctx, fmt.Sprintf("/api/v1/tags/%d", tagID), req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateUserTag API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = u.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (u *UserTagClientAdapter) DeleteUserTag(ctx context.Context, tagID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := u.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = u.apiClient.DELETE(ctx, fmt.Sprintf("/api/v1/tags/%d", tagID), headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !u.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteUserTag API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = u.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func NewUserTagClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.IUserTagClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm/task-manager"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &UserTagClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
