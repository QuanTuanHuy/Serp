# SupplierControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getDetailSupplier**](SupplierControllerApi.md#getDetailSupplier) | **GET** /logistics/api/v1/supplier/search/{supplierId} |  |
| [**getSuppliers**](SupplierControllerApi.md#getSuppliers) | **GET** /logistics/api/v1/supplier/search |  |


<a name="getDetailSupplier"></a>
# **getDetailSupplier**
> GeneralResponseSupplierEntity getDetailSupplier(supplierId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **supplierId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseSupplierEntity**](../Models/GeneralResponseSupplierEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getSuppliers"></a>
# **getSuppliers**
> GeneralResponsePageResponseSupplierEntity getSuppliers(page, size, sortBy, sortDirection, query, statusId)



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

[**GeneralResponsePageResponseSupplierEntity**](../Models/GeneralResponsePageResponseSupplierEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

