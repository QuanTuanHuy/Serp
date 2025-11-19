/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	service "github.com/serp/api-gateway/src/core/service/purchase"
	adapter "github.com/serp/api-gateway/src/infrastructure/client/purchase"
	controller "github.com/serp/api-gateway/src/ui/controller/purchase"
	"go.uber.org/fx"
)

func PurchaseModule() fx.Option {
	return fx.Module("purchase",
		// Provide adapter
		fx.Provide(adapter.NewProductClientAdapter),
		fx.Provide(adapter.NewAddressClientAdapter),
		fx.Provide(adapter.NewCategoryClientAdapter),
		fx.Provide(adapter.NewFacilityClientAdapter),
		fx.Provide(adapter.NewSupplierClientAdapter),
		fx.Provide(adapter.NewOrderClientAdapter),
		fx.Provide(adapter.NewShipmentClientAdapter),

		// Provide service
		fx.Provide(service.NewProductService),
		fx.Provide(service.NewAddressService),
		fx.Provide(service.NewCategoryService),
		fx.Provide(service.NewFacilityService),
		fx.Provide(service.NewSupplierService),
		fx.Provide(service.NewOrderService),
		fx.Provide(service.NewShipmentService),

		// Provide controller
		fx.Provide(controller.NewProductController),
		fx.Provide(controller.NewAddressController),
		fx.Provide(controller.NewCategoryController),
		fx.Provide(controller.NewFacilityController),
		fx.Provide(controller.NewSupplierController),
		fx.Provide(controller.NewOrderController),
		fx.Provide(controller.NewShipmentController),
	)
}
