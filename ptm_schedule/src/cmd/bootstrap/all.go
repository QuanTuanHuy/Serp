/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/golibs-starter/golib"
	golibdata "github.com/golibs-starter/golib-data"
	golibgin "github.com/golibs-starter/golib-gin"
	"github.com/serp/ptm-schedule/src/core/service"
	"github.com/serp/ptm-schedule/src/core/usecase"
	adapter2 "github.com/serp/ptm-schedule/src/infrastructure/client"
	"github.com/serp/ptm-schedule/src/infrastructure/store/adapter"
	"github.com/serp/ptm-schedule/src/kernel/properties"
	"github.com/serp/ptm-schedule/src/kernel/utils"
	"github.com/serp/ptm-schedule/src/ui/controller"
	kafkahandler "github.com/serp/ptm-schedule/src/ui/kafka"
	"github.com/serp/ptm-schedule/src/ui/middleware"
	"github.com/serp/ptm-schedule/src/ui/router"
	"go.uber.org/fx"
)

func All() fx.Option {
	return fx.Options(
		golib.AppOpt(),
		golib.PropertiesOpt(),
		golib.LoggingOpt(),
		golib.EventOpt(),
		golib.BuildInfoOpt(Version, CommitHash, BuildTime),
		golib.ActuatorEndpointOpt(),
		golib.HttpClientOpt(),

		// Provide datasource auto properties
		golibdata.RedisOpt(),
		golibdata.DatasourceOpt(),

		// Provide properties
		golib.ProvideProps(properties.NewKeycloakProperties),
		golib.ProvideProps(properties.NewKafkaProducerProperties),
		golib.ProvideProps(properties.NewKafkaConsumerProperties),

		fx.Invoke(InitializeDB),

		// Provide adapter
		fx.Provide(adapter2.NewKafkaProducerAdapter),
		fx.Provide(adapter2.NewKafkaConsumer),

		fx.Provide(adapter.NewDBTransactionAdapter),
		fx.Provide(adapter.NewSchedulePlanStoreAdapter),
		fx.Provide(adapter.NewScheduleTaskStoreAdapter),
		fx.Provide(adapter.NewAvailabilityCalendarAdapter),
		fx.Provide(adapter.NewCalendarExceptionAdapter),
		fx.Provide(adapter.NewScheduleWindowAdapter),
		fx.Provide(adapter.NewScheduleEventAdapter),

		// Provide service
		fx.Provide(service.NewTransactionService),
		fx.Provide(service.NewSchedulePlanService),
		fx.Provide(service.NewScheduleTaskService),
		fx.Provide(service.NewAvailabilityCalendarService),
		fx.Provide(service.NewCalendarExceptionService),
		fx.Provide(service.NewScheduleWindowService),
		fx.Provide(service.NewScheduleEventService),

		// Provide usecase
		fx.Provide(usecase.NewSchedulePlanUseCase),
		fx.Provide(usecase.NewScheduleTaskUseCase),
		fx.Provide(usecase.NewAvailabilityCalendarUseCase),
		fx.Provide(usecase.NewCalendarExceptionUseCase),
		fx.Provide(usecase.NewScheduleWindowUseCase),
		fx.Provide(usecase.NewScheduleEventUseCase),

		// Provide controller
		fx.Provide(controller.NewSchedulePlanController),
		fx.Provide(controller.NewScheduleTaskController),
		fx.Provide(controller.NewAvailabilityCalendarController),
		fx.Provide(controller.NewCalendarExceptionController),
		fx.Provide(controller.NewScheduleWindowController),
		fx.Provide(controller.NewScheduleEventController),

		// Provide JWT components
		fx.Provide(utils.NewKeycloakJwksUtils),
		fx.Provide(utils.NewJWTUtils),
		fx.Provide(middleware.NewJWTMiddleware),

		// Http server
		golibgin.GinHttpServerOpt(),
		fx.Invoke(router.RegisterGinRouters),
		golibgin.OnStopHttpServerOpt(),

		// Kafka consumer
		fx.Provide(kafkahandler.NewPtmTaskHandler),
		fx.Invoke(InitializeKafkaConsumer),
	)
}
