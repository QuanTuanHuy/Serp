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
	purchase "github.com/serp/api-gateway/src/ui/controller/purchase"
	"github.com/serp/api-gateway/src/ui/middleware"
	"go.uber.org/fx"
)

type RegisterRoutersIn struct {
	fx.In
	App      *golib.App
	Engine   *gin.Engine
	Actuator *actuator.Endpoint

	AuthController             *account.AuthController
	UserController             *account.UserController
	KeycloakController         *account.KeycloakController
	RoleController             *account.RoleController
	PermissionController       *account.PermissionController
	ModuleController           *account.ModuleController
	SubscriptionController     *account.SubscriptionController
	SubscriptionPlanController *account.SubscriptionPlanController
	ModuleAccessController     *account.ModuleAccessController
	MenuDisplayController      *account.MenuDisplayController
	OrganizationController     *account.OrganizationController
	DepartmentController       *account.DepartmentController

	ProjectController   *ptm.ProjectController
	GroupTaskController *ptm.GroupTaskController
	TaskController      *ptm.TaskController
	CommentController   *ptm.CommentController
	NoteController      *ptm.NoteController
	UserTagController   *ptm.UserTagController

	ProductController  *purchase.ProductController
	AddressController  *purchase.AddressController
	CategoryController *purchase.CategoryController
	FacilityController *purchase.FacilityController
	SupplierController *purchase.SupplierController
	OrderController    *purchase.OrderController
	ShipmentController *purchase.ShipmentController

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
		p.RoleController,
		p.PermissionController,
		p.ModuleController,
		p.SubscriptionController,
		p.SubscriptionPlanController,
		p.ModuleAccessController,
		p.MenuDisplayController,
		p.DepartmentController,
		p.OrganizationController,
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

	RegisterPurchaseRoutes(
		group,
		p.ProductController,
		p.AddressController,
		p.CategoryController,
		p.FacilityController,
		p.SupplierController,
		p.OrderController,
		p.ShipmentController,
	)
}
