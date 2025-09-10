/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib"
	"github.com/golibs-starter/golib/web/actuator"
	"github.com/serp/ptm-task/src/ui/controller"
	"github.com/serp/ptm-task/src/ui/middleware"
	"go.uber.org/fx"
)

type RegisterRoutersIn struct {
	fx.In
	App      *golib.App
	Engine   *gin.Engine
	Actuator *actuator.Endpoint

	ProjectController   *controller.ProjectController
	GroupTaskController *controller.GroupTaskController
	TaskController      *controller.TaskController
	CommentController   *controller.CommentController
	NoteController      *controller.NoteController

	JWTMiddleware *middleware.JWTMiddleware
}

func RegisterGinRouters(p RegisterRoutersIn) {
	group := p.Engine.Group(p.App.Path())

	group.GET("/actuator/health", gin.WrapF(p.Actuator.Health))
	group.GET("/actuator/info", gin.WrapF(p.Actuator.Info))

	requiredAuthV1 := group.Group("/api/v1")
	requiredAuthV1.Use(p.JWTMiddleware.AuthenticateJWT())
	{
		projectV1 := requiredAuthV1.Group("/projects")
		{
			projectV1.POST("", p.ProjectController.CreateProject)
			projectV1.GET("/all", p.ProjectController.GetProjectsByUserID)
			projectV1.GET("/:id", p.ProjectController.GetProjectByID)
			projectV1.PUT("/:id", p.ProjectController.UpdateProject)
			projectV1.PUT("/:id/archive", p.ProjectController.ArchiveProject)
			projectV1.GET("/:id/group-tasks", p.ProjectController.GetGroupTasksByProjectID)
			projectV1.GET("", p.ProjectController.GetProjects)
			projectV1.GET("/search", p.ProjectController.GetProjectsByName)
		}

		groupTaskV1 := requiredAuthV1.Group("/group-tasks")
		{
			groupTaskV1.POST("", p.GroupTaskController.CreateGroupTask)
			groupTaskV1.GET("/:id", p.GroupTaskController.GetGroupTaskByID)
		}

		taskV1 := requiredAuthV1.Group("/tasks")
		{
			taskV1.POST("", p.TaskController.CreateTask)
			taskV1.GET("/:id", p.TaskController.GetTaskByID)
			taskV1.PUT("/:id", p.TaskController.UpdateTask)
			taskV1.DELETE("/:id", p.TaskController.DeleteTask)
			taskV1.GET("/:id/comments", p.TaskController.GetCommentsByTaskID)
		}

		commentV1 := requiredAuthV1.Group("/comments")
		{
			commentV1.POST("", p.CommentController.CreateComment)
			commentV1.GET("/:id", p.CommentController.GetCommentByID)
			commentV1.PUT("/:id", p.CommentController.UpdateComment)
			commentV1.DELETE("/:id", p.CommentController.DeleteComment)
		}

		noteV1 := requiredAuthV1.Group("/notes")
		{
			noteV1.POST("", p.NoteController.CreateNote)
			noteV1.GET("", p.NoteController.GetAllNotes)
			noteV1.GET("/:id", p.NoteController.GetNoteByID)
			noteV1.DELETE("/:id", p.NoteController.DeleteNote)
			noteV1.PUT("/:id/lock", p.NoteController.LockNote)
			noteV1.PUT("/:id/unlock", p.NoteController.UnlockNote)
		}
	}
}
