# Documentation for Purchase Service API

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *http://localhost:8088*

| Class | Method | HTTP request | Description |
|------------ | ------------- | ------------- | -------------|
| *AddressControllerApi* | [**createAddress**](Apis/AddressControllerApi.md#createAddress) | **POST** /purchase-service/api/v1/address/create |  |
*AddressControllerApi* | [**getAddressesByEntityId**](Apis/AddressControllerApi.md#getAddressesByEntityId) | **GET** /purchase-service/api/v1/address/search/by-entity/{entityId} |  |
*AddressControllerApi* | [**updateAddress**](Apis/AddressControllerApi.md#updateAddress) | **PATCH** /purchase-service/api/v1/address/update/{addressId} |  |
| *CategoryControllerApi* | [**createCategory**](Apis/CategoryControllerApi.md#createCategory) | **POST** /purchase-service/api/v1/category/create |  |
*CategoryControllerApi* | [**deleteCategory**](Apis/CategoryControllerApi.md#deleteCategory) | **DELETE** /purchase-service/api/v1/category/delete/{categoryId} |  |
*CategoryControllerApi* | [**getCategories**](Apis/CategoryControllerApi.md#getCategories) | **GET** /purchase-service/api/v1/category/search |  |
*CategoryControllerApi* | [**getCategory**](Apis/CategoryControllerApi.md#getCategory) | **GET** /purchase-service/api/v1/category/search/{categoryId} |  |
*CategoryControllerApi* | [**updateCategory**](Apis/CategoryControllerApi.md#updateCategory) | **PATCH** /purchase-service/api/v1/category/update/{categoryId} |  |
| *FacilityControllerApi* | [**createFacility**](Apis/FacilityControllerApi.md#createFacility) | **POST** /purchase-service/api/v1/facility/create |  |
*FacilityControllerApi* | [**deleteFacility**](Apis/FacilityControllerApi.md#deleteFacility) | **DELETE** /purchase-service/api/v1/facility/delete/{facilityId} |  |
*FacilityControllerApi* | [**getFacilities**](Apis/FacilityControllerApi.md#getFacilities) | **GET** /purchase-service/api/v1/facility/search |  |
*FacilityControllerApi* | [**getFacilityDetail**](Apis/FacilityControllerApi.md#getFacilityDetail) | **GET** /purchase-service/api/v1/facility/search/{facilityId} |  |
*FacilityControllerApi* | [**updateFacility**](Apis/FacilityControllerApi.md#updateFacility) | **PATCH** /purchase-service/api/v1/facility/update/{facilityId} |  |
| *OrderControllerApi* | [**addProductToOrder**](Apis/OrderControllerApi.md#addProductToOrder) | **POST** /purchase-service/api/v1/order/create/{orderId}/add |  |
*OrderControllerApi* | [**approveOrder**](Apis/OrderControllerApi.md#approveOrder) | **PATCH** /purchase-service/api/v1/order/manage/{orderId}/approve |  |
*OrderControllerApi* | [**cancelOrder**](Apis/OrderControllerApi.md#cancelOrder) | **PATCH** /purchase-service/api/v1/order/manage/{orderId}/cancel |  |
*OrderControllerApi* | [**createOrder**](Apis/OrderControllerApi.md#createOrder) | **POST** /purchase-service/api/v1/order/create |  |
*OrderControllerApi* | [**deleteOrder**](Apis/OrderControllerApi.md#deleteOrder) | **DELETE** /purchase-service/api/v1/order/delete/{orderId} |  |
*OrderControllerApi* | [**deleteProductFromOrder**](Apis/OrderControllerApi.md#deleteProductFromOrder) | **PATCH** /purchase-service/api/v1/order/update/{orderId}/delete/{orderItemId} |  |
*OrderControllerApi* | [**getOrderDetail**](Apis/OrderControllerApi.md#getOrderDetail) | **GET** /purchase-service/api/v1/order/search/{orderId} |  |
*OrderControllerApi* | [**getOrders**](Apis/OrderControllerApi.md#getOrders) | **GET** /purchase-service/api/v1/order/search |  |
*OrderControllerApi* | [**updateOrder**](Apis/OrderControllerApi.md#updateOrder) | **PATCH** /purchase-service/api/v1/order/update/{orderId} |  |
*OrderControllerApi* | [**updateProductInOrder**](Apis/OrderControllerApi.md#updateProductInOrder) | **PATCH** /purchase-service/api/v1/order/update/{orderId}/update/{orderItemId} |  |
| *ProductControllerApi* | [**createProduct**](Apis/ProductControllerApi.md#createProduct) | **POST** /purchase-service/api/v1/product/create |  |
*ProductControllerApi* | [**deleteProduct**](Apis/ProductControllerApi.md#deleteProduct) | **DELETE** /purchase-service/api/v1/product/delete/{productId} |  |
*ProductControllerApi* | [**getProduct**](Apis/ProductControllerApi.md#getProduct) | **GET** /purchase-service/api/v1/product/search/{productId} |  |
*ProductControllerApi* | [**getProducts**](Apis/ProductControllerApi.md#getProducts) | **GET** /purchase-service/api/v1/product/search |  |
*ProductControllerApi* | [**updateProduct**](Apis/ProductControllerApi.md#updateProduct) | **PATCH** /purchase-service/api/v1/product/update/{productId} |  |
| *ShipmentControllerApi* | [**getShipmentDetail**](Apis/ShipmentControllerApi.md#getShipmentDetail) | **GET** /purchase-service/api/v1/shipment/search/{shipmentId} |  |
*ShipmentControllerApi* | [**getShipmentsByOrderId**](Apis/ShipmentControllerApi.md#getShipmentsByOrderId) | **GET** /purchase-service/api/v1/shipment/search/by-order/{orderId} |  |
| *SupplierControllerApi* | [**createSupplier**](Apis/SupplierControllerApi.md#createSupplier) | **POST** /purchase-service/api/v1/supplier/create |  |
*SupplierControllerApi* | [**deleteSupplier**](Apis/SupplierControllerApi.md#deleteSupplier) | **DELETE** /purchase-service/api/v1/supplier/delete/{supplierId} |  |
*SupplierControllerApi* | [**getDetailSupplier**](Apis/SupplierControllerApi.md#getDetailSupplier) | **GET** /purchase-service/api/v1/supplier/search/{supplierId} |  |
*SupplierControllerApi* | [**getSuppliers**](Apis/SupplierControllerApi.md#getSuppliers) | **GET** /purchase-service/api/v1/supplier/search |  |
*SupplierControllerApi* | [**updateSupplier**](Apis/SupplierControllerApi.md#updateSupplier) | **PATCH** /purchase-service/api/v1/supplier/update/{supplierId} |  |


<a name="documentation-for-models"></a>
## Documentation for Models

 - [AddressCreationForm](./Models/AddressCreationForm.md)
 - [AddressEntity](./Models/AddressEntity.md)
 - [AddressUpdateForm](./Models/AddressUpdateForm.md)
 - [CategoryEntity](./Models/CategoryEntity.md)
 - [CategoryForm](./Models/CategoryForm.md)
 - [FacilityCreationForm](./Models/FacilityCreationForm.md)
 - [FacilityEntity](./Models/FacilityEntity.md)
 - [FacilityUpdateForm](./Models/FacilityUpdateForm.md)
 - [GeneralResponseCategoryEntity](./Models/GeneralResponseCategoryEntity.md)
 - [GeneralResponseFacilityEntity](./Models/GeneralResponseFacilityEntity.md)
 - [GeneralResponseListAddressEntity](./Models/GeneralResponseListAddressEntity.md)
 - [GeneralResponseListShipmentEntity](./Models/GeneralResponseListShipmentEntity.md)
 - [GeneralResponseObject](./Models/GeneralResponseObject.md)
 - [GeneralResponseOrderEntity](./Models/GeneralResponseOrderEntity.md)
 - [GeneralResponsePageResponseCategoryEntity](./Models/GeneralResponsePageResponseCategoryEntity.md)
 - [GeneralResponsePageResponseFacilityEntity](./Models/GeneralResponsePageResponseFacilityEntity.md)
 - [GeneralResponsePageResponseOrderEntity](./Models/GeneralResponsePageResponseOrderEntity.md)
 - [GeneralResponsePageResponseProductEntity](./Models/GeneralResponsePageResponseProductEntity.md)
 - [GeneralResponsePageResponseSupplierEntity](./Models/GeneralResponsePageResponseSupplierEntity.md)
 - [GeneralResponseProductEntity](./Models/GeneralResponseProductEntity.md)
 - [GeneralResponseShipmentEntity](./Models/GeneralResponseShipmentEntity.md)
 - [GeneralResponseSupplierEntity](./Models/GeneralResponseSupplierEntity.md)
 - [InventoryItemDetailEntity](./Models/InventoryItemDetailEntity.md)
 - [OrderCancellationForm](./Models/OrderCancellationForm.md)
 - [OrderCreationForm](./Models/OrderCreationForm.md)
 - [OrderEntity](./Models/OrderEntity.md)
 - [OrderItem](./Models/OrderItem.md)
 - [OrderItemEntity](./Models/OrderItemEntity.md)
 - [OrderItemUpdateForm](./Models/OrderItemUpdateForm.md)
 - [OrderUpdateForm](./Models/OrderUpdateForm.md)
 - [PageResponseCategoryEntity](./Models/PageResponseCategoryEntity.md)
 - [PageResponseFacilityEntity](./Models/PageResponseFacilityEntity.md)
 - [PageResponseOrderEntity](./Models/PageResponseOrderEntity.md)
 - [PageResponseProductEntity](./Models/PageResponseProductEntity.md)
 - [PageResponseSupplierEntity](./Models/PageResponseSupplierEntity.md)
 - [ProductCreationForm](./Models/ProductCreationForm.md)
 - [ProductEntity](./Models/ProductEntity.md)
 - [ProductUpdateForm](./Models/ProductUpdateForm.md)
 - [ShipmentEntity](./Models/ShipmentEntity.md)
 - [SupplierCreationForm](./Models/SupplierCreationForm.md)
 - [SupplierEntity](./Models/SupplierEntity.md)
 - [SupplierUpdateForm](./Models/SupplierUpdateForm.md)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="Bearer Authentication"></a>
### Bearer Authentication

- **Type**: HTTP Bearer Token authentication (JWT)

