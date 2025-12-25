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
	port "github.com/serp/api-gateway/src/core/port/client/ptm"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type TaskClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (t *TaskClientAdapter) CreateTask(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = t.apiClient.POST(ctx, "/api/v1/tasks", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call create task API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateTask API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := t.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create task response: %w", err)
	}
	return &result, nil
}

func (t *TaskClientAdapter) GetTasksByUserID(ctx context.Context, params map[string]string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = t.apiClient.GETWithQuery(ctx, "/api/v1/tasks", params, headers)
		if err != nil {
			return fmt.Errorf("failed to call get tasks API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetTasksByUserID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := t.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get tasks response: %w", err)
	}
	return &result, nil
}

func (t *TaskClientAdapter) GetTaskByID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d", taskID)

	var httpResponse *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = t.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get task API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetTaskByID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := t.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get task response: %w", err)
	}
	return &result, nil
}

func (t *TaskClientAdapter) GetTaskTreeByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d/tree", taskID)

	var httpResponse *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = t.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get task tree API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetTaskTreeByTaskID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := t.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get task tree response: %w", err)
	}
	return &result, nil
}

func (t *TaskClientAdapter) UpdateTask(ctx context.Context, taskID int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d", taskID)

	var httpResponse *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = t.apiClient.PATCH(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call update task API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateTask API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := t.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update task response: %w", err)
	}
	return &result, nil
}

func (t *TaskClientAdapter) DeleteTask(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d", taskID)

	var httpResponse *utils.HTTPResponse
	err := t.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = t.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete task API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !t.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteTask API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := t.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete task response: %w", err)
	}
	return &result, nil
}

func NewTaskClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.ITaskClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm-task"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &TaskClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
