# ShipmentControllerApi

All URIs are relative to *http://localhost:8088*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getShipmentDetail**](ShipmentControllerApi.md#getShipmentDetail) | **GET** /purchase-service/api/v1/shipment/search/{shipmentId} |  |
| [**getShipmentsByOrderId**](ShipmentControllerApi.md#getShipmentsByOrderId) | **GET** /purchase-service/api/v1/shipment/search/by-order/{orderId} |  |


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

<a name="getShipmentsByOrderId"></a>
# **getShipmentsByOrderId**
> GeneralResponseListShipmentEntity getShipmentsByOrderId(orderId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseListShipmentEntity**](../Models/GeneralResponseListShipmentEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

