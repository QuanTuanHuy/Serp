# CustomerControllerApi

All URIs are relative to *http://localhost:8090*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createCustomer**](CustomerControllerApi.md#createCustomer) | **POST** /sales/api/v1/customer/create |  |
| [**deleteCustomer**](CustomerControllerApi.md#deleteCustomer) | **DELETE** /sales/api/v1/customer/delete/{customerId} |  |
| [**getCustomers**](CustomerControllerApi.md#getCustomers) | **GET** /sales/api/v1/customer/search |  |
| [**getDetailCustomer**](CustomerControllerApi.md#getDetailCustomer) | **GET** /sales/api/v1/customer/search/{customerId} |  |
| [**updateCustomer**](CustomerControllerApi.md#updateCustomer) | **PATCH** /sales/api/v1/customer/update/{customerId} |  |


<a name="createCustomer"></a>
# **createCustomer**
> GeneralResponseObject createCustomer(CustomerCreationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **CustomerCreationForm** | [**CustomerCreationForm**](../Models/CustomerCreationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="deleteCustomer"></a>
# **deleteCustomer**
> GeneralResponseObject deleteCustomer(customerId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **customerId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

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

<a name="updateCustomer"></a>
# **updateCustomer**
> GeneralResponseObject updateCustomer(customerId, CustomerUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **customerId** | **String**|  | [default to null] |
| **CustomerUpdateForm** | [**CustomerUpdateForm**](../Models/CustomerUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

