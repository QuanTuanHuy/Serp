/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	service "github.com/serp/api-gateway/src/core/service/account"
	adapter "github.com/serp/api-gateway/src/infrastructure/client/account"
	controller "github.com/serp/api-gateway/src/ui/controller/account"
	"go.uber.org/fx"
)

func AccountModule() fx.Option {
	return fx.Module("account",
		// Provide adapter
		fx.Provide(adapter.NewAuthClientAdapter),
		fx.Provide(adapter.NewUserClientAdapter),
		fx.Provide(adapter.NewKeycloakClientAdapter),
		fx.Provide(adapter.NewRoleClientAdapter),
		fx.Provide(adapter.NewPermissionClientAdapter),
		fx.Provide(adapter.NewModuleClientAdapter),
		fx.Provide(adapter.NewSubscriptionClientAdapter),
		fx.Provide(adapter.NewSubscriptionPlanClientAdapter),
		fx.Provide(adapter.NewModuleAccessClientAdapter),
		fx.Provide(adapter.NewMenuDisplayClientAdapter),
		fx.Provide(adapter.NewOrganizationClientAdapter),
		fx.Provide(adapter.NewDepartmentClientAdapter),

		// Provide service
		fx.Provide(service.NewAuthService),
		fx.Provide(service.NewUserService),
		fx.Provide(service.NewKeycloakService),
		fx.Provide(service.NewRoleService),
		fx.Provide(service.NewPermissionService),
		fx.Provide(service.NewModuleService),
		fx.Provide(service.NewSubscriptionService),
		fx.Provide(service.NewSubscriptionPlanService),
		fx.Provide(service.NewModuleAccessService),
		fx.Provide(service.NewMenuDisplayService),
		fx.Provide(service.NewOrganizationService),
		fx.Provide(service.NewDepartmentService),

		// Provide controller
		fx.Provide(controller.NewAuthController),
		fx.Provide(controller.NewUserController),
		fx.Provide(controller.NewKeycloakController),
		fx.Provide(controller.NewRoleController),
		fx.Provide(controller.NewPermissionController),
		fx.Provide(controller.NewModuleController),
		fx.Provide(controller.NewSubscriptionController),
		fx.Provide(controller.NewSubscriptionPlanController),
		fx.Provide(controller.NewModuleAccessController),
		fx.Provide(controller.NewMenuDisplayController),
		fx.Provide(controller.NewOrganizationController),
		fx.Provide(controller.NewDepartmentController),
	)
}
