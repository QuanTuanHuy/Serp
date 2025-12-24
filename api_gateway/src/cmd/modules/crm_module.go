/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	service "github.com/serp/api-gateway/src/core/service/crm"
	adapter "github.com/serp/api-gateway/src/infrastructure/client/crm"
	controller "github.com/serp/api-gateway/src/ui/controller/crm"
	"go.uber.org/fx"
)

func CrmModule() fx.Option {
	return fx.Module("crm",
		// Provide adapter
		fx.Provide(adapter.NewLeadClientAdapter),
		fx.Provide(adapter.NewOpportunityClientAdapter),
		fx.Provide(adapter.NewCustomerClientAdapter),
		fx.Provide(adapter.NewContactClientAdapter),

		// Provide service
		fx.Provide(service.NewLeadService),
		fx.Provide(service.NewOpportunityService),
		fx.Provide(service.NewCustomerService),
		fx.Provide(service.NewContactService),

		// Provide controller
		fx.Provide(controller.NewLeadController),
		fx.Provide(controller.NewOpportunityController),
		fx.Provide(controller.NewCustomerController),
		fx.Provide(controller.NewContactController),
	)
}
