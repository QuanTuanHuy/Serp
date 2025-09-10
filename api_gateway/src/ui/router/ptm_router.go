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

func RegisterPtmRoutes(group *gin.RouterGroup, projectController *controller.ProjectController, groupTaskController *controller.GroupTaskController, taskController *controller.TaskController, commentController *controller.CommentController, noteController *controller.NoteController) {
	ptmV1 := group.Group("/ptm/api/v1")

	ptmV1.Use(middleware.AuthMiddleware())
	{
		projectV1 := ptmV1.Group("/projects")
		{
			projectV1.POST("", projectController.CreateProject)
			projectV1.GET("/all", projectController.GetProjectsByUserID)
			projectV1.GET("/:id", projectController.GetProjectByID)
			projectV1.PUT("/:id", projectController.UpdateProject)
			projectV1.PUT("/:id/archive", projectController.ArchiveProject)
			projectV1.GET("/:id/group-tasks", projectController.GetGroupTasksByProjectID)
			projectV1.GET("", projectController.GetProjects)
			projectV1.GET("/search", projectController.GetProjectsByName)
		}

		groupTaskV1 := ptmV1.Group("/group-tasks")
		{
			groupTaskV1.POST("", groupTaskController.CreateGroupTask)
			groupTaskV1.GET("/:id", groupTaskController.GetGroupTaskByID)
		}

		taskV1 := ptmV1.Group("/tasks")
		{
			taskV1.POST("", taskController.CreateTask)
			taskV1.GET("/:id", taskController.GetTaskByID)
			taskV1.PUT("/:id", taskController.UpdateTask)
			taskV1.DELETE("/:id", taskController.DeleteTask)
			taskV1.GET("/:id/comments", taskController.GetCommentsByTaskID)
		}

		commentV1 := ptmV1.Group("/comments")
		{
			commentV1.POST("", commentController.CreateComment)
			commentV1.GET("/:id", commentController.GetCommentByID)
			commentV1.PUT("/:id", commentController.UpdateComment)
			commentV1.DELETE("/:id", commentController.DeleteComment)
		}

		noteV1 := ptmV1.Group("/notes")
		{
			noteV1.POST("", noteController.CreateNote)
			noteV1.GET("", noteController.GetAllNotes)
			noteV1.GET("/:id", noteController.GetNoteByID)
			noteV1.DELETE("/:id", noteController.DeleteNote)
			noteV1.PUT("/:id/lock", noteController.LockNote)
			noteV1.PUT("/:id/unlock", noteController.UnlockNote)
		}
	}
}
