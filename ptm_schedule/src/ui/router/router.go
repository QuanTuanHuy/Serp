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

	SchedulePlanController  *controller.SchedulePlanController
	ScheduleGroupController *controller.ScheduleGroupController
	ScheduleTaskController  *controller.ScheduleTaskController

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

		scheduleTaskV1 := requiredAuthV1.Group("/schedule-tasks")
		{
			scheduleTaskV1.GET("/tasks", p.ScheduleTaskController.GetListTaskByUserID)
			scheduleTaskV1.GET("/batch-tasks", p.ScheduleTaskController.GetBatchTasks)
			scheduleTaskV1.POST("/choose-task-batch", p.ScheduleTaskController.ChooseTaskBatch)
			scheduleTaskV1.GET("/detail", p.ScheduleTaskController.GetScheduleTaskDetail)
		}
	}

}
