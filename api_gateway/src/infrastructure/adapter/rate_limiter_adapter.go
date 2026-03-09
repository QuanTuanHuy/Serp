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
	if limit <= 0 {
		return nil, fmt.Errorf("invalid rate limit configuration: limit must be > 0")
	}

	if windowSecs <= 0 {
		return nil, fmt.Errorf("invalid rate limit configuration: windowSecs must be > 0")
	}

	now := time.Now().Unix()
	windowSize := int64(windowSecs)
	currentWindowStart := (now / windowSize) * windowSize
	previousWindowStart := currentWindowStart - windowSize

	currentKey := fmt.Sprintf("rl:%s:w%d:%d", key, windowSecs, currentWindowStart)
	previousKey := fmt.Sprintf("rl:%s:w%d:%d", key, windowSecs, previousWindowStart)

	pipe := r.redisClient.Pipeline()
	incrCmd := pipe.Incr(ctx, currentKey)
	expireCmd := pipe.Expire(ctx, currentKey, time.Duration(windowSecs*2)*time.Second)
	prevCmd := pipe.Get(ctx, previousKey)

	if _, err := pipe.Exec(ctx); err != nil && err != redis.Nil {
		if incrErr := incrCmd.Err(); incrErr != nil {
			log.Warn(ctx, "Rate limiter Redis INCR failed for key ", key, ": ", incrErr)
			return nil, incrErr
		}

		if expireErr := expireCmd.Err(); expireErr != nil {
			log.Warn(ctx, "Rate limiter Redis EXPIRE failed for key ", key, ": ", expireErr)
			return nil, expireErr
		}

		if prevErr := prevCmd.Err(); prevErr != nil && prevErr != redis.Nil {
			log.Warn(ctx, "Rate limiter Redis GET failed for key ", previousKey, ": ", prevErr)
			return nil, prevErr
		}

		log.Warn(ctx, "Rate limiter Redis pipeline failed for key ", key, ": ", err)
		return nil, err
	}

	currentCount := incrCmd.Val()

	var previousCount int64
	if val, err := prevCmd.Int64(); err == nil {
		previousCount = val
	} else if err != redis.Nil {
		log.Warn(ctx, "Rate limiter previous window parse failed for key ", previousKey, ": ", err)
		return nil, err
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
	retryAfter := int(resetAt - now)
	if retryAfter < 0 {
		retryAfter = 0
	}

	return &port.RateLimitResult{
		Allowed:    weightedCount <= limit,
		Limit:      limit,
		Remaining:  remaining,
		ResetAt:    resetAt,
		RetryAfter: retryAfter,
	}, nil
}
