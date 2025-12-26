# Documentation for Sales Module API

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *http://localhost:8090*

| Class | Method | HTTP request | Description |
|------------ | ------------- | ------------- | -------------|
| *AddressControllerApi* | [**createAddress**](Apis/AddressControllerApi.md#createAddress) | **POST** /sales/api/v1/address/create |  |
*AddressControllerApi* | [**getAddressesByEntityId**](Apis/AddressControllerApi.md#getAddressesByEntityId) | **GET** /sales/api/v1/address/search/by-entity/{entityId} |  |
*AddressControllerApi* | [**updateAddress**](Apis/AddressControllerApi.md#updateAddress) | **PATCH** /sales/api/v1/address/update/{addressId} |  |
| *CategoryControllerApi* | [**createCategory**](Apis/CategoryControllerApi.md#createCategory) | **POST** /sales/api/v1/category/create |  |
*CategoryControllerApi* | [**deleteCategory**](Apis/CategoryControllerApi.md#deleteCategory) | **DELETE** /sales/api/v1/category/delete/{categoryId} |  |
*CategoryControllerApi* | [**getCategories**](Apis/CategoryControllerApi.md#getCategories) | **GET** /sales/api/v1/category/search |  |
*CategoryControllerApi* | [**getCategory**](Apis/CategoryControllerApi.md#getCategory) | **GET** /sales/api/v1/category/search/{categoryId} |  |
*CategoryControllerApi* | [**updateCategory**](Apis/CategoryControllerApi.md#updateCategory) | **PATCH** /sales/api/v1/category/update/{categoryId} |  |
| *CustomerControllerApi* | [**createCustomer**](Apis/CustomerControllerApi.md#createCustomer) | **POST** /sales/api/v1/customer/create |  |
*CustomerControllerApi* | [**deleteCustomer**](Apis/CustomerControllerApi.md#deleteCustomer) | **DELETE** /sales/api/v1/customer/delete/{customerId} |  |
*CustomerControllerApi* | [**getCustomers**](Apis/CustomerControllerApi.md#getCustomers) | **GET** /sales/api/v1/customer/search |  |
*CustomerControllerApi* | [**getDetailCustomer**](Apis/CustomerControllerApi.md#getDetailCustomer) | **GET** /sales/api/v1/customer/search/{customerId} |  |
*CustomerControllerApi* | [**updateCustomer**](Apis/CustomerControllerApi.md#updateCustomer) | **PATCH** /sales/api/v1/customer/update/{customerId} |  |
| *FacilityControllerApi* | [**createFacility**](Apis/FacilityControllerApi.md#createFacility) | **POST** /sales/api/v1/facility/create |  |
*FacilityControllerApi* | [**deleteFacility**](Apis/FacilityControllerApi.md#deleteFacility) | **DELETE** /sales/api/v1/facility/delete/{facilityId} |  |
*FacilityControllerApi* | [**getFacilities**](Apis/FacilityControllerApi.md#getFacilities) | **GET** /sales/api/v1/facility/search |  |
*FacilityControllerApi* | [**getFacilityDetail**](Apis/FacilityControllerApi.md#getFacilityDetail) | **GET** /sales/api/v1/facility/search/{facilityId} |  |
*FacilityControllerApi* | [**updateFacility**](Apis/FacilityControllerApi.md#updateFacility) | **PATCH** /sales/api/v1/facility/update/{facilityId} |  |
| *InventoryItemControllerApi* | [**createInventoryItem**](Apis/InventoryItemControllerApi.md#createInventoryItem) | **POST** /sales/api/v1/inventory-item/create |  |
*InventoryItemControllerApi* | [**deleteInventoryItem**](Apis/InventoryItemControllerApi.md#deleteInventoryItem) | **DELETE** /sales/api/v1/inventory-item/delete/{inventoryItemId} |  |
*InventoryItemControllerApi* | [**searchInventoryItem**](Apis/InventoryItemControllerApi.md#searchInventoryItem) | **GET** /sales/api/v1/inventory-item/search/{inventoryItemId} |  |
*InventoryItemControllerApi* | [**searchInventoryItems**](Apis/InventoryItemControllerApi.md#searchInventoryItems) | **GET** /sales/api/v1/inventory-item/search |  |
*InventoryItemControllerApi* | [**updateInventoryItem**](Apis/InventoryItemControllerApi.md#updateInventoryItem) | **PATCH** /sales/api/v1/inventory-item/update/{inventoryItemId} |  |
| *OrderControllerApi* | [**addProductToOrder**](Apis/OrderControllerApi.md#addProductToOrder) | **POST** /sales/api/v1/order/create/{orderId}/add |  |
*OrderControllerApi* | [**approveOrder**](Apis/OrderControllerApi.md#approveOrder) | **PATCH** /sales/api/v1/order/manage/{orderId}/approve |  |
*OrderControllerApi* | [**cancelOrder**](Apis/OrderControllerApi.md#cancelOrder) | **PATCH** /sales/api/v1/order/manage/{orderId}/cancel |  |
*OrderControllerApi* | [**createOrder**](Apis/OrderControllerApi.md#createOrder) | **POST** /sales/api/v1/order/create |  |
*OrderControllerApi* | [**deleteOrder**](Apis/OrderControllerApi.md#deleteOrder) | **DELETE** /sales/api/v1/order/delete/{orderId} |  |
*OrderControllerApi* | [**deleteProductFromOrder**](Apis/OrderControllerApi.md#deleteProductFromOrder) | **PATCH** /sales/api/v1/order/update/{orderId}/delete/{orderItemId} |  |
*OrderControllerApi* | [**getOrderDetail**](Apis/OrderControllerApi.md#getOrderDetail) | **GET** /sales/api/v1/order/search/{orderId} |  |
*OrderControllerApi* | [**getOrders**](Apis/OrderControllerApi.md#getOrders) | **GET** /sales/api/v1/order/search |  |
*OrderControllerApi* | [**updateOrder**](Apis/OrderControllerApi.md#updateOrder) | **PATCH** /sales/api/v1/order/update/{orderId} |  |
| *ProductControllerApi* | [**createProduct**](Apis/ProductControllerApi.md#createProduct) | **POST** /sales/api/v1/product/create |  |
*ProductControllerApi* | [**deleteProduct**](Apis/ProductControllerApi.md#deleteProduct) | **DELETE** /sales/api/v1/product/delete/{productId} |  |
*ProductControllerApi* | [**getProduct**](Apis/ProductControllerApi.md#getProduct) | **GET** /sales/api/v1/product/search/{productId} |  |
*ProductControllerApi* | [**getProducts**](Apis/ProductControllerApi.md#getProducts) | **GET** /sales/api/v1/product/search |  |
*ProductControllerApi* | [**updateProduct**](Apis/ProductControllerApi.md#updateProduct) | **PATCH** /sales/api/v1/product/update/{productId} |  |


<a name="documentation-for-models"></a>
## Documentation for Models

 - [AddressCreationForm](./Models/AddressCreationForm.md)
 - [AddressEntity](./Models/AddressEntity.md)
 - [AddressUpdateForm](./Models/AddressUpdateForm.md)
 - [CategoryEntity](./Models/CategoryEntity.md)
 - [CategoryForm](./Models/CategoryForm.md)
 - [CustomerCreationForm](./Models/CustomerCreationForm.md)
 - [CustomerEntity](./Models/CustomerEntity.md)
 - [CustomerUpdateForm](./Models/CustomerUpdateForm.md)
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
 - [GeneralResponseProductEntity](./Models/GeneralResponseProductEntity.md)
 - [InventoryItemCreationForm](./Models/InventoryItemCreationForm.md)
 - [InventoryItemDetailEntity](./Models/InventoryItemDetailEntity.md)
 - [InventoryItemEntity](./Models/InventoryItemEntity.md)
 - [InventoryItemUpdateForm](./Models/InventoryItemUpdateForm.md)
 - [OrderCancellationForm](./Models/OrderCancellationForm.md)
 - [OrderCreationForm](./Models/OrderCreationForm.md)
 - [OrderEntity](./Models/OrderEntity.md)
 - [OrderItem](./Models/OrderItem.md)
 - [OrderItemEntity](./Models/OrderItemEntity.md)
 - [OrderUpdateForm](./Models/OrderUpdateForm.md)
 - [PageResponseCategoryEntity](./Models/PageResponseCategoryEntity.md)
 - [PageResponseCustomerEntity](./Models/PageResponseCustomerEntity.md)
 - [PageResponseFacilityEntity](./Models/PageResponseFacilityEntity.md)
 - [PageResponseInventoryItemEntity](./Models/PageResponseInventoryItemEntity.md)
 - [PageResponseOrderEntity](./Models/PageResponseOrderEntity.md)
 - [PageResponseProductEntity](./Models/PageResponseProductEntity.md)
 - [ProductCreationForm](./Models/ProductCreationForm.md)
 - [ProductEntity](./Models/ProductEntity.md)
 - [ProductUpdateForm](./Models/ProductUpdateForm.md)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="Bearer Authentication"></a>
### Bearer Authentication

- **Type**: HTTP Bearer Token authentication (JWT)

