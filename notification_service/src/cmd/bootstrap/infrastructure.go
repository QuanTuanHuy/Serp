/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
	"github.com/serp/notification-service/src/kernel/properties"
	"go.uber.org/zap"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func NewDatabase(props *properties.AppProperties, zapLogger *zap.Logger) (*gorm.DB, error) {
	dsn := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable",
		props.Datasource.Host,
		props.Datasource.Port,
		props.Datasource.Username,
		props.Datasource.Password,
		props.Datasource.Database)

	gormLogger := logger.New(
		&zapGormLogger{zapLogger: zapLogger},
		logger.Config{
			SlowThreshold:             200 * time.Millisecond,
			LogLevel:                  logger.Info,
			IgnoreRecordNotFoundError: true,
			Colorful:                  false,
		},
	)

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		Logger: gormLogger,
	})
	if err != nil {
		return nil, fmt.Errorf("failed to connect to database: %w", err)
	}

	sqlDB, err := db.DB()
	if err != nil {
		return nil, fmt.Errorf("failed to get database instance: %w", err)
	}

	// Set connection pool settings
	sqlDB.SetMaxIdleConns(10)
	sqlDB.SetMaxOpenConns(100)
	sqlDB.SetConnMaxLifetime(time.Hour)

	zapLogger.Info("Database connected successfully",
		zap.String("host", props.Datasource.Host),
		zap.Int("port", props.Datasource.Port),
		zap.String("database", props.Datasource.Database))

	return db, nil
}

func NewRedisClient(props *properties.AppProperties, zapLogger *zap.Logger) (*redis.Client, error) {
	client := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", props.Redis.Host, props.Redis.Port),
		Password: "",
		DB:       0,
	})

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := client.Ping(ctx).Err(); err != nil {
		return nil, fmt.Errorf("failed to connect to Redis: %w", err)
	}

	zapLogger.Info("Redis connected successfully",
		zap.String("host", props.Redis.Host),
		zap.Int("port", props.Redis.Port))

	return client, nil
}

type zapGormLogger struct {
	zapLogger *zap.Logger
}

func (l *zapGormLogger) Printf(format string, args ...interface{}) {
	l.zapLogger.Info(fmt.Sprintf(format, args...))
}
