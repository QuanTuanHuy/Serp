/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package rate_limiter

import "context"

// RateLimitResult contains the outcome of a rate limit check.
type RateLimitResult struct {
	Allowed    bool
	Limit      int
	Remaining  int
	ResetAt    int64
	RetryAfter int
}

// IRateLimiterPort defines the contract for rate limiting storage operations.
type IRateLimiterPort interface {
	// CheckRateLimit atomically increments the counter for the given key
	// and returns whether the request is within the allowed limit.
	// Uses sliding window counter algorithm.
	CheckRateLimit(ctx context.Context, key string, limit int, windowSecs int) (*RateLimitResult, error)
}
