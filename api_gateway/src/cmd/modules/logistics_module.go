/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	service "github.com/serp/api-gateway/src/core/service/logistics"
	adapter "github.com/serp/api-gateway/src/infrastructure/client/logistics"
	controller "github.com/serp/api-gateway/src/ui/controller/logistics"
	"go.uber.org/fx"
)

func LogisticsModule() fx.Option {
	return fx.Module("logistics",
		// Provide adapter
		fx.Provide(adapter.NewAddressClientAdapter),
		fx.Provide(adapter.NewCategoryClientAdapter),
		fx.Provide(adapter.NewCustomerClientAdapter),
		fx.Provide(adapter.NewFacilityClientAdapter),
		fx.Provide(adapter.NewInventoryItemClientAdapter),
		fx.Provide(adapter.NewOrderClientAdapter),
		fx.Provide(adapter.NewProductClientAdapter),
		fx.Provide(adapter.NewSupplierClientAdapter),
		fx.Provide(adapter.NewShipmentClientAdapter),

		// Provide service
		fx.Provide(service.NewAddressService),
		fx.Provide(service.NewCategoryService),
		fx.Provide(service.NewCustomerService),
		fx.Provide(service.NewFacilityService),
		fx.Provide(service.NewInventoryItemService),
		fx.Provide(service.NewOrderService),
		fx.Provide(service.NewProductService),
		fx.Provide(service.NewSupplierService),
		fx.Provide(service.NewShipmentService),

		// Provide controller
		fx.Provide(controller.NewAddressController),
		fx.Provide(controller.NewCategoryController),
		fx.Provide(controller.NewCustomerController),
		fx.Provide(controller.NewFacilityController),
		fx.Provide(controller.NewInventoryItemController),
		fx.Provide(controller.NewOrderController),
		fx.Provide(controller.NewProductController),
		fx.Provide(controller.NewSupplierController),
		fx.Provide(controller.NewShipmentController),
	)
}
