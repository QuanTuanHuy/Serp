/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/notification"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterNotificationRoutes(
	group *gin.RouterGroup,
	notificationProxyController *notification.NotificationProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
) {
	notificationGroup := group.Group("ws/notifications")
	// notificationGroup.Use(jwtMiddleware.AuthenticateJWT())
	{
		notificationGroup.GET("", notificationProxyController.ProxyWebSocket)
	}
}
