# SupplierControllerApi

All URIs are relative to *http://localhost:8088*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createSupplier**](SupplierControllerApi.md#createSupplier) | **POST** /purchase-service/api/v1/supplier/create |  |
| [**deleteSupplier**](SupplierControllerApi.md#deleteSupplier) | **DELETE** /purchase-service/api/v1/supplier/delete/{supplierId} |  |
| [**getDetailSupplier**](SupplierControllerApi.md#getDetailSupplier) | **GET** /purchase-service/api/v1/supplier/search/{supplierId} |  |
| [**getSuppliers**](SupplierControllerApi.md#getSuppliers) | **GET** /purchase-service/api/v1/supplier/search |  |
| [**updateSupplier**](SupplierControllerApi.md#updateSupplier) | **PATCH** /purchase-service/api/v1/supplier/update/{supplierId} |  |


<a name="createSupplier"></a>
# **createSupplier**
> GeneralResponseObject createSupplier(SupplierCreationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **SupplierCreationForm** | [**SupplierCreationForm**](../Models/SupplierCreationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="deleteSupplier"></a>
# **deleteSupplier**
> GeneralResponseObject deleteSupplier(supplierId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **supplierId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

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

<a name="updateSupplier"></a>
# **updateSupplier**
> GeneralResponseObject updateSupplier(supplierId, SupplierUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **supplierId** | **String**|  | [default to null] |
| **SupplierUpdateForm** | [**SupplierUpdateForm**](../Models/SupplierUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

