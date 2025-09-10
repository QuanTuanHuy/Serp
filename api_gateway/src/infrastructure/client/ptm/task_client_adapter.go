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

type TaskClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (t *TaskClientAdapter) CreateTask(ctx context.Context, req *request.CreateTaskRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = t.apiClient.POST(ctx, "/api/v1/tasks", req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateTask API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = t.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (t *TaskClientAdapter) GetTaskByID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d", taskID)

	var res *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = t.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetTaskByID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = t.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (t *TaskClientAdapter) UpdateTask(ctx context.Context, taskID int64, req *request.UpdateTaskRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d", taskID)

	var res *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = t.apiClient.PUT(ctx, url, req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateTask API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = t.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (t *TaskClientAdapter) DeleteTask(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d", taskID)

	var res *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = t.apiClient.DELETE(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteTask API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = t.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (t *TaskClientAdapter) GetCommentsByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d/comments", taskID)

	var res *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = t.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetCommentsByTaskID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = t.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func NewTaskClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.ITaskClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm/task-manager"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &TaskClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
