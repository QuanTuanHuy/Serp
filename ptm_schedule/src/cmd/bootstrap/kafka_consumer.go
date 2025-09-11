/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	adapter "github.com/serp/ptm-schedule/src/infrastructure/client"
	"go.uber.org/fx"
)

func InitializeKafkaConsumer(
	lc fx.Lifecycle,
	consumer *adapter.KafkaConsumer,
) {
	var consumerCtx context.Context
	var consumerCancel context.CancelFunc

	lc.Append(fx.Hook{
		OnStart: func(ctx context.Context) error {
			log.Info(ctx, "Starting Kafka consumer ...")

			topics := []string{
				string(enum.TASK_MANAGER_TOPIC),
			}
			if err := consumer.Subscribe(topics); err != nil {
				log.Error(ctx, "Failed to subscribe to topics: ", err)
				return err
			}

			consumerCtx, consumerCancel = context.WithCancel(context.Background())
			if err := consumer.StartConsumer(consumerCtx); err != nil {
				log.Error(ctx, "Failed to start Kafka consumer: ", err)
				consumerCancel()
				return err
			}
			log.Info(ctx, "Kafka consumer started successfully")
			return nil
		},
		OnStop: func(ctx context.Context) error {
			log.Info(ctx, "Stopping Kafka consumer ...")

			if consumerCancel != nil {
				consumerCancel()
			}

			if err := consumer.Close(); err != nil {
				log.Error(ctx, "Failed to stop Kafka consumer: ", err)
				return err
			}
			log.Info(ctx, "Kafka consumer stopped successfully")
			return nil
		},
	})
}
