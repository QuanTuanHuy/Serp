/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IAddressClientPort interface {
	CreateAddress(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateAddress(ctx context.Context, addressId string, req map[string]interface{}) (*response.BaseResponse, error)
	GetAddressesByEntityId(ctx context.Context, entityId string) (*response.BaseResponse, error)
}

type ICategoryClientPort interface {
	CreateCategory(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateCategory(ctx context.Context, categoryId string, req map[string]interface{}) (*response.BaseResponse, error)
	GetCategories(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
	GetCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error)
	DeleteCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error)
}

type ICustomerClientPort interface {
	GetCustomers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
}

type IFacilityClientPort interface {
	CreateFacility(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateFacility(ctx context.Context, facilityId string, req map[string]interface{}) (*response.BaseResponse, error)
	GetFacilities(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
	GetFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error)
	DeleteFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error)
}

type IInventoryItemClientPort interface {
	CreateInventoryItem(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateInventoryItem(ctx context.Context, inventoryItemId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error)
	GetInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error)
	GetInventoryItems(ctx context.Context, page, size int, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId string) (*response.BaseResponse, error)
}

type IOrderClientPort interface {
	GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	GetOrders(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter string) (*response.BaseResponse, error)
}

type IProductClientPort interface {
	CreateProduct(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateProduct(ctx context.Context, productId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProducts(ctx context.Context, page, size int, sortBy, sortDirection, query, categoryId, statusId string) (*response.BaseResponse, error)
}

type ISupplierClientPort interface {
	GetSuppliers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
	GetSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error)
}

type IShipmentClientPort interface {
	CreateShipment(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error)
	ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	AddItemToShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateItemInShipment(ctx context.Context, shipmentId, itemId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteItemFromShipment(ctx context.Context, shipmentId, itemId string) (*response.BaseResponse, error)
	GetShipmentDetail(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipments(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId string) (*response.BaseResponse, error)
}
