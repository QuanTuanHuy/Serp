/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"
	"net/url"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/crm"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type OpportunityClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (o *OpportunityClientAdapter) CreateOpportunity(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.POST(ctx, "/api/v1/opportunities", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call create opportunity API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateOpportunity API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create opportunity response: %w", err)
	}
	return &result, nil
}

func (o *OpportunityClientAdapter) UpdateOpportunity(ctx context.Context, opportunityId int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/opportunities/%d", opportunityId)
		httpResponse, err = o.apiClient.PATCH(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call update opportunity API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateOpportunity API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update opportunity response: %w", err)
	}
	return &result, nil
}

func (o *OpportunityClientAdapter) GetOpportunityByID(ctx context.Context, opportunityId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/opportunities/%d", opportunityId)
		httpResponse, err = o.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get opportunity API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOpportunityByID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get opportunity response: %w", err)
	}
	return &result, nil
}

func (o *OpportunityClientAdapter) GetOpportunities(ctx context.Context, page, size int) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	queryParams := map[string]string{
		"page": fmt.Sprintf("%d", page),
		"size": fmt.Sprintf("%d", size),
	}

	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.GETWithQuery(ctx, "/api/v1/opportunities", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get opportunities API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetOpportunities API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get opportunities response: %w", err)
	}
	return &result, nil
}

func (o *OpportunityClientAdapter) FilterOpportunities(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = o.apiClient.POST(ctx, "/api/v1/opportunities/search", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call filter opportunities API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("FilterOpportunities API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal filter opportunities response: %w", err)
	}
	return &result, nil
}

func (o *OpportunityClientAdapter) ChangeStage(ctx context.Context, opportunityId int64, newStage string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/opportunities/%d/stage?newStage=%s", opportunityId, url.QueryEscape(newStage))
		httpResponse, err = o.apiClient.PATCH(ctx, path, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call change stage API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ChangeStage API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal change stage response: %w", err)
	}
	return &result, nil
}

func (o *OpportunityClientAdapter) CloseAsWon(ctx context.Context, opportunityId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/opportunities/%d/close-won", opportunityId)
		httpResponse, err = o.apiClient.POST(ctx, url, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call close as won API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CloseAsWon API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal close as won response: %w", err)
	}
	return &result, nil
}

func (o *OpportunityClientAdapter) CloseAsLost(ctx context.Context, opportunityId int64, lostReason string) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		path := fmt.Sprintf("/api/v1/opportunities/%d/close-lost?lostReason=%s", opportunityId, url.QueryEscape(lostReason))
		httpResponse, err = o.apiClient.POST(ctx, path, nil, headers)
		if err != nil {
			return fmt.Errorf("failed to call close as lost API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CloseAsLost API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal close as lost response: %w", err)
	}
	return &result, nil
}

func (o *OpportunityClientAdapter) DeleteOpportunity(ctx context.Context, opportunityId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := o.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/opportunities/%d", opportunityId)
		httpResponse, err = o.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete opportunity API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !o.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteOpportunity API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := o.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete opportunity response: %w", err)
	}
	return &result, nil
}

func NewOpportunityClientAdapter(props *properties.ExternalServiceProperties) port.IOpportunityClientPort {
	baseURL := fmt.Sprintf("http://%s:%s/crm", props.CrmService.Host, props.CrmService.Port)

	return &OpportunityClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.CrmService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
