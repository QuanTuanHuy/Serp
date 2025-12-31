# OrderControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getOrderDetail**](OrderControllerApi.md#getOrderDetail) | **GET** /logistics/api/v1/order/search/{orderId} |  |
| [**getOrders**](OrderControllerApi.md#getOrders) | **GET** /logistics/api/v1/order/search |  |


<a name="getOrderDetail"></a>
# **getOrderDetail**
> GeneralResponseOrderEntity getOrderDetail(orderId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseOrderEntity**](../Models/GeneralResponseOrderEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getOrders"></a>
# **getOrders**
> GeneralResponsePageResponseOrderEntity getOrders(page, size, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sortBy** | **String**|  | [optional] [default to createdStamp] |
| **sortDirection** | **String**|  | [optional] [default to desc] |
| **query** | **String**|  | [optional] [default to null] |
| **statusId** | **String**|  | [optional] [default to null] |
| **orderTypeId** | **String**|  | [optional] [default to null] |
| **toCustomerId** | **String**|  | [optional] [default to null] |
| **fromSupplierId** | **String**|  | [optional] [default to null] |
| **saleChannelId** | **String**|  | [optional] [default to null] |
| **orderDateAfter** | **date**|  | [optional] [default to null] |
| **orderDateBefore** | **date**|  | [optional] [default to null] |
| **deliveryBefore** | **date**|  | [optional] [default to null] |
| **deliveryAfter** | **date**|  | [optional] [default to null] |

### Return type

[**GeneralResponsePageResponseOrderEntity**](../Models/GeneralResponsePageResponseOrderEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

