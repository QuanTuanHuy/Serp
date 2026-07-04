/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib"
	"github.com/golibs-starter/golib/web/actuator"
	"github.com/serp/api-gateway/src/ui/controller/common"
	crm "github.com/serp/api-gateway/src/ui/controller/crm"
	ptm "github.com/serp/api-gateway/src/ui/controller/ptm"
	"github.com/serp/api-gateway/src/ui/middleware"
	"go.uber.org/fx"
)

type RegisterRoutersIn struct {
	fx.In
	App      *golib.App
	Engine   *gin.Engine
	Actuator *actuator.Endpoint

	WebSocketProxyController *common.WebSocketProxyController
	GenericProxyController   *common.GenericProxyController

	LeadController        *crm.LeadController
	OpportunityController *crm.OpportunityController
	CustomerController    *crm.CustomerController
	ContactController     *crm.ContactController

	ProjectController *ptm.ProjectController
	TaskController    *ptm.TaskController
	NoteController    *ptm.NoteController

	SchedulePlanController         *ptm.SchedulePlanController
	AvailabilityCalendarController *ptm.AvailabilityCalendarController
	ScheduleWindowController       *ptm.ScheduleWindowController
	ScheduleEventController        *ptm.ScheduleEventController
	ScheduleTaskController         *ptm.ScheduleTaskController

	JWTMiddleware                 *middleware.JWTMiddleware
	CorsMiddleware                *middleware.CorsMiddleware
	RateLimitMiddleware           *middleware.RateLimitMiddleware
	AccountProxyJWTGateMiddleware *middleware.AccountProxyJWTGateMiddleware
}

func RegisterGinRouters(p RegisterRoutersIn) {
	p.Engine.Use(p.CorsMiddleware.Handler())
	p.Engine.Use(p.RateLimitMiddleware.IPRateLimit())

	group := p.Engine.Group(p.App.Path())

	group.GET("/actuator/health", gin.WrapF(p.Actuator.Health))
	group.GET("/actuator/info", gin.WrapF(p.Actuator.Info))

	RegisterAccountRoutes(
		group,
		p.App.Path(),
		p.GenericProxyController,
		p.AccountProxyJWTGateMiddleware,
	)

	RegisterCrmRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterPtmRoutes(
		group,
		p.ProjectController,
		p.TaskController,
		p.NoteController,
		p.SchedulePlanController,
		p.AvailabilityCalendarController,
		p.ScheduleWindowController,
		p.ScheduleEventController,
		p.ScheduleTaskController,
	)

	RegisterPurchaseRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterLogisticsRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterLogistics2Routes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterSchoolBusRoutes(
		group,
		p.WebSocketProxyController,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterFirstMileRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterSecondMileRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterPaymentRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterTmsBillingRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterTmsOrderRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterNotificationRoutes(
		group,
		p.WebSocketProxyController,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterDiscussRoutes(
		group,
		p.WebSocketProxyController,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterSalesRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterPmCoreRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)

	RegisterTtcrsRoutes(
		group,
		p.GenericProxyController,
		p.JWTMiddleware,
		p.RateLimitMiddleware,
	)
}
