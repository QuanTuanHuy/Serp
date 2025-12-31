/*
Author: QuanTuanHuy
Description: Part of Serp Project - Optimization Service Properties
*/

package properties

import (
	"time"

	"github.com/golibs-starter/golib/config"
)

type OptimizationProperties struct {
	BaseURL      string `mapstructure:"baseUrl"`
	TimeoutMs    int    `mapstructure:"timeoutMs"`
	RetryCount   int    `mapstructure:"retryCount"`
	RetryDelayMs int    `mapstructure:"retryDelayMs"`
}

func (o OptimizationProperties) Prefix() string {
	return "app.optimization"
}

func (o OptimizationProperties) GetTimeout() time.Duration {
	if o.TimeoutMs <= 0 {
		return 60 * time.Second
	}
	return time.Duration(o.TimeoutMs) * time.Millisecond
}

func (o OptimizationProperties) GetRetryDelay() time.Duration {
	if o.RetryDelayMs <= 0 {
		return 1 * time.Second
	}
	return time.Duration(o.RetryDelayMs) * time.Millisecond
}

func (o OptimizationProperties) GetRetryCount() int {
	if o.RetryCount <= 0 {
		return 3
	}
	return o.RetryCount
}

func (o OptimizationProperties) GetBaseURL() string {
	if o.BaseURL == "" {
		return "http://localhost:8085/ptm-optimization"
	}
	return o.BaseURL
}

func NewOptimizationProperties(loader config.Loader) (*OptimizationProperties, error) {
	var props OptimizationProperties
	if err := loader.Bind(&props); err != nil {
		return nil, err
	}
	return &props, nil
}
