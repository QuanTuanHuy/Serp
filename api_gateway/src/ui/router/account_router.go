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

func RegisterAccountRoutes(
	group *gin.RouterGroup,
	appPath string,
	genericProxyController *common.GenericProxyController,
	accountProxyJWTGateMiddleware *middleware.AccountProxyJWTGateMiddleware,
) {
	accountV1 := group.Group("/api/v1")
	{
		accountV1.Any(
			"/*proxyPath",
			accountProxyJWTGateMiddleware.Handler(appPath),
			genericProxyController.ProxyHandler("account"),
		)
	}
}
