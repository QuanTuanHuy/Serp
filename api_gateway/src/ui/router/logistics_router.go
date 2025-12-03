/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	logistics "github.com/serp/api-gateway/src/ui/controller/logistics"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterLogisticsRoutes(group *gin.RouterGroup,
	addressController *logistics.AddressController,
	categoryController *logistics.CategoryController,
	customerController *logistics.CustomerController,
	facilityController *logistics.FacilityController,
	inventoryItemController *logistics.InventoryItemController,
	orderController *logistics.OrderController,
	productController *logistics.ProductController,
	supplierController *logistics.SupplierController,
	shipmentController *logistics.ShipmentController) {

	addressV1 := group.Group("/logistics/api/v1/address")
	{
		addressV1.Use(middleware.AuthMiddleware()).POST("/create", addressController.CreateAddress)
		addressV1.Use(middleware.AuthMiddleware()).PATCH("/update/:addressId", addressController.UpdateAddress)
		addressV1.Use(middleware.AuthMiddleware()).GET("/search/by-entity/:entityId", addressController.GetAddressesByEntityId)
	}

	categoryV1 := group.Group("/logistics/api/v1/category")
	{
		categoryV1.Use(middleware.AuthMiddleware()).POST("/create", categoryController.CreateCategory)
		categoryV1.Use(middleware.AuthMiddleware()).PATCH("/update/:categoryId", categoryController.UpdateCategory)
		categoryV1.Use(middleware.AuthMiddleware()).GET("/search", categoryController.GetCategories)
		categoryV1.Use(middleware.AuthMiddleware()).GET("/search/:categoryId", categoryController.GetCategory)
		categoryV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:categoryId", categoryController.DeleteCategory)
	}

	customerV1 := group.Group("/logistics/api/v1/customer")
	{
		customerV1.Use(middleware.AuthMiddleware()).GET("/search", customerController.GetCustomers)
	}

	facilityV1 := group.Group("/logistics/api/v1/facility")
	{
		facilityV1.Use(middleware.AuthMiddleware()).POST("/create", facilityController.CreateFacility)
		facilityV1.Use(middleware.AuthMiddleware()).PATCH("/update/:facilityId", facilityController.UpdateFacility)
		facilityV1.Use(middleware.AuthMiddleware()).GET("/search", facilityController.GetFacilities)
		facilityV1.Use(middleware.AuthMiddleware()).GET("/search/:facilityId", facilityController.GetFacility)
		facilityV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:facilityId", facilityController.DeleteFacility)
	}

	inventoryItemV1 := group.Group("/logistics/api/v1/inventory-item")
	{
		inventoryItemV1.Use(middleware.AuthMiddleware()).POST("/create", inventoryItemController.CreateInventoryItem)
		inventoryItemV1.Use(middleware.AuthMiddleware()).PATCH("/update/:inventoryItemId", inventoryItemController.UpdateInventoryItem)
		inventoryItemV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:inventoryItemId", inventoryItemController.DeleteInventoryItem)
		inventoryItemV1.Use(middleware.AuthMiddleware()).GET("/search/:inventoryItemId", inventoryItemController.GetInventoryItem)
		inventoryItemV1.Use(middleware.AuthMiddleware()).GET("/search", inventoryItemController.GetInventoryItems)
	}

	orderV1 := group.Group("/logistics/api/v1/order")
	{
		orderV1.Use(middleware.AuthMiddleware()).GET("/search/:orderId", orderController.GetOrder)
		orderV1.Use(middleware.AuthMiddleware()).GET("/search", orderController.GetOrders)
	}

	productV1 := group.Group("/logistics/api/v1/product")
	{
		productV1.Use(middleware.AuthMiddleware()).POST("/create", productController.CreateProduct)
		productV1.Use(middleware.AuthMiddleware()).PATCH("/update/:productId", productController.UpdateProduct)
		productV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:productId", productController.DeleteProduct)
		productV1.Use(middleware.AuthMiddleware()).GET("/search/:productId", productController.GetProduct)
		productV1.Use(middleware.AuthMiddleware()).GET("/search", productController.GetProducts)
	}

	supplierV1 := group.Group("/logistics/api/v1/supplier")
	{
		supplierV1.Use(middleware.AuthMiddleware()).GET("/search", supplierController.GetSuppliers)
		supplierV1.Use(middleware.AuthMiddleware()).GET("/search/:supplierId", supplierController.GetSupplier)
	}

	shipmentV1 := group.Group("/logistics/api/v1/shipment")
	{
		shipmentV1.Use(middleware.AuthMiddleware()).POST("/create", shipmentController.CreateShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/update/:shipmentId", shipmentController.UpdateShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/manage/:shipmentId/import", shipmentController.ImportShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).DELETE("/delete/:shipmentId", shipmentController.DeleteShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).POST("/create/:shipmentId/add", shipmentController.AddItemToShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/update/:shipmentId/update/:itemId", shipmentController.UpdateItemInShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).PATCH("/update/:shipmentId/delete/:itemId", shipmentController.DeleteItemFromShipment)
		shipmentV1.Use(middleware.AuthMiddleware()).GET("/search/:shipmentId", shipmentController.GetShipmentDetail)
		shipmentV1.Use(middleware.AuthMiddleware()).GET("/search", shipmentController.GetShipments)
	}
}
