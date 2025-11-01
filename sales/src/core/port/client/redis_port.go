/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"
	"time"
)

type IRedisPort interface {
	Set(ctx context.Context, key string, value any, expiration time.Duration) error
	Get(ctx context.Context, key string) (string, error)
	Delete(ctx context.Context, key string) error
	Exists(ctx context.Context, key string) (bool, error)
}
