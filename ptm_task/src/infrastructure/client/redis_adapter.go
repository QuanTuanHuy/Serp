/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"encoding/json"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/redis/go-redis/v9"
	port "github.com/serp/ptm-task/src/core/port/client"
)

type RedisAdapter struct {
	redisClient *redis.Client
}

func (r *RedisAdapter) DeleteKeyFromRedis(ctx context.Context, key string) error {
	if err := r.redisClient.Del(ctx, key).Err(); err != nil {
		log.Error(ctx, "Failed to delete key from redis ", key, " error: ", err)
		return err
	}
	return nil
}

func (r *RedisAdapter) ExistsInRedis(ctx context.Context, key string) (bool, error) {
	exists, err := r.redisClient.Exists(ctx, key).Result()
	if err != nil {
		log.Error(ctx, "Failed to check existence of key in redis ", key, " error: ", err)
		return false, err
	}
	return exists > 0, nil
}

func (r *RedisAdapter) GetFromRedis(ctx context.Context, key string, destination any) error {
	data, err := r.redisClient.Get(ctx, key).Result()
	if err != nil {
		if err == redis.Nil {
			return nil
		}
		log.Error(ctx, "Failed to get from redis key ", key, " error: ", err)
		return err
	}
	if err = json.Unmarshal([]byte(data), destination); err != nil {
		log.Error(ctx, "Failed to unmarshal data for key ", key, " error: ", err)
		return err
	}
	return nil
}

func (r *RedisAdapter) GetHSetFromRedis(ctx context.Context, key string) (map[string]string, error) {
	data, err := r.redisClient.HGetAll(ctx, key).Result()
	if err != nil {
		if err == redis.Nil {
			return nil, nil
		}
		log.Error(ctx, "Failed to get HSet from redis key ", key, " error: ", err)
		return nil, err
	}
	return data, nil
}

func (r *RedisAdapter) GetKeysFromRedis(ctx context.Context, pattern string) ([]string, error) {
	keys, err := r.redisClient.Keys(ctx, pattern).Result()
	if err != nil {
		log.Error(ctx, "Failed to get keys from redis with pattern ", pattern, " error: ", err)
		return nil, err
	}
	return keys, nil
}

func (r *RedisAdapter) SetHSetToRedis(ctx context.Context, key string, mapData map[string]any, ttl int) error {
	if err := r.redisClient.HSet(ctx, key, mapData).Err(); err != nil {
		log.Error(ctx, "Failed to set HSet to redis key ", key, " error: ", err)
		return err
	}
	if ttl > 0 {
		if err := r.redisClient.Expire(ctx, key, time.Duration(ttl)*time.Second).Err(); err != nil {
			log.Error(ctx, "Failed to set TTL for HSet key ", key, " error: ", err)
			return err
		}
	}
	return nil
}

func (r *RedisAdapter) SetToRedis(ctx context.Context, key string, value any, ttl int) error {
	data, err := json.Marshal(value)
	if err != nil {
		log.Error(ctx, "Failed to marshal value for key ", key, " error: ", err)
		return err
	}
	if err = r.redisClient.Set(ctx, key, data, time.Duration(ttl)*time.Second).Err(); err != nil {
		log.Error(ctx, "Failed to set to redis value for key ", key, " error: ", err)
		return err
	}
	return nil
}

func NewRedisAdapter(redisClient *redis.Client) port.IRedisPort {
	return &RedisAdapter{
		redisClient: redisClient,
	}
}
