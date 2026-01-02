# OrderControllerApi

All URIs are relative to *http://localhost:8088*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addProductToOrder**](OrderControllerApi.md#addProductToOrder) | **POST** /purchase-service/api/v1/order/create/{orderId}/add |  |
| [**approveOrder**](OrderControllerApi.md#approveOrder) | **PATCH** /purchase-service/api/v1/order/manage/{orderId}/approve |  |
| [**cancelOrder**](OrderControllerApi.md#cancelOrder) | **PATCH** /purchase-service/api/v1/order/manage/{orderId}/cancel |  |
| [**createOrder**](OrderControllerApi.md#createOrder) | **POST** /purchase-service/api/v1/order/create |  |
| [**deleteOrder**](OrderControllerApi.md#deleteOrder) | **DELETE** /purchase-service/api/v1/order/delete/{orderId} |  |
| [**deleteProductFromOrder**](OrderControllerApi.md#deleteProductFromOrder) | **PATCH** /purchase-service/api/v1/order/update/{orderId}/delete/{orderItemId} |  |
| [**getOrderDetail**](OrderControllerApi.md#getOrderDetail) | **GET** /purchase-service/api/v1/order/search/{orderId} |  |
| [**getOrders**](OrderControllerApi.md#getOrders) | **GET** /purchase-service/api/v1/order/search |  |
| [**updateOrder**](OrderControllerApi.md#updateOrder) | **PATCH** /purchase-service/api/v1/order/update/{orderId} |  |
| [**updateProductInOrder**](OrderControllerApi.md#updateProductInOrder) | **PATCH** /purchase-service/api/v1/order/update/{orderId}/update/{orderItemId} |  |


<a name="addProductToOrder"></a>
# **addProductToOrder**
> GeneralResponseObject addProductToOrder(orderId, OrderItem)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |
| **OrderItem** | [**OrderItem**](../Models/OrderItem.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="approveOrder"></a>
# **approveOrder**
> GeneralResponseObject approveOrder(orderId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="cancelOrder"></a>
# **cancelOrder**
> GeneralResponseObject cancelOrder(orderId, OrderCancellationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |
| **OrderCancellationForm** | [**OrderCancellationForm**](../Models/OrderCancellationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="createOrder"></a>
# **createOrder**
> GeneralResponseObject createOrder(OrderCreationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **OrderCreationForm** | [**OrderCreationForm**](../Models/OrderCreationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="deleteOrder"></a>
# **deleteOrder**
> GeneralResponseObject deleteOrder(orderId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="deleteProductFromOrder"></a>
# **deleteProductFromOrder**
> GeneralResponseObject deleteProductFromOrder(orderId, orderItemId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |
| **orderItemId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

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
> GeneralResponsePageResponseOrderEntity getOrders(page, size, sortBy, sortDirection, query, statusId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sortBy** | **String**|  | [optional] [default to createdStamp] |
| **sortDirection** | **String**|  | [optional] [default to desc] |
| **query** | **String**|  | [optional] [default to null] |
| **statusId** | **String**|  | [optional] [default to null] |
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

<a name="updateOrder"></a>
# **updateOrder**
> GeneralResponseObject updateOrder(orderId, OrderUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |
| **OrderUpdateForm** | [**OrderUpdateForm**](../Models/OrderUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="updateProductInOrder"></a>
# **updateProductInOrder**
> GeneralResponseObject updateProductInOrder(orderId, orderItemId, OrderItemUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **orderId** | **String**|  | [default to null] |
| **orderItemId** | **String**|  | [default to null] |
| **OrderItemUpdateForm** | [**OrderItemUpdateForm**](../Models/OrderItemUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

