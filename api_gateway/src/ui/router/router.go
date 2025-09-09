/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib"
	"github.com/golibs-starter/golib/web/actuator"
	"github.com/serp/api-gateway/src/kernel/properties"
	account "github.com/serp/api-gateway/src/ui/controller/account"
	"github.com/serp/api-gateway/src/ui/middleware"
	"go.uber.org/fx"
)

type RegisterRoutersIn struct {
	fx.In
	App      *golib.App
	Engine   *gin.Engine
	Actuator *actuator.Endpoint

	AuthController *account.AuthController
	UserController *account.UserController

	JWTMiddleware *middleware.JWTMiddleware
}

func RegisterGinRouters(p RegisterRoutersIn) {
	corsMiddleware := middleware.NewCorsMiddleware(properties.NewDefaultCorsProperties())
	p.Engine.Use(corsMiddleware.Handler())

	group := p.Engine.Group(p.App.Path())

	group.GET("/actuator/health", gin.WrapF(p.Actuator.Health))
	group.GET("/actuator/info", gin.WrapF(p.Actuator.Info))

	if p.AuthController != nil {
		RegisterAccountRoutes(group, p.AuthController, p.UserController)
	}

	// test
	group.Use(p.JWTMiddleware.AuthenticateJWT()).GET("/actuator/test", gin.WrapF(p.Actuator.Health))

}
