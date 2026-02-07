/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import "time"

type CircuitBreakerProperties struct {
	MaxFailures  int
	ResetTimeout time.Duration
	Timeout      time.Duration
}
