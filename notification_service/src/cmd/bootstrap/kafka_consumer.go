/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"context"
	"log"

	"github.com/serp/notification-service/src/core/domain/enum"
	adapter "github.com/serp/notification-service/src/infrastructure/client"
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
			log.Println("Starting Kafka consumer ...")

			topics := []string{
				string(enum.USER_NOTIFICATION_TOPIC),
			}
			if err := consumer.Subscribe(topics); err != nil {
				log.Println("Failed to subscribe to topics: ", err)
				return err
			}

			consumerCtx, consumerCancel = context.WithCancel(context.Background())
			if err := consumer.StartConsumer(consumerCtx); err != nil {
				log.Println("Failed to start Kafka consumer: ", err)
				consumerCancel()
				return err
			}
			log.Println("Kafka consumer started successfully")
			return nil
		},
		OnStop: func(ctx context.Context) error {
			log.Println("Stopping Kafka consumer ...")
			if consumerCancel != nil {
				consumerCancel()
			}

			if err := consumer.Close(); err != nil {
				log.Println("Failed to stop Kafka consumer: ", err)
				return err
			}
			log.Println("Kafka consumer stopped successfully")
			return nil
		},
	})
}
