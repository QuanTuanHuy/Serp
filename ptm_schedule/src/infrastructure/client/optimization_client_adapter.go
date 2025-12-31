/*
Author: QuanTuanHuy
Description: Part of Serp Project - Optimization Client HTTP Adapter
*/

package adapter

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/dto/optimization"
	portClient "github.com/serp/ptm-schedule/src/core/port/client"
	"github.com/serp/ptm-schedule/src/kernel/properties"
)

const (
	scheduleEndpoint     = "/api/v1/optimization/schedule"
	scheduleWithFallback = "/api/v1/optimization/schedule-with-fallback"
	healthEndpoint       = "/actuator/health"
)

type OptimizationClientAdapter struct {
	httpClient *http.Client
	config     *properties.OptimizationProperties
}

func NewOptimizationClientAdapter(config *properties.OptimizationProperties) portClient.IOptimizationClient {
	return &OptimizationClientAdapter{
		httpClient: &http.Client{
			Timeout: config.GetTimeout(),
		},
		config: config,
	}
}

func (c *OptimizationClientAdapter) Optimize(
	ctx context.Context,
	req *optimization.OptimizationRequest,
	strategy optimization.StrategyType,
) (*optimization.PlanResult, error) {
	url := fmt.Sprintf("%s%s?strategy=%s", c.config.GetBaseURL(), scheduleEndpoint, strategy)

	return c.doOptimize(ctx, url, req)
}

func (c *OptimizationClientAdapter) OptimizeWithFallback(
	ctx context.Context,
	req *optimization.OptimizationRequest,
) (*optimization.PlanResult, error) {
	url := fmt.Sprintf("%s%s", c.config.GetBaseURL(), scheduleWithFallback)

	return c.doOptimize(ctx, url, req)
}

func (c *OptimizationClientAdapter) doOptimize(
	ctx context.Context,
	url string,
	req *optimization.OptimizationRequest,
) (*optimization.PlanResult, error) {
	body, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal optimization request: %w", err)
	}

	var lastErr error
	retryCount := c.config.GetRetryCount()
	for attempt := 0; attempt <= retryCount; attempt++ {
		if attempt > 0 {
			log.Infof("Retrying optimization request (attempt %d/%d)", attempt, retryCount)
			time.Sleep(c.config.GetRetryDelay())
		}

		result, err := c.doRequest(ctx, url, body)
		if err == nil {
			return result, nil
		}

		lastErr = err
		log.Warnf("Optimization request failed (attempt %d/%d): %v", attempt+1, retryCount+1, err)
	}

	return nil, fmt.Errorf("optimization request failed after %d attempts: %w", retryCount+1, lastErr)
}

func (c *OptimizationClientAdapter) doRequest(
	ctx context.Context,
	url string,
	body []byte,
) (*optimization.PlanResult, error) {
	httpReq, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewReader(body))
	if err != nil {
		return nil, fmt.Errorf("failed to create HTTP request: %w", err)
	}

	httpReq.Header.Set("Content-Type", "application/json")

	resp, err := c.httpClient.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("HTTP request failed: %w", err)
	}
	defer resp.Body.Close()

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("optimization service returned status %d: %s", resp.StatusCode, string(respBody))
	}

	var generalResp optimization.GeneralResponse
	if err := json.Unmarshal(respBody, &generalResp); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}

	if generalResp.Code != 200 {
		return nil, fmt.Errorf("optimization service error: %s", generalResp.Message)
	}

	return generalResp.Data, nil
}

func (c *OptimizationClientAdapter) HealthCheck(ctx context.Context) error {
	url := fmt.Sprintf("%s%s", c.config.GetBaseURL(), healthEndpoint)

	httpReq, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return fmt.Errorf("failed to create health check request: %w", err)
	}

	resp, err := c.httpClient.Do(httpReq)
	if err != nil {
		return fmt.Errorf("health check failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("optimization service unhealthy: status %d", resp.StatusCode)
	}

	return nil
}
