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

func RegisterPurchaseRoutes(
	group *gin.RouterGroup,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
) {
	purchaseGroup := group.Group("/purchase-service/api/v1")
	{
		purchaseGroup.Use(jwtMiddleware.AuthenticateJWT()).Any("/*proxyPath", genericProxyController.ProxyToPurchase)
	}
}
