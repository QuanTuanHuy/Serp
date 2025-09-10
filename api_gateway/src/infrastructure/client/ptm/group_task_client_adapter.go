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

type GroupTaskClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (g *GroupTaskClientAdapter) CreateGroupTask(ctx context.Context, req *request.CreateGroupTaskRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := g.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = g.apiClient.POST(ctx, "/api/v1/group-tasks", req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !g.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateGroupTask API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = g.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (g *GroupTaskClientAdapter) GetGroupTaskByID(ctx context.Context, groupTaskID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/group-tasks/%d", groupTaskID)

	var res *utils.HTTPResponse
	err := g.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = g.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !g.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetGroupTaskByID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = g.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func NewGroupTaskClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.IGroupTaskClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm/task-manager"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &GroupTaskClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
