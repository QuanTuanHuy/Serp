package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/common"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterTtcrsRoutes(
	group *gin.RouterGroup,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	ttcrsGroup := group.Group("/ttcrs/api/v1")
	{
		ttcrsGroup.Use(
			rateLimitMiddleware.IPRateLimit(),
		).Any("/*proxyPath", genericProxyController.ProxyHandler("ttcrs"))
	}
}
