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

type ProjectClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (p *ProjectClientAdapter) GetProjectsByUserID(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = p.apiClient.GET(ctx, "/api/v1/projects/all", headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetProjectsByUserID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = p.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (p *ProjectClientAdapter) CreateProject(ctx context.Context, req *request.CreateProjectRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = p.apiClient.POST(ctx, "/api/v1/projects", req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateProject API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = p.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (p *ProjectClientAdapter) UpdateProject(ctx context.Context, projectID int64, req *request.UpdateProjectRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d", projectID)

	var res *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = p.apiClient.PUT(ctx, url, req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateProject API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = p.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (p *ProjectClientAdapter) GetProjectByID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d", projectID)

	var res *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = p.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetProjectByID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = p.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (p *ProjectClientAdapter) GetProjects(ctx context.Context, params *request.GetProjectsRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := utils.BuildQueryParams(params)
	url := "/api/v1/projects" + queryParams

	var res *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = p.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetProjects API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = p.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (p *ProjectClientAdapter) GetProjectsByName(ctx context.Context, name string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/search?name=%s", name)

	var res *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = p.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetProjectsByName API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = p.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (p *ProjectClientAdapter) ArchiveProject(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d/archive", projectID)

	var res *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = p.apiClient.PUT(ctx, url, nil, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ArchiveProject API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = p.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (p *ProjectClientAdapter) GetGroupTasksByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d/group-tasks", projectID)

	var res *utils.HTTPResponse
	err := p.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = p.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !p.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetGroupTasksByProjectID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = p.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func NewProjectClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.IProjectClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm/task-manager"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &ProjectClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
