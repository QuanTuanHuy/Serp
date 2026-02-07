/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/common"
	"github.com/serp/api-gateway/src/ui/controller/notification"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterNotificationRoutes(
	group *gin.RouterGroup,
	notificationProxyController *notification.NotificationProxyController,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	notificationWSGroup := group.Group("ws/notifications")
	{
		notificationWSGroup.GET("", notificationProxyController.ProxyWebSocket)
	}

	notificationGroup := group.Group("/ns/api/v1")
	{
		notificationGroup.Use(
			jwtMiddleware.AuthenticateJWT(),
			rateLimitMiddleware.UserRateLimit(),
		).Any("/*proxyPath", genericProxyController.ProxyToNotification)
	}
}
