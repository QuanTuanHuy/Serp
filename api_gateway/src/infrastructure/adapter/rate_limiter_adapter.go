/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/redis/go-redis/v9"
	port "github.com/serp/api-gateway/src/core/port/rate_limiter"
)

type RateLimiterAdapter struct {
	redisClient *redis.Client
}

func NewRateLimiterAdapter(redisClient *redis.Client) port.IRateLimiterPort {
	return &RateLimiterAdapter{
		redisClient: redisClient,
	}
}

func (r *RateLimiterAdapter) CheckRateLimit(
	ctx context.Context, key string, limit int, windowSecs int,
) (*port.RateLimitResult, error) {
	now := time.Now().Unix()
	windowSize := int64(windowSecs)
	currentWindowStart := (now / windowSize) * windowSize
	previousWindowStart := currentWindowStart - windowSize

	currentKey := fmt.Sprintf("rl:%s:%d", key, currentWindowStart)
	previousKey := fmt.Sprintf("rl:%s:%d", key, previousWindowStart)

	pipe := r.redisClient.Pipeline()
	incrCmd := pipe.Incr(ctx, currentKey)
	pipe.Expire(ctx, currentKey, time.Duration(windowSecs*2)*time.Second)
	prevCmd := pipe.Get(ctx, previousKey)

	_, err := pipe.Exec(ctx)
	if err != nil && err != redis.Nil {
		if incrCmd.Err() != nil {
			log.Warn(ctx, "Rate limiter Redis INCR failed for key ", key, ": ", incrCmd.Err())
			return nil, incrCmd.Err()
		}
	}

	currentCount := incrCmd.Val()

	var previousCount int64
	if val, err := prevCmd.Int64(); err == nil {
		previousCount = val
	}

	// Sliding window weighted count
	elapsed := float64(now - currentWindowStart)
	overlapWeight := 1.0 - (elapsed / float64(windowSize))
	if overlapWeight < 0 {
		overlapWeight = 0
	}

	weightedCount := int(float64(previousCount)*overlapWeight) + int(currentCount)

	resetAt := currentWindowStart + windowSize
	remaining := max(limit-weightedCount, 0)

	return &port.RateLimitResult{
		Allowed:    weightedCount <= limit,
		Limit:      limit,
		Remaining:  remaining,
		ResetAt:    resetAt,
		RetryAfter: int(resetAt - now),
	}, nil
}
