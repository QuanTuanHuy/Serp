/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib"
	"github.com/golibs-starter/golib/web/actuator"
	account "github.com/serp/api-gateway/src/ui/controller/account"
	ptm "github.com/serp/api-gateway/src/ui/controller/ptm"
	"github.com/serp/api-gateway/src/ui/middleware"
	"go.uber.org/fx"
)

type RegisterRoutersIn struct {
	fx.In
	App      *golib.App
	Engine   *gin.Engine
	Actuator *actuator.Endpoint

	AuthController     *account.AuthController
	UserController     *account.UserController
	KeycloakController *account.KeycloakController

	ProjectController   *ptm.ProjectController
	GroupTaskController *ptm.GroupTaskController
	TaskController      *ptm.TaskController
	CommentController   *ptm.CommentController
	NoteController      *ptm.NoteController
	UserTagController   *ptm.UserTagController

	JWTMiddleware  *middleware.JWTMiddleware
	CorsMiddleware *middleware.CorsMiddleware
}

func RegisterGinRouters(p RegisterRoutersIn) {
	p.Engine.Use(p.CorsMiddleware.Handler())

	group := p.Engine.Group(p.App.Path())

	group.GET("/actuator/health", gin.WrapF(p.Actuator.Health))
	group.GET("/actuator/info", gin.WrapF(p.Actuator.Info))

	RegisterAccountRoutes(
		group,
		p.AuthController,
		p.UserController,
		p.KeycloakController,
	)

	RegisterPtmRoutes(
		group,
		p.ProjectController,
		p.GroupTaskController,
		p.TaskController,
		p.CommentController,
		p.NoteController,
		p.UserTagController,
	)
}
