/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib"
	"github.com/golibs-starter/golib/web/actuator"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/ui/controller"
	"github.com/serp/ptm-schedule/src/ui/middleware"
	"go.uber.org/fx"
)

type RegisterRoutersIn struct {
	fx.In
	App      *golib.App
	Engine   *gin.Engine
	Actuator *actuator.Endpoint

	SchedulePlanController         *controller.SchedulePlanController
	ScheduleGroupController        *controller.ScheduleGroupController
	ScheduleTaskController         *controller.ScheduleTaskController
	AvailabilityCalendarController *controller.AvailabilityCalendarController
	CalendarExceptionController    *controller.CalendarExceptionController
	ScheduleWindowController       *controller.ScheduleWindowController
	ScheduleEventController        *controller.ScheduleEventController

	JWTMiddleware *middleware.JWTMiddleware
}

func RegisterGinRouters(p RegisterRoutersIn) {
	group := p.Engine.Group(p.App.Path())

	group.GET("/actuator/health", gin.WrapF(p.Actuator.Health))
	group.GET("/actuator/info", gin.WrapF(p.Actuator.Info))

	requiredAuthV1 := group.Group("/api/v1")
	requiredAuthV1.Use(p.JWTMiddleware.AuthenticateJWT(), p.JWTMiddleware.RequireAnyRole(string(enum.PTM_ADMIN), string(enum.PTM_USER)))
	{
		schedulePlanV1 := requiredAuthV1.Group("/schedule-plans")
		{
			schedulePlanV1.POST("", p.SchedulePlanController.CreateSchedulePlan)
		}

		scheduleGroupV1 := requiredAuthV1.Group("/schedule-groups")
		{
			scheduleGroupV1.GET("", p.ScheduleGroupController.GetScheduleGroupsByUserID)
			scheduleGroupV1.POST("", p.ScheduleGroupController.CreateScheduleGroup)
			scheduleGroupV1.GET("/:id", p.ScheduleGroupController.GetScheduleGroupByID)
			scheduleGroupV1.DELETE("/:id", p.ScheduleGroupController.DeleteScheduleGroup)
		}

		availabilityV1 := requiredAuthV1.Group("/availability-calendar")
		{
			availabilityV1.GET("", p.AvailabilityCalendarController.GetAvailability)
			availabilityV1.POST("", p.AvailabilityCalendarController.SetAvailability)
			availabilityV1.PUT("", p.AvailabilityCalendarController.ReplaceAvailability)
		}

		exceptionV1 := requiredAuthV1.Group("/calendar-exceptions")
		{
			exceptionV1.GET("", p.CalendarExceptionController.ListExceptions)
			exceptionV1.POST("", p.CalendarExceptionController.SaveExceptions)
			exceptionV1.PUT("", p.CalendarExceptionController.ReplaceExceptions)
			exceptionV1.DELETE("", p.CalendarExceptionController.DeleteExceptions)
		}

		windowV1 := requiredAuthV1.Group("/schedule-windows")
		{
			windowV1.GET("", p.ScheduleWindowController.ListAvailabilityWindows)
			windowV1.POST("/materialize", p.ScheduleWindowController.MaterializeWindows)
		}

		eventV1 := requiredAuthV1.Group("/schedule-events")
		{
			eventV1.GET("", p.ScheduleEventController.ListEvents)
			eventV1.POST("", p.ScheduleEventController.SaveEvents)
			eventV1.PATCH("/:id/status", p.ScheduleEventController.UpdateEventStatus)
		}
	}

}
