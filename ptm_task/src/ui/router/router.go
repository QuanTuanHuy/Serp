/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/kernel/properties"
	"github.com/serp/ptm-task/src/ui/controller"
	"github.com/serp/ptm-task/src/ui/middleware"
	"go.uber.org/zap"
)

type RouterConfig struct {
	AppProps          *properties.AppProperties
	Engine            *gin.Engine
	ProjectController *controller.ProjectController
	TaskController    *controller.TaskController
	NoteController    *controller.NoteController
	JWTMiddleware     *middleware.JWTMiddleware
	RoleMiddleware    *middleware.RoleMiddleware
	Logger            *zap.Logger
}

func RegisterRoutes(config *RouterConfig) {
	config.Engine.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "UP",
			"service": config.AppProps.Name,
		})
	})

	apiV1 := config.Engine.Group(fmt.Sprintf("%s/api/v1", config.AppProps.Path))
	apiV1.Use(config.JWTMiddleware.AuthenticateJWT(), config.RoleMiddleware.RequireRole(string(enum.RoleUser), string(enum.RoleAdmin)))
	{
		projects := apiV1.Group("/projects")
		{
			projects.POST("", config.ProjectController.CreateProject)
			projects.GET("", config.ProjectController.GetAllProjects)
			projects.GET("/:id", config.ProjectController.GetProjectByID)
			projects.GET("/:id/tasks", config.ProjectController.GetTasksByProjectID)
			projects.GET("/:id/notes", config.NoteController.GetNotesByProjectID)
			projects.PATCH("/:id", config.ProjectController.UpdateProject)
			projects.DELETE("/:id", config.ProjectController.DeleteProject)
		}

		tasks := apiV1.Group("/tasks")
		{
			tasks.POST("", config.TaskController.CreateTask)
			tasks.GET("", config.TaskController.GetTasksByUserID)
			tasks.GET("/:id", config.TaskController.GetTaskByID)
			tasks.GET("/:id/tree", config.TaskController.GetTaskTreeByTaskID)
			tasks.GET("/:id/notes", config.NoteController.GetNotesByTaskID)
			tasks.PATCH("/:id", config.TaskController.UpdateTask)
			tasks.DELETE("/:id", config.TaskController.DeleteTask)
		}

		notes := apiV1.Group("/notes")
		{
			notes.POST("", config.NoteController.CreateNote)
			notes.GET("/search", config.NoteController.SearchNotes)
			notes.GET("/:id", config.NoteController.GetNoteByID)
			notes.PATCH("/:id", config.NoteController.UpdateNote)
			notes.DELETE("/:id", config.NoteController.DeleteNote)
		}
	}

	config.Logger.Info("Routes registered successfully")
}
