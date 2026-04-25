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

func RegisterCrmRoutes(
	group *gin.RouterGroup,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	crmProxyGroup := group.Group("/crm/api/v1")
	{
		crmProxyGroup.Use(
			jwtMiddleware.AuthenticateJWT(),
			rateLimitMiddleware.UserRateLimit(),
		).Any("/*proxyPath", genericProxyController.ProxyHandler("crm"))
	}
}
