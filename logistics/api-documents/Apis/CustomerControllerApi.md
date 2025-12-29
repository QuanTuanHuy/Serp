# CustomerControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getCustomers**](CustomerControllerApi.md#getCustomers) | **GET** /logistics/api/v1/customer/search |  |
| [**getDetailCustomer**](CustomerControllerApi.md#getDetailCustomer) | **GET** /logistics/api/v1/customer/search/{customerId} |  |


<a name="getCustomers"></a>
# **getCustomers**
> GeneralResponsePageResponseCustomerEntity getCustomers(page, size, sortBy, sortDirection, query, statusId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sortBy** | **String**|  | [optional] [default to createdStamp] |
| **sortDirection** | **String**|  | [optional] [default to desc] |
| **query** | **String**|  | [optional] [default to null] |
| **statusId** | **String**|  | [optional] [default to null] |

### Return type

[**GeneralResponsePageResponseCustomerEntity**](../Models/GeneralResponsePageResponseCustomerEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getDetailCustomer"></a>
# **getDetailCustomer**
> GeneralResponseCustomerEntity getDetailCustomer(customerId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **customerId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseCustomerEntity**](../Models/GeneralResponseCustomerEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

