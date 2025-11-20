/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	purchase "github.com/serp/api-gateway/src/ui/controller/purchase"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterPurchaseRoutes(group *gin.RouterGroup,
	productController *purchase.ProductController,
	addressController *purchase.AddressController,
	categoryController *purchase.CategoryController,
	facilityController *purchase.FacilityController,
	supplierController *purchase.SupplierController,
	orderController *purchase.OrderController,
	shipmentController *purchase.ShipmentController) {

	productV1 := group.Group("purchase-service/api/v1/product")
	{
		productV1.Use(middleware.AuthMiddleware()).POST("/create", productController.CreateProduct)
		productV1.Use(middleware.AuthMiddleware()).PATCH("/update/:productId", productController.UpdateProduct)
		productV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:productId", productController.DeleteProduct)
		productV1.Use(middleware.AuthMiddleware()).GET("/search/:productId", productController.GetProduct)
		productV1.Use(middleware.AuthMiddleware()).GET("/search", productController.GetProducts)
	}

	addressV1 := group.Group("purchase-service/api/v1/address")
	{
		addressV1.Use(middleware.AuthMiddleware()).POST("/create", addressController.CreateAddress)
		addressV1.Use(middleware.AuthMiddleware()).PATCH("/update/:addressId", addressController.UpdateAddress)
	}

	categoryV1 := group.Group("purchase-service/api/v1/category")
	{
		categoryV1.Use(middleware.AuthMiddleware()).POST("/create", categoryController.CreateCategory)
		categoryV1.Use(middleware.AuthMiddleware()).PATCH("/update/:categoryId", categoryController.UpdateCategory)
		categoryV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:categoryId", categoryController.DeleteCategory)
		categoryV1.Use(middleware.AuthMiddleware()).GET("/search/:categoryId", categoryController.GetCategory)
		categoryV1.Use(middleware.AuthMiddleware()).GET("/search", categoryController.GetCategories)
	}

	facilityV1 := group.Group("purchase-service/api/v1/facility")
	{
		facilityV1.Use(middleware.AuthMiddleware()).POST("/create", facilityController.CreateFacility)
		facilityV1.Use(middleware.AuthMiddleware()).PATCH("/update/:facilityId", facilityController.UpdateFacility)
		facilityV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:facilityId", facilityController.DeleteFacility)
		facilityV1.Use(middleware.AuthMiddleware()).GET("/search/:facilityId", facilityController.GetFacility)
		facilityV1.Use(middleware.AuthMiddleware()).GET("/search", facilityController.GetFacilities)
	}

	supplierV1 := group.Group("purchase-service/api/v1/supplier")
	{
		supplierV1.Use(middleware.AuthMiddleware()).POST("/create", supplierController.CreateSupplier)
		supplierV1.Use(middleware.AuthMiddleware()).PATCH("/update/:supplierId", supplierController.UpdateSupplier)
		supplierV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:supplierId", supplierController.DeleteSupplier)
		supplierV1.Use(middleware.AuthMiddleware()).GET("/search/:supplierId", supplierController.GetSupplier)
		supplierV1.Use(middleware.AuthMiddleware()).GET("/search", supplierController.GetSuppliers)
	}

	orderV1 := group.Group("purchase-service/api/v1/order")
	{
		orderV1.Use(middleware.AuthMiddleware()).POST("/create", orderController.CreateOrder)
		orderV1.Use(middleware.AuthMiddleware()).PATCH("/update/:orderId", orderController.UpdateOrder)
		orderV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:orderId", orderController.DeleteOrder)
		orderV1.Use(middleware.AuthMiddleware()).GET("/search/:orderId", orderController.GetOrder)
		orderV1.Use(middleware.AuthMiddleware()).GET("/search", orderController.GetOrders)
		// Order item operations
		orderV1.Use(middleware.AuthMiddleware()).POST("/create/:orderId/add", orderController.AddProductToOrder)
		orderV1.Use(middleware.AuthMiddleware()).PATCH("/update/:orderId/delete/:orderItemId", orderController.DeleteProductFromOrder)
		orderV1.Use(middleware.AuthMiddleware()).PATCH("/update/:orderId/update/:orderItemId", orderController.UpdateProductInOrder)

		// Order state operations
		orderV1.Use(middleware.AuthMiddleware()).PATCH("/manage/:orderId/approve", orderController.ApproveOrder)
		orderV1.Use(middleware.AuthMiddleware()).PATCH("/manage/:orderId/cancel", orderController.CancelOrder)
		orderV1.Use(middleware.AuthMiddleware()).PATCH("/update/:orderId/ready", orderController.MarkOrderAsReady)
	}

	shipmentV1 := group.Group("purchase-service/api/v1/shipment")
	{
		shipmentV1.Use(middleware.AuthMiddleware()).POST("/create", shipmentController.CreateShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/update/:shipmentId", shipmentController.UpdateShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:shipmentId", shipmentController.DeleteShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).GET("/search/:shipmentId", shipmentController.GetShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).GET("/search/by-order/:orderId", shipmentController.GetShipmentsByOrderId)

		// Shipment state operation
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/manage/:shipmentId/import", shipmentController.ImportShipment)

		// Shipment item operations
		shipmentV1.Use(middleware.AuthMiddleware()).POST("/create/:shipmentId/add", shipmentController.AddItemToShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/update/:shipmentId/delete/:itemId", shipmentController.DeleteItemFromShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/update/:shipmentId/update/:itemId", shipmentController.UpdateItemInShipment)

		// Shipment facility operation
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/update/:shipmentId/facility", shipmentController.UpdateShipmentFacility)
	}
}
