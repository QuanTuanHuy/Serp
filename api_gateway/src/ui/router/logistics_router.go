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

func RegisterLogisticsRoutes(
	group *gin.RouterGroup,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
) {
	logisticsGroup := group.Group("/logistics/api/v1")
	{
		logisticsGroup.Use(jwtMiddleware.AuthenticateJWT()).Any("/*proxyPath", genericProxyController.ProxyToLogistics)
	}
}
