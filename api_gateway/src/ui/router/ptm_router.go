/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	controller "github.com/serp/api-gateway/src/ui/controller/ptm"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterPtmRoutes(
	group *gin.RouterGroup,
	projectController *controller.ProjectController,
	taskController *controller.TaskController,
	noteController *controller.NoteController,
	schedulePlanController *controller.SchedulePlanController,
	availabilityCalendarController *controller.AvailabilityCalendarController,
	scheduleWindowController *controller.ScheduleWindowController,
	scheduleEventController *controller.ScheduleEventController,
	scheduleTaskC *controller.ScheduleTaskController,
) {
	ptmV1 := group.Group("/ptm/api/v1")

	ptmV1.Use(middleware.AuthMiddleware())
	{
		// PTM Task - Projects
		projectV1 := ptmV1.Group("/projects")
		{
			projectV1.POST("", projectController.CreateProject)
			projectV1.GET("", projectController.GetAllProjects)
			projectV1.GET("/:id", projectController.GetProjectByID)
			projectV1.GET("/:id/tasks", projectController.GetTasksByProjectID)
			projectV1.GET("/:id/notes", noteController.GetNotesByProjectID)
			projectV1.PATCH("/:id", projectController.UpdateProject)
			projectV1.DELETE("/:id", projectController.DeleteProject)
		}

		// PTM Task - Tasks
		taskV1 := ptmV1.Group("/tasks")
		{
			taskV1.POST("", taskController.CreateTask)
			taskV1.GET("", taskController.GetTasksByUserID)
			taskV1.GET("/:id", taskController.GetTaskByID)
			taskV1.GET("/:id/tree", taskController.GetTaskTreeByTaskID)
			taskV1.GET("/:id/notes", noteController.GetNotesByTaskID)
			taskV1.PATCH("/:id", taskController.UpdateTask)
			taskV1.DELETE("/:id", taskController.DeleteTask)
		}

		// PTM Task - Notes
		noteV1 := ptmV1.Group("/notes")
		{
			noteV1.POST("", noteController.CreateNote)
			noteV1.GET("/search", noteController.SearchNotes)
			noteV1.GET("/:id", noteController.GetNoteByID)
			noteV1.PATCH("/:id", noteController.UpdateNote)
			noteV1.DELETE("/:id", noteController.DeleteNote)
		}

		// PTM Schedule - Schedule Plans
		schedulePlanV1 := ptmV1.Group("/schedule-plans")
		{
			schedulePlanV1.POST("", schedulePlanController.GetOrCreateActivePlan)
			schedulePlanV1.GET("/active", schedulePlanController.GetActivePlan)
			schedulePlanV1.GET("/active/detail", schedulePlanController.GetActivePlanDetail)
			schedulePlanV1.GET("/history", schedulePlanController.GetPlanHistory)
			schedulePlanV1.POST("/reschedule", schedulePlanController.TriggerReschedule)
			schedulePlanV1.GET("/:id", schedulePlanController.GetPlanByID)
			schedulePlanV1.GET("/:id/events", schedulePlanController.GetPlanWithEvents)
			schedulePlanV1.POST("/:id/apply", schedulePlanController.ApplyProposedPlan)
			schedulePlanV1.POST("/:id/revert", schedulePlanController.RevertToPlan)
			schedulePlanV1.DELETE("/:id", schedulePlanController.DiscardProposedPlan)
		}

		// PTM Schedule - Schedule Tasks
		scheduleTaskV1 := ptmV1.Group("/schedule-tasks")
		{
			scheduleTaskV1.GET("", scheduleTaskC.GetTasksByPlanID)
		}

		// PTM Schedule - Availability Calendar
		availabilityCalendarV1 := ptmV1.Group("/availability-calendar")
		{
			availabilityCalendarV1.GET("", availabilityCalendarController.GetAvailability)
			availabilityCalendarV1.POST("", availabilityCalendarController.SetAvailability)
			availabilityCalendarV1.PUT("", availabilityCalendarController.ReplaceAvailability)
		}

		// PTM Schedule - Schedule Windows
		scheduleWindowV1 := ptmV1.Group("/schedule-windows")
		{
			scheduleWindowV1.GET("", scheduleWindowController.ListAvailabilityWindows)
			scheduleWindowV1.POST("/materialize", scheduleWindowController.MaterializeWindows)
		}

		// PTM Schedule - Schedule Events
		scheduleEventV1 := ptmV1.Group("/schedule-events")
		{
			scheduleEventV1.GET("", scheduleEventController.ListEvents)
			scheduleEventV1.POST("", scheduleEventController.SaveEvents)
			scheduleEventV1.POST("/:id/move", scheduleEventController.ManuallyMoveEvent)
			scheduleEventV1.POST("/:id/complete", scheduleEventController.CompleteEvent)
			scheduleEventV1.POST("/:id/split", scheduleEventController.SplitEvent)
		}
	}
}
