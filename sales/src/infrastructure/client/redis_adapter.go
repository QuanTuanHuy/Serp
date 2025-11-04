/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package client

import (
	"context"
	"time"

	"github.com/redis/go-redis/v9"
	port "github.com/serp/sales/src/core/port/client"
)

type RedisAdapter struct {
	client *redis.Client
}

func (r *RedisAdapter) Set(ctx context.Context, key string, value interface{}, expiration time.Duration) error {
	return r.client.Set(ctx, key, value, expiration).Err()
}

func (r *RedisAdapter) Get(ctx context.Context, key string) (string, error) {
	return r.client.Get(ctx, key).Result()
}

func (r *RedisAdapter) Delete(ctx context.Context, key string) error {
	return r.client.Del(ctx, key).Err()
}

func (r *RedisAdapter) Exists(ctx context.Context, key string) (bool, error) {
	result, err := r.client.Exists(ctx, key).Result()
	return result > 0, err
}

func NewRedisAdapter(client *redis.Client) port.IRedisPort {
	return &RedisAdapter{client: client}
}
