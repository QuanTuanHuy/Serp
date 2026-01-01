# ShipmentControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addItemToShipment**](ShipmentControllerApi.md#addItemToShipment) | **POST** /logistics/api/v1/shipment/create/{shipmentId}/add |  |
| [**createShipment**](ShipmentControllerApi.md#createShipment) | **POST** /logistics/api/v1/shipment/create |  |
| [**deleteItemFromShipment**](ShipmentControllerApi.md#deleteItemFromShipment) | **PATCH** /logistics/api/v1/shipment/update/{shipmentId}/delete/{itemId} |  |
| [**deleteShipment**](ShipmentControllerApi.md#deleteShipment) | **DELETE** /logistics/api/v1/shipment/delete/{shipmentId} |  |
| [**getShipmentDetail**](ShipmentControllerApi.md#getShipmentDetail) | **GET** /logistics/api/v1/shipment/search/{shipmentId} |  |
| [**getShipments**](ShipmentControllerApi.md#getShipments) | **GET** /logistics/api/v1/shipment/search |  |
| [**importShipment**](ShipmentControllerApi.md#importShipment) | **PATCH** /logistics/api/v1/shipment/manage/{shipmentId}/import |  |
| [**updateItemInShipment**](ShipmentControllerApi.md#updateItemInShipment) | **PATCH** /logistics/api/v1/shipment/update/{shipmentId}/update/{itemId} |  |
| [**updateShipment**](ShipmentControllerApi.md#updateShipment) | **PATCH** /logistics/api/v1/shipment/update/{shipmentId} |  |


<a name="addItemToShipment"></a>
# **addItemToShipment**
> GeneralResponseObject addItemToShipment(shipmentId, InventoryItemDetail)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **shipmentId** | **String**|  | [default to null] |
| **InventoryItemDetail** | [**InventoryItemDetail**](../Models/InventoryItemDetail.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="createShipment"></a>
# **createShipment**
> GeneralResponseObject createShipment(ShipmentCreationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **ShipmentCreationForm** | [**ShipmentCreationForm**](../Models/ShipmentCreationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="deleteItemFromShipment"></a>
# **deleteItemFromShipment**
> GeneralResponseObject deleteItemFromShipment(shipmentId, itemId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **shipmentId** | **String**|  | [default to null] |
| **itemId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="deleteShipment"></a>
# **deleteShipment**
> GeneralResponseObject deleteShipment(shipmentId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **shipmentId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getShipmentDetail"></a>
# **getShipmentDetail**
> GeneralResponseShipmentEntity getShipmentDetail(shipmentId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **shipmentId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseShipmentEntity**](../Models/GeneralResponseShipmentEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getShipments"></a>
# **getShipments**
> GeneralResponsePageResponseShipmentEntity getShipments(page, size, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sortBy** | **String**|  | [optional] [default to createdStamp] |
| **sortDirection** | **String**|  | [optional] [default to desc] |
| **query** | **String**|  | [optional] [default to null] |
| **statusId** | **String**|  | [optional] [default to null] |
| **shipmentTypeId** | **String**|  | [optional] [default to null] |
| **toCustomerId** | **String**|  | [optional] [default to null] |
| **fromSupplierId** | **String**|  | [optional] [default to null] |
| **orderId** | **String**|  | [optional] [default to null] |

### Return type

[**GeneralResponsePageResponseShipmentEntity**](../Models/GeneralResponsePageResponseShipmentEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="importShipment"></a>
# **importShipment**
> GeneralResponseObject importShipment(shipmentId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **shipmentId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="updateItemInShipment"></a>
# **updateItemInShipment**
> GeneralResponseObject updateItemInShipment(shipmentId, itemId, InventoryItemDetailUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **shipmentId** | **String**|  | [default to null] |
| **itemId** | **String**|  | [default to null] |
| **InventoryItemDetailUpdateForm** | [**InventoryItemDetailUpdateForm**](../Models/InventoryItemDetailUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="updateShipment"></a>
# **updateShipment**
> GeneralResponseObject updateShipment(shipmentId, ShipmentUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **shipmentId** | **String**|  | [default to null] |
| **ShipmentUpdateForm** | [**ShipmentUpdateForm**](../Models/ShipmentUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

