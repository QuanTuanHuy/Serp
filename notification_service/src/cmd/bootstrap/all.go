/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/serp/notification-service/src/core/service"
	"github.com/serp/notification-service/src/core/usecase"
	"github.com/serp/notification-service/src/core/websocket"
	client "github.com/serp/notification-service/src/infrastructure/client"
	store "github.com/serp/notification-service/src/infrastructure/store/adapter"
	"github.com/serp/notification-service/src/kernel/utils"
	"github.com/serp/notification-service/src/ui/controller.go"
	kafkahandler "github.com/serp/notification-service/src/ui/kafka"
	"github.com/serp/notification-service/src/ui/middleware"
	"github.com/serp/notification-service/src/ui/router"
	"go.uber.org/fx"
)

func All() fx.Option {
	return fx.Options(
		// Core infrastructure
		fx.Provide(NewLogger),
		fx.Provide(NewConfig),
		fx.Provide(NewAppProperties),
		fx.Provide(NewDatabase),
		fx.Provide(NewRedisClient),
		fx.Provide(NewGinEngine),

		// Middleware
		fx.Provide(middleware.NewJWTMiddleware),
		fx.Provide(middleware.NewRoleMiddleware),

		// Utils
		fx.Provide(utils.NewKeycloakJwksUtils),
		fx.Provide(utils.NewJWTUtils),

		// Adapter
		fx.Provide(client.NewRedisAdapter),
		fx.Provide(client.NewKafkaProducerAdapter),
		fx.Provide(client.NewKafkaConsumer),

		fx.Provide(store.NewDBTransactionAdapter),
		fx.Provide(store.NewNotificationAdapter),
		fx.Provide(store.NewPreferenceAdapter),
		fx.Provide(store.NewFailedEventAdapter),
		fx.Provide(store.NewProcessedEventAdapter),

		// Services
		fx.Provide(service.NewTransactionService),
		fx.Provide(service.NewPreferenceService),
		fx.Provide(service.NewNotificationService),
		fx.Provide(service.NewDeliveryService),
		fx.Provide(service.NewIdempotencyService),

		// WebSocket Hub
		fx.Provide(websocket.NewHub),

		// Use cases
		fx.Provide(usecase.NewPreferenceUseCase),
		fx.Provide(usecase.NewNotificationUseCase),

		// Controllers
		fx.Provide(controller.NewPreferenceController),
		fx.Provide(controller.NewNotificationController),
		fx.Provide(controller.NewWebSocketController),

		// Router
		fx.Provide(NewRouterConfig),

		// Kafka consumer
		fx.Provide(kafkahandler.NewMessageProcessingMiddleware),
		fx.Invoke(InitializeKafkaConsumer),

		// Lifecycle
		fx.Invoke(InitializeDB),
		fx.Invoke(router.RegisterRoutes),
		fx.Invoke(StartServer),
	)
}
