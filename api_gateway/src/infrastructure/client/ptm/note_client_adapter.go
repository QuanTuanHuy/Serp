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

type NoteClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (n *NoteClientAdapter) CreateNote(ctx context.Context, req *request.CreateNoteRequest) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = n.apiClient.POST(ctx, "/api/v1/notes", req, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateNote API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = n.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (n *NoteClientAdapter) GetAllNotes(ctx context.Context) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var res *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = n.apiClient.GET(ctx, "/api/v1/notes", headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetAllNotes API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = n.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (n *NoteClientAdapter) GetNoteByID(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/notes/%d", noteID)

	var res *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = n.apiClient.GET(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetNoteByID API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = n.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (n *NoteClientAdapter) DeleteNote(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/notes/%d", noteID)

	var res *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = n.apiClient.DELETE(ctx, url, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteNote API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = n.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (n *NoteClientAdapter) LockNote(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/notes/%d/lock", noteID)

	var res *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = n.apiClient.PUT(ctx, url, nil, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("LockNote API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = n.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func (n *NoteClientAdapter) UnlockNote(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/notes/%d/unlock", noteID)

	var res *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		res, err = n.apiClient.PUT(ctx, url, nil, headers)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(res.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UnlockNote API returned error status: %d", res.StatusCode))
	}

	var result response.BaseResponse
	err = n.apiClient.UnmarshalResponse(ctx, res, &result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

func NewNoteClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.INoteClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm/task-manager"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &NoteClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
