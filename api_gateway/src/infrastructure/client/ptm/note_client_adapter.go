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

type NoteClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (n *NoteClientAdapter) CreateNote(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	var httpResponse *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = n.apiClient.POST(ctx, "/api/v1/notes", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call create note API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateNote API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := n.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create note response: %w", err)
	}
	return &result, nil
}

func (n *NoteClientAdapter) SearchNotes(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)

	queryParams := make(map[string]string)
	for key, value := range payload {
		if strValue, ok := value.(string); ok {
			queryParams[key] = strValue
		}
	}

	var httpResponse *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = n.apiClient.GETWithQuery(ctx, "/api/v1/notes/search", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call search notes API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("SearchNotes API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := n.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal search notes response: %w", err)
	}
	return &result, nil
}

func (n *NoteClientAdapter) GetNoteByID(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/notes/%d", noteID)

	var httpResponse *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = n.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get note API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetNoteByID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := n.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get note response: %w", err)
	}
	return &result, nil
}

func (n *NoteClientAdapter) UpdateNote(ctx context.Context, noteID int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/notes/%d", noteID)

	var httpResponse *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = n.apiClient.PATCH(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call update note API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateNote API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := n.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update note response: %w", err)
	}
	return &result, nil
}

func (n *NoteClientAdapter) DeleteNote(ctx context.Context, noteID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/notes/%d", noteID)

	var httpResponse *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = n.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete note API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteNote API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := n.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete note response: %w", err)
	}
	return &result, nil
}

func (n *NoteClientAdapter) GetNotesByProjectID(ctx context.Context, projectID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/projects/%d/notes", projectID)

	var httpResponse *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = n.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get notes by project API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetNotesByProjectID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := n.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get notes by project response: %w", err)
	}
	return &result, nil
}

func (n *NoteClientAdapter) GetNotesByTaskID(ctx context.Context, taskID int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	url := fmt.Sprintf("/api/v1/tasks/%d/notes", taskID)

	var httpResponse *utils.HTTPResponse
	err := n.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = n.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get notes by task API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !n.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetNotesByTaskID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := n.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get notes by task response: %w", err)
	}
	return &result, nil
}

func NewNoteClientAdapter(taskManagerProps *properties.ExternalServiceProperties) port.INoteClientPort {
	baseURL := "http://" + taskManagerProps.PTMTask.Host + ":" + taskManagerProps.PTMTask.Port + "/ptm-task"
	apiClient := utils.NewBaseAPIClient(baseURL, taskManagerProps.PTMTask.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &NoteClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
