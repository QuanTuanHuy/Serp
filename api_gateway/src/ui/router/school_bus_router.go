/*
Author: Codex
Description: Part of Serp Project - School bus routes
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/common"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterSchoolBusRoutes(
	group *gin.RouterGroup,
	wsProxyController *common.WebSocketProxyController,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	schoolBusWSGroup := group.Group("ws/school-bus")
	{
		schoolBusWSGroup.GET("", wsProxyController.ProxyHandler("school-bus"))
	}

	schoolBusGroup := group.Group("/school-bus/api/v1")
	{
		schoolBusGroup.Use(
			jwtMiddleware.AuthenticateJWT(),
			rateLimitMiddleware.UserRateLimit(),
		).Any("/*proxyPath", genericProxyController.ProxyHandler("school-bus"))
	}
}
