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

type CommentClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (c *CommentClientAdapter) CreateComment(ctx context.Context, req *request.CreateCommentRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = c.apiClient.POST(ctx, "/api/v1/comments", req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateComment API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = c.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (c *CommentClientAdapter) GetCommentByID(ctx context.Context, commentID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/comments/%d", commentID)

	var res *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = c.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetCommentByID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = c.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (c *CommentClientAdapter) UpdateComment(ctx context.Context, commentID int64, req *request.UpdateCommentRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/comments/%d", commentID)

	var res *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = c.apiClient.PUT(ctx, url, req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateComment API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = c.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (c *CommentClientAdapter) DeleteComment(ctx context.Context, commentID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/comments/%d", commentID)

	var res *utils.HTTPResponse
	err := c.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = c.apiClient.DELETE(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !c.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteComment API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = c.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func NewCommentClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.ICommentClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm/task-manager"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &CommentClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
