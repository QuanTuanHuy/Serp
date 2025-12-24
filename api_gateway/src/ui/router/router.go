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
	"github.com/serp/api-gateway/src/ui/controller/common"
	crm "github.com/serp/api-gateway/src/ui/controller/crm"
	logistics "github.com/serp/api-gateway/src/ui/controller/logistics"
	notification "github.com/serp/api-gateway/src/ui/controller/notification"
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

	NotificationProxyController *notification.NotificationProxyController
	GenericProxyController      *common.GenericProxyController

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

	LeadController        *crm.LeadController
	OpportunityController *crm.OpportunityController
	CustomerController    *crm.CustomerController
	ContactController     *crm.ContactController

	ProjectController *ptm.ProjectController
	TaskController    *ptm.TaskController
	NoteController    *ptm.NoteController

	SchedulePlanController         *ptm.SchedulePlanController
	AvailabilityCalendarController *ptm.AvailabilityCalendarController
	ScheduleWindowController       *ptm.ScheduleWindowController
	ScheduleEventController        *ptm.ScheduleEventController

	ProductController  *purchase.ProductController
	AddressController  *purchase.AddressController
	CategoryController *purchase.CategoryController
	FacilityController *purchase.FacilityController
	SupplierController *purchase.SupplierController
	OrderController    *purchase.OrderController
	ShipmentController *purchase.ShipmentController

	LogisticsAddressController       *logistics.AddressController
	LogisticsCategoryController      *logistics.CategoryController
	LogisticsCustomerController      *logistics.CustomerController
	LogisticsFacilityController      *logistics.FacilityController
	LogisticsInventoryItemController *logistics.InventoryItemController
	LogisticsOrderController         *logistics.OrderController
	LogisticsProductController       *logistics.ProductController
	LogisticsSupplierController      *logistics.SupplierController
	LogisticsShipmentController      *logistics.ShipmentController

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

	RegisterCrmRoutes(
		group,
		p.LeadController,
		p.OpportunityController,
		p.CustomerController,
		p.ContactController,
		p.GenericProxyController,
	)

	RegisterPtmRoutes(
		group,
		p.ProjectController,
		p.TaskController,
		p.NoteController,
		p.SchedulePlanController,
		p.AvailabilityCalendarController,
		p.ScheduleWindowController,
		p.ScheduleEventController,
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

	RegisterLogisticsRoutes(
		group,
		p.LogisticsAddressController,
		p.LogisticsCategoryController,
		p.LogisticsCustomerController,
		p.LogisticsFacilityController,
		p.LogisticsInventoryItemController,
		p.LogisticsOrderController,
		p.LogisticsProductController,
		p.LogisticsSupplierController,
		p.LogisticsShipmentController,
	)

	RegisterNotificationRoutes(
		group,
		p.NotificationProxyController,
		p.GenericProxyController,
		p.JWTMiddleware,
	)
}
