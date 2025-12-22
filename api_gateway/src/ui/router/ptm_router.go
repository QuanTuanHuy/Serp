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
	noteController *controller.NoteController) {
	ptmV1 := group.Group("/ptm/api/v1")

	ptmV1.Use(middleware.AuthMiddleware())
	{
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

		taskV1 := ptmV1.Group("/tasks")
		{
			taskV1.POST("", taskController.CreateTask)
			taskV1.GET("", taskController.GetTasksByUserID)
			taskV1.GET("/:id", taskController.GetTaskByID)
			taskV1.GET("/:id/notes", noteController.GetNotesByTaskID)
			taskV1.PATCH("/:id", taskController.UpdateTask)
			taskV1.DELETE("/:id", taskController.DeleteTask)
		}

		noteV1 := ptmV1.Group("/notes")
		{
			noteV1.POST("", noteController.CreateNote)
			noteV1.GET("/search", noteController.SearchNotes)
			noteV1.GET("/:id", noteController.GetNoteByID)
			noteV1.PATCH("/:id", noteController.UpdateNote)
			noteV1.DELETE("/:id", noteController.DeleteNote)
		}
	}
}
