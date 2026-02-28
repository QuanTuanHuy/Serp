/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/common"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterDiscussRoutes(
	group *gin.RouterGroup,
	wsProxyController *common.WebSocketProxyController,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	discussWSGroup := group.Group("ws/discuss")
	{
		discussWSGroup.GET("", wsProxyController.ProxyHandler("discuss"))
	}

	discussGroup := group.Group("/discuss/api/v1")
	{
		discussGroup.Use(
			jwtMiddleware.AuthenticateJWT(),
			rateLimitMiddleware.UserRateLimit(),
		).Any("/*proxyPath", genericProxyController.ProxyHandler("discuss"))
	}
}
