/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/serp/notification-service/src/kernel/properties"
	"github.com/serp/notification-service/src/ui/controller.go"
	"github.com/serp/notification-service/src/ui/middleware"
	"go.uber.org/zap"
)

type RouterConfig struct {
	AppProps       *properties.AppProperties
	Engine         *gin.Engine
	JWTMiddleware  *middleware.JWTMiddleware
	RoleMiddleware *middleware.RoleMiddleware
	Logger         *zap.Logger

	PreferenceController   *controller.PreferenceController
	NotificationController *controller.NotificationController
	WebSocketController    *controller.WebSocketController
}

func RegisterRoutes(config *RouterConfig) {
	config.Engine.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "UP",
			"service": config.AppProps.Name,
		})
	})

	apiV1 := config.Engine.Group(fmt.Sprintf("%s/api/v1", config.AppProps.Path))
	apiV1.Use(config.JWTMiddleware.AuthenticateJWT())
	{
		preferences := apiV1.Group("/preferences")
		{
			preferences.GET("", config.PreferenceController.GetPreferences)
			preferences.PATCH("", config.PreferenceController.UpdatePreferences)
		}

		notifications := apiV1.Group("/notifications")
		{
			notifications.POST("", config.NotificationController.CreateNotification)
			notifications.GET("", config.NotificationController.GetNotifications)
			notifications.GET("/:id", config.NotificationController.GetNotificationByID)
			notifications.PATCH("/:id", config.NotificationController.UpdateNotificationByID)
			notifications.PATCH("/read-all", config.NotificationController.MarkAllAsRead)
			notifications.DELETE("/:id", config.NotificationController.DeleteNotificationByID)
			notifications.GET("/unread-count", config.NotificationController.GetUnreadCount)
		}

	}

	websocket := config.Engine.Group(fmt.Sprintf("%s/ws", config.AppProps.Path))
	{
		websocket.GET("", config.WebSocketController.HandleWebSocket)
	}

	config.Logger.Info("Routes registered successfully")
}
