/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/common"
	"github.com/serp/api-gateway/src/ui/controller/discuss"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterDiscussRoutes(
	group *gin.RouterGroup,
	discussProxyController *discuss.DiscussProxyController,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
) {
	discussWSGroup := group.Group("ws/discuss")
	{
		discussWSGroup.GET("", discussProxyController.ProxyWebSocket)
	}

	discussGroup := group.Group("/discuss/api/v1")
	{
		discussGroup.Use(jwtMiddleware.AuthenticateJWT()).Any("/*proxyPath", genericProxyController.ProxyToDiscuss)
	}
}
