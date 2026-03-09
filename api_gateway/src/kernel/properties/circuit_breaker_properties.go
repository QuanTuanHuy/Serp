/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import "time"

// CircuitBreakerProperties holds configuration for the custom CircuitBreaker utility.
type CircuitBreakerProperties struct {
	MaxFailures  int
	ResetTimeout time.Duration
	Timeout      time.Duration
}
