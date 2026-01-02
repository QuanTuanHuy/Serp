/*
Author: QuanTuanHuy
Description: Part of Serp Project - Optimization Client Port Interface
*/

package client

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/dto/optimization"
)

// IOptimizationClient defines the interface for calling ptm_optimization service
type IOptimizationClient interface {
	// Optimize calls ptm_optimization with specified strategy
	Optimize(ctx context.Context, req *optimization.OptimizationRequest, strategy optimization.StrategyType) (*optimization.PlanResult, error)

	// OptimizeWithFallback calls ptm_optimization with fallback chain
	OptimizeWithFallback(ctx context.Context, req *optimization.OptimizationRequest) (*optimization.PlanResult, error)

	// HealthCheck verifies if ptm_optimization service is available
	HealthCheck(ctx context.Context) error
}
