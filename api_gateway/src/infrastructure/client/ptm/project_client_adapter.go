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

type ProjectClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (p *ProjectClientAdapter) CreateProject(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.POST(ctx, "/api/v1/projects", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call create project API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateProject API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create project response: %w", err)
	}
	return &result, nil
}

func (p *ProjectClientAdapter) GetAllProjects(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := make(map[string]string)
	for key, value := range payload {
		if strValue, ok := value.(string); ok {
			queryParams[key] = strValue
		}
	}

	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.GETWithQuery(ctx, "/api/v1/projects", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get all projects API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAllProjects API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get all projects response: %w", err)
	}
	return &result, nil
}

func (p *ProjectClientAdapter) GetProjectByID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d", projectID)

	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get project API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetProjectByID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get project response: %w", err)
	}
	return &result, nil
}

func (p *ProjectClientAdapter) GetTasksByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d/tasks", projectID)

	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get tasks by project API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetTasksByProjectID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get tasks by project response: %w", err)
	}
	return &result, nil
}

func (p *ProjectClientAdapter) UpdateProject(ctx context.Context, projectID int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d", projectID)

	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.PATCH(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call update project API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateProject API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update project response: %w", err)
	}
	return &result, nil
}

func (p *ProjectClientAdapter) DeleteProject(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d", projectID)

	var httpResponse *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = p.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete project API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteProject API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := p.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete project response: %w", err)
	}
	return &result, nil
}

func NewProjectClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.IProjectClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm-task"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ProjectClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
