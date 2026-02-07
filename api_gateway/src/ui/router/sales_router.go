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

func RegisterSalesRoutes(
	group *gin.RouterGroup,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	salesGroup := group.Group("/sales/api/v1")
	{
		salesGroup.Use(
			jwtMiddleware.AuthenticateJWT(),
			rateLimitMiddleware.UserRateLimit(),
		).Any("/*proxyPath", genericProxyController.ProxyToSales)
	}
}
