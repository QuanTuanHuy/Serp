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

func RegisterPaymentRoutes(
	group *gin.RouterGroup,
	genericProxyController *common.GenericProxyController,
	jwtMiddleware *middleware.JWTMiddleware,
	rateLimitMiddleware *middleware.RateLimitMiddleware,
) {
	paymentV1 := group.Group("/payment/api/v1")
	{
		// Public endpoints for ZaloPay integration
		paymentV1.POST("/payments/zalopay/callback", genericProxyController.ProxyHandler("payment"))
		paymentV1.GET("/payments/zalopay/health", genericProxyController.ProxyHandler("payment"))
		paymentV1.GET("/payments/zalopay/banks", genericProxyController.ProxyHandler("payment"))

		// Authenticated endpoints
		protected := paymentV1.Use(jwtMiddleware.AuthenticateJWT())
		protected.Use(rateLimitMiddleware.UserRateLimit())
		{
			protected.POST("/payments/zalopay/create-order", genericProxyController.ProxyHandler("payment"))
			protected.POST("/payments/zalopay/query-order", genericProxyController.ProxyHandler("payment"))
			protected.POST("/payments/zalopay/refund", genericProxyController.ProxyHandler("payment"))
			protected.POST("/payments/zalopay/query-refund", genericProxyController.ProxyHandler("payment"))

			protected.GET("/transactions/my-history", genericProxyController.ProxyHandler("payment"))
			protected.GET("/transactions/admin/history", genericProxyController.ProxyHandler("payment"))
		}
	}
}

