# Documentation for Logistics Module API

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *http://localhost:8089*

| Class | Method | HTTP request | Description |
|------------ | ------------- | ------------- | -------------|
| *AddressControllerApi* | [**createAddress**](Apis/AddressControllerApi.md#createAddress) | **POST** /logistics/api/v1/address/create |  |
*AddressControllerApi* | [**getAddressesByEntityId**](Apis/AddressControllerApi.md#getAddressesByEntityId) | **GET** /logistics/api/v1/address/search/by-entity/{entityId} |  |
*AddressControllerApi* | [**updateAddress**](Apis/AddressControllerApi.md#updateAddress) | **PATCH** /logistics/api/v1/address/update/{addressId} |  |
| *CategoryControllerApi* | [**createCategory**](Apis/CategoryControllerApi.md#createCategory) | **POST** /logistics/api/v1/category/create |  |
*CategoryControllerApi* | [**deleteCategory**](Apis/CategoryControllerApi.md#deleteCategory) | **DELETE** /logistics/api/v1/category/delete/{categoryId} |  |
*CategoryControllerApi* | [**getCategories**](Apis/CategoryControllerApi.md#getCategories) | **GET** /logistics/api/v1/category/search |  |
*CategoryControllerApi* | [**getCategory**](Apis/CategoryControllerApi.md#getCategory) | **GET** /logistics/api/v1/category/search/{categoryId} |  |
*CategoryControllerApi* | [**updateCategory**](Apis/CategoryControllerApi.md#updateCategory) | **PATCH** /logistics/api/v1/category/update/{categoryId} |  |
| *CustomerControllerApi* | [**getCustomers**](Apis/CustomerControllerApi.md#getCustomers) | **GET** /logistics/api/v1/customer/search |  |
*CustomerControllerApi* | [**getDetailCustomer**](Apis/CustomerControllerApi.md#getDetailCustomer) | **GET** /logistics/api/v1/customer/search/{customerId} |  |
| *FacilityControllerApi* | [**createFacility**](Apis/FacilityControllerApi.md#createFacility) | **POST** /logistics/api/v1/facility/create |  |
*FacilityControllerApi* | [**deleteFacility**](Apis/FacilityControllerApi.md#deleteFacility) | **DELETE** /logistics/api/v1/facility/delete/{facilityId} |  |
*FacilityControllerApi* | [**getFacilities**](Apis/FacilityControllerApi.md#getFacilities) | **GET** /logistics/api/v1/facility/search |  |
*FacilityControllerApi* | [**getFacilityDetail**](Apis/FacilityControllerApi.md#getFacilityDetail) | **GET** /logistics/api/v1/facility/search/{facilityId} |  |
*FacilityControllerApi* | [**updateFacility**](Apis/FacilityControllerApi.md#updateFacility) | **PATCH** /logistics/api/v1/facility/update/{facilityId} |  |
| *InventoryItemControllerApi* | [**createInventoryItem**](Apis/InventoryItemControllerApi.md#createInventoryItem) | **POST** /logistics/api/v1/inventory-item/create |  |
*InventoryItemControllerApi* | [**deleteInventoryItem**](Apis/InventoryItemControllerApi.md#deleteInventoryItem) | **DELETE** /logistics/api/v1/inventory-item/delete/{inventoryItemId} |  |
*InventoryItemControllerApi* | [**searchInventoryItem**](Apis/InventoryItemControllerApi.md#searchInventoryItem) | **GET** /logistics/api/v1/inventory-item/search/{inventoryItemId} |  |
*InventoryItemControllerApi* | [**searchInventoryItems**](Apis/InventoryItemControllerApi.md#searchInventoryItems) | **GET** /logistics/api/v1/inventory-item/search |  |
*InventoryItemControllerApi* | [**updateInventoryItem**](Apis/InventoryItemControllerApi.md#updateInventoryItem) | **PATCH** /logistics/api/v1/inventory-item/update/{inventoryItemId} |  |
| *OrderControllerApi* | [**getOrderDetail**](Apis/OrderControllerApi.md#getOrderDetail) | **GET** /logistics/api/v1/order/search/{orderId} |  |
*OrderControllerApi* | [**getOrders**](Apis/OrderControllerApi.md#getOrders) | **GET** /logistics/api/v1/order/search |  |
| *ProductControllerApi* | [**createProduct**](Apis/ProductControllerApi.md#createProduct) | **POST** /logistics/api/v1/product/create |  |
*ProductControllerApi* | [**deleteProduct**](Apis/ProductControllerApi.md#deleteProduct) | **DELETE** /logistics/api/v1/product/delete/{productId} |  |
*ProductControllerApi* | [**getProduct**](Apis/ProductControllerApi.md#getProduct) | **GET** /logistics/api/v1/product/search/{productId} |  |
*ProductControllerApi* | [**getProducts**](Apis/ProductControllerApi.md#getProducts) | **GET** /logistics/api/v1/product/search |  |
*ProductControllerApi* | [**updateProduct**](Apis/ProductControllerApi.md#updateProduct) | **PATCH** /logistics/api/v1/product/update/{productId} |  |
| *ShipmentControllerApi* | [**addItemToShipment**](Apis/ShipmentControllerApi.md#addItemToShipment) | **POST** /logistics/api/v1/shipment/create/{shipmentId}/add |  |
*ShipmentControllerApi* | [**createShipment**](Apis/ShipmentControllerApi.md#createShipment) | **POST** /logistics/api/v1/shipment/create |  |
*ShipmentControllerApi* | [**deleteItemFromShipment**](Apis/ShipmentControllerApi.md#deleteItemFromShipment) | **PATCH** /logistics/api/v1/shipment/update/{shipmentId}/delete/{itemId} |  |
*ShipmentControllerApi* | [**deleteShipment**](Apis/ShipmentControllerApi.md#deleteShipment) | **DELETE** /logistics/api/v1/shipment/delete/{shipmentId} |  |
*ShipmentControllerApi* | [**getShipmentDetail**](Apis/ShipmentControllerApi.md#getShipmentDetail) | **GET** /logistics/api/v1/shipment/search/{shipmentId} |  |
*ShipmentControllerApi* | [**getShipments**](Apis/ShipmentControllerApi.md#getShipments) | **GET** /logistics/api/v1/shipment/search |  |
*ShipmentControllerApi* | [**importShipment**](Apis/ShipmentControllerApi.md#importShipment) | **PATCH** /logistics/api/v1/shipment/manage/{shipmentId}/import |  |
*ShipmentControllerApi* | [**updateItemInShipment**](Apis/ShipmentControllerApi.md#updateItemInShipment) | **PATCH** /logistics/api/v1/shipment/update/{shipmentId}/update/{itemId} |  |
*ShipmentControllerApi* | [**updateShipment**](Apis/ShipmentControllerApi.md#updateShipment) | **PATCH** /logistics/api/v1/shipment/update/{shipmentId} |  |
| *SupplierControllerApi* | [**getDetailSupplier**](Apis/SupplierControllerApi.md#getDetailSupplier) | **GET** /logistics/api/v1/supplier/search/{supplierId} |  |
*SupplierControllerApi* | [**getSuppliers**](Apis/SupplierControllerApi.md#getSuppliers) | **GET** /logistics/api/v1/supplier/search |  |


<a name="documentation-for-models"></a>
## Documentation for Models

 - [AddressCreationForm](./Models/AddressCreationForm.md)
 - [AddressEntity](./Models/AddressEntity.md)
 - [AddressUpdateForm](./Models/AddressUpdateForm.md)
 - [CategoryEntity](./Models/CategoryEntity.md)
 - [CategoryForm](./Models/CategoryForm.md)
 - [CustomerEntity](./Models/CustomerEntity.md)
 - [FacilityCreationForm](./Models/FacilityCreationForm.md)
 - [FacilityEntity](./Models/FacilityEntity.md)
 - [FacilityUpdateForm](./Models/FacilityUpdateForm.md)
 - [GeneralResponseCategoryEntity](./Models/GeneralResponseCategoryEntity.md)
 - [GeneralResponseCustomerEntity](./Models/GeneralResponseCustomerEntity.md)
 - [GeneralResponseFacilityEntity](./Models/GeneralResponseFacilityEntity.md)
 - [GeneralResponseInventoryItemEntity](./Models/GeneralResponseInventoryItemEntity.md)
 - [GeneralResponseListAddressEntity](./Models/GeneralResponseListAddressEntity.md)
 - [GeneralResponseObject](./Models/GeneralResponseObject.md)
 - [GeneralResponseOrderEntity](./Models/GeneralResponseOrderEntity.md)
 - [GeneralResponsePageResponseCategoryEntity](./Models/GeneralResponsePageResponseCategoryEntity.md)
 - [GeneralResponsePageResponseCustomerEntity](./Models/GeneralResponsePageResponseCustomerEntity.md)
 - [GeneralResponsePageResponseFacilityEntity](./Models/GeneralResponsePageResponseFacilityEntity.md)
 - [GeneralResponsePageResponseInventoryItemEntity](./Models/GeneralResponsePageResponseInventoryItemEntity.md)
 - [GeneralResponsePageResponseOrderEntity](./Models/GeneralResponsePageResponseOrderEntity.md)
 - [GeneralResponsePageResponseProductEntity](./Models/GeneralResponsePageResponseProductEntity.md)
 - [GeneralResponsePageResponseShipmentEntity](./Models/GeneralResponsePageResponseShipmentEntity.md)
 - [GeneralResponsePageResponseSupplierEntity](./Models/GeneralResponsePageResponseSupplierEntity.md)
 - [GeneralResponseProductEntity](./Models/GeneralResponseProductEntity.md)
 - [GeneralResponseShipmentEntity](./Models/GeneralResponseShipmentEntity.md)
 - [GeneralResponseSupplierEntity](./Models/GeneralResponseSupplierEntity.md)
 - [InventoryItemCreationForm](./Models/InventoryItemCreationForm.md)
 - [InventoryItemDetail](./Models/InventoryItemDetail.md)
 - [InventoryItemDetailEntity](./Models/InventoryItemDetailEntity.md)
 - [InventoryItemDetailUpdateForm](./Models/InventoryItemDetailUpdateForm.md)
 - [InventoryItemEntity](./Models/InventoryItemEntity.md)
 - [InventoryItemUpdateForm](./Models/InventoryItemUpdateForm.md)
 - [OrderEntity](./Models/OrderEntity.md)
 - [OrderItemEntity](./Models/OrderItemEntity.md)
 - [PageResponseCategoryEntity](./Models/PageResponseCategoryEntity.md)
 - [PageResponseCustomerEntity](./Models/PageResponseCustomerEntity.md)
 - [PageResponseFacilityEntity](./Models/PageResponseFacilityEntity.md)
 - [PageResponseInventoryItemEntity](./Models/PageResponseInventoryItemEntity.md)
 - [PageResponseOrderEntity](./Models/PageResponseOrderEntity.md)
 - [PageResponseProductEntity](./Models/PageResponseProductEntity.md)
 - [PageResponseShipmentEntity](./Models/PageResponseShipmentEntity.md)
 - [PageResponseSupplierEntity](./Models/PageResponseSupplierEntity.md)
 - [ProductCreationForm](./Models/ProductCreationForm.md)
 - [ProductEntity](./Models/ProductEntity.md)
 - [ProductUpdateForm](./Models/ProductUpdateForm.md)
 - [ShipmentCreationForm](./Models/ShipmentCreationForm.md)
 - [ShipmentEntity](./Models/ShipmentEntity.md)
 - [ShipmentUpdateForm](./Models/ShipmentUpdateForm.md)
 - [SupplierEntity](./Models/SupplierEntity.md)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="Bearer Authentication"></a>
### Bearer Authentication

- **Type**: HTTP Bearer Token authentication (JWT)

