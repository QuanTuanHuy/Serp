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
	port "github.com/serp/api-gateway/src/core/port/client/crm"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type LeadClientAdapter struct {
	apiClient      *utils.BaseAPIClient
	circuitBreaker *utils.CircuitBreaker
}

func (l *LeadClientAdapter) CreateLead(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := l.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = l.apiClient.POST(ctx, "/api/v1/leads", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call create lead API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !l.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("CreateLead API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := l.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal create lead response: %w", err)
	}
	return &result, nil
}

func (l *LeadClientAdapter) UpdateLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := l.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/leads/%d", leadId)
		httpResponse, err = l.apiClient.PATCH(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call update lead API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !l.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("UpdateLead API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := l.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal update lead response: %w", err)
	}
	return &result, nil
}

func (l *LeadClientAdapter) GetLeadByID(ctx context.Context, leadId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := l.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/leads/%d", leadId)
		httpResponse, err = l.apiClient.GET(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call get lead API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !l.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetLeadByID API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := l.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get lead response: %w", err)
	}
	return &result, nil
}

func (l *LeadClientAdapter) GetLeads(ctx context.Context, page, size int) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	queryParams := map[string]string{
		"page": fmt.Sprintf("%d", page),
		"size": fmt.Sprintf("%d", size),
	}

	var httpResponse *utils.HTTPResponse
	err := l.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = l.apiClient.GETWithQuery(ctx, "/api/v1/leads", queryParams, headers)
		if err != nil {
			return fmt.Errorf("failed to call get leads API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !l.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("GetLeads API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := l.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal get leads response: %w", err)
	}
	return &result, nil
}

func (l *LeadClientAdapter) FilterLeads(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := l.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		httpResponse, err = l.apiClient.POST(ctx, "/api/v1/leads/search", payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call filter leads API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !l.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("FilterLeads API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := l.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal filter leads response: %w", err)
	}
	return &result, nil
}

func (l *LeadClientAdapter) QualifyLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := l.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/leads/%d/qualify", leadId)
		httpResponse, err = l.apiClient.POST(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call qualify lead API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !l.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("QualifyLead API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := l.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal qualify lead response: %w", err)
	}
	return &result, nil
}

func (l *LeadClientAdapter) ConvertLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := l.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/leads/%d/convert", leadId)
		httpResponse, err = l.apiClient.POST(ctx, url, payload, headers)
		if err != nil {
			return fmt.Errorf("failed to call convert lead API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !l.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("ConvertLead API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := l.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal convert lead response: %w", err)
	}
	return &result, nil
}

func (l *LeadClientAdapter) DeleteLead(ctx context.Context, leadId int64) (*response.BaseResponse, error) {
	headers := utils.BuildHeadersFromContext(ctx)
	var httpResponse *utils.HTTPResponse
	err := l.circuitBreaker.ExecuteWithoutTimeout(ctx, func(ctx context.Context) error {
		var err error
		url := fmt.Sprintf("/api/v1/leads/%d", leadId)
		httpResponse, err = l.apiClient.DELETE(ctx, url, headers)
		if err != nil {
			return fmt.Errorf("failed to call delete lead API: %w", err)
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	if !l.apiClient.IsSuccessStatusCode(httpResponse.StatusCode) {
		log.Error(ctx, fmt.Sprintf("DeleteLead API returned error status: %d", httpResponse.StatusCode))
	}

	var result response.BaseResponse
	if err := l.apiClient.UnmarshalResponse(ctx, httpResponse, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal delete lead response: %w", err)
	}
	return &result, nil
}

func NewLeadClientAdapter(props *properties.ExternalServiceProperties) port.ILeadClientPort {
	baseURL := fmt.Sprintf("http://%s:%s/crm", props.CrmService.Host, props.CrmService.Port)

	return &LeadClientAdapter{
		apiClient:      utils.NewBaseAPIClient(baseURL, props.CrmService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
