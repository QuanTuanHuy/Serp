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
	ScheduleTaskController         *controller.ScheduleTaskController
	AvailabilityCalendarController *controller.AvailabilityCalendarController
	CalendarExceptionController    *controller.CalendarExceptionController
	ScheduleWindowController       *controller.ScheduleWindowController
	ScheduleEventController        *controller.ScheduleEventController

	JWTMiddleware  *middleware.JWTMiddleware
	RoleMiddleware *middleware.RoleMiddleware
}

func RegisterGinRouters(p RegisterRoutersIn) {
	group := p.Engine.Group(p.App.Path())

	group.GET("/actuator/health", gin.WrapF(p.Actuator.Health))
	group.GET("/actuator/info", gin.WrapF(p.Actuator.Info))

	requiredAuthV1 := group.Group("/api/v1")
	requiredAuthV1.Use(p.JWTMiddleware.AuthenticateJWT(), p.RoleMiddleware.RequireRole(string(enum.PTM_ADMIN), string(enum.PTM_USER)))
	{
		planV1 := requiredAuthV1.Group("/schedule-plans")
		{
			planV1.POST("", p.SchedulePlanController.GetOrCreateActivePlan)
			planV1.GET("/active", p.SchedulePlanController.GetActivePlan)
			planV1.GET("/active/detail", p.SchedulePlanController.GetActivePlanDetail)
			planV1.GET("/history", p.SchedulePlanController.GetPlanHistory)
			planV1.POST("/reschedule", p.SchedulePlanController.TriggerReschedule)
			planV1.GET("/:id", p.SchedulePlanController.GetPlanByID)
			planV1.GET("/:id/events", p.SchedulePlanController.GetPlanWithEvents)
			planV1.POST("/:id/apply", p.SchedulePlanController.ApplyProposedPlan)
			planV1.POST("/:id/revert", p.SchedulePlanController.RevertToPlan)
			planV1.DELETE("/:id", p.SchedulePlanController.DiscardProposedPlan)
		}

		availabilityV1 := requiredAuthV1.Group("/availability-calendar")
		{
			availabilityV1.GET("", p.AvailabilityCalendarController.GetAvailability)
			availabilityV1.POST("", p.AvailabilityCalendarController.SetAvailability)
			availabilityV1.PUT("", p.AvailabilityCalendarController.ReplaceAvailability)
		}

		scheduleTaskV1 := requiredAuthV1.Group("/schedule-tasks")
		{
			scheduleTaskV1.GET("", p.ScheduleTaskController.ListScheduleTasks)
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
			eventV1.POST("/:id/move", p.ScheduleEventController.ManuallyMoveEvent)
			eventV1.POST("/:id/complete", p.ScheduleEventController.CompleteEvent)
			eventV1.POST("/:id/split", p.ScheduleEventController.SplitEvent)
		}

		// exceptionV1 := requiredAuthV1.Group("/calendar-exceptions")
		// {
		// 	exceptionV1.GET("", p.CalendarExceptionController.ListExceptions)
		// 	exceptionV1.POST("", p.CalendarExceptionController.SaveExceptions)
		// 	exceptionV1.PUT("", p.CalendarExceptionController.ReplaceExceptions)
		// 	exceptionV1.DELETE("", p.CalendarExceptionController.DeleteExceptions)
		// }
	}

}
