/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import (
	"time"

	"github.com/golibs-starter/golib/config"
)

// ResilienceProperties holds circuit breaker and retry configuration for the proxy layer.
type ResilienceProperties struct {
	// Circuit breaker settings
	MaxRequests         uint32        `mapstructure:"maxRequests"`
	Interval            time.Duration `mapstructure:"interval"`
	Timeout             time.Duration `mapstructure:"timeout"`
	ConsecutiveFailures uint32        `mapstructure:"consecutiveFailures"`
	FailureRatio        float64       `mapstructure:"failureRatio"`
	MinRequests         uint32        `mapstructure:"minRequests"`

	// Retry settings
	MaxRetries   int           `mapstructure:"maxRetries"`
	InitialDelay time.Duration `mapstructure:"initialDelay"`
	MaxDelay     time.Duration `mapstructure:"maxDelay"`
}

func (r ResilienceProperties) Prefix() string {
	return "app.resilience"
}

func NewResilienceProperties(loader config.Loader) (*ResilienceProperties, error) {
	props := NewDefaultResilienceProperties()
	err := loader.Bind(&props)
	return &props, err
}

// NewDefaultResilienceProperties returns sensible defaults so the gateway works without explicit config.
func NewDefaultResilienceProperties() ResilienceProperties {
	return ResilienceProperties{
		MaxRequests:         3,
		Interval:            60 * time.Second,
		Timeout:             30 * time.Second,
		ConsecutiveFailures: 5,
		FailureRatio:        0.6,
		MinRequests:         10,

		MaxRetries:   3,
		InitialDelay: 100 * time.Millisecond,
		MaxDelay:     2 * time.Second,
	}
}
