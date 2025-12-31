# ProductControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createProduct**](ProductControllerApi.md#createProduct) | **POST** /logistics/api/v1/product/create |  |
| [**deleteProduct**](ProductControllerApi.md#deleteProduct) | **DELETE** /logistics/api/v1/product/delete/{productId} |  |
| [**getProduct**](ProductControllerApi.md#getProduct) | **GET** /logistics/api/v1/product/search/{productId} |  |
| [**getProducts**](ProductControllerApi.md#getProducts) | **GET** /logistics/api/v1/product/search |  |
| [**updateProduct**](ProductControllerApi.md#updateProduct) | **PATCH** /logistics/api/v1/product/update/{productId} |  |


<a name="createProduct"></a>
# **createProduct**
> GeneralResponseObject createProduct(ProductCreationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **ProductCreationForm** | [**ProductCreationForm**](../Models/ProductCreationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="deleteProduct"></a>
# **deleteProduct**
> GeneralResponseObject deleteProduct(productId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **productId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getProduct"></a>
# **getProduct**
> GeneralResponseProductEntity getProduct(productId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **productId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseProductEntity**](../Models/GeneralResponseProductEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getProducts"></a>
# **getProducts**
> GeneralResponsePageResponseProductEntity getProducts(page, size, sortBy, sortDirection, query, categoryId, statusId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sortBy** | **String**|  | [optional] [default to createdStamp] |
| **sortDirection** | **String**|  | [optional] [default to desc] |
| **query** | **String**|  | [optional] [default to null] |
| **categoryId** | **String**|  | [optional] [default to null] |
| **statusId** | **String**|  | [optional] [default to null] |

### Return type

[**GeneralResponsePageResponseProductEntity**](../Models/GeneralResponsePageResponseProductEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="updateProduct"></a>
# **updateProduct**
> GeneralResponseObject updateProduct(productId, ProductUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **productId** | **String**|  | [default to null] |
| **ProductUpdateForm** | [**ProductUpdateForm**](../Models/ProductUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

