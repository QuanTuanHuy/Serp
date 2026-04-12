/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/common"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterFirstMileRoutes(
	group *gin.RouterGroup,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	firstMileGroup := group.Group("/first-mile/api/v1")
	{
		firstMileGroup.Use(rateLimitMiddleware.IPRateLimit())
		firstMileGroup.GET("/locations/provinces", genericProxyController.ProxyHandler("first-mile"))
		firstMileGroup.GET("/locations/provinces/:provinceCode/wards", genericProxyController.ProxyHandler("first-mile"))

		firstMileGroup.Use(
			jwtMiddleware.AuthenticateJWT(),
			rateLimitMiddleware.UserRateLimit(),
		).Any("/:resource", genericProxyController.ProxyHandler("first-mile"))

		firstMileGroup.Use(
			jwtMiddleware.AuthenticateJWT(),
			rateLimitMiddleware.UserRateLimit(),
		).Any("/:resource/*proxyPath", genericProxyController.ProxyHandler("first-mile"))
	}
}
