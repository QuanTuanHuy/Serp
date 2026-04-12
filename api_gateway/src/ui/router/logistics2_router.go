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

func RegisterLogistics2Routes(
	group *gin.RouterGroup,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	logistics2Group := group.Group("/logistics2/api/v1")
	{
		logistics2Group.Use(
			jwtMiddleware.AuthenticateJWT(),
			rateLimitMiddleware.UserRateLimit(),
		).Any("/*proxyPath", genericProxyController.ProxyHandler("logistics2"))
	}
}
