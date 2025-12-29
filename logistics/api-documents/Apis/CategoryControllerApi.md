# CategoryControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createCategory**](CategoryControllerApi.md#createCategory) | **POST** /logistics/api/v1/category/create |  |
| [**deleteCategory**](CategoryControllerApi.md#deleteCategory) | **DELETE** /logistics/api/v1/category/delete/{categoryId} |  |
| [**getCategories**](CategoryControllerApi.md#getCategories) | **GET** /logistics/api/v1/category/search |  |
| [**getCategory**](CategoryControllerApi.md#getCategory) | **GET** /logistics/api/v1/category/search/{categoryId} |  |
| [**updateCategory**](CategoryControllerApi.md#updateCategory) | **PATCH** /logistics/api/v1/category/update/{categoryId} |  |


<a name="createCategory"></a>
# **createCategory**
> GeneralResponseObject createCategory(CategoryForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **CategoryForm** | [**CategoryForm**](../Models/CategoryForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="deleteCategory"></a>
# **deleteCategory**
> GeneralResponseObject deleteCategory(categoryId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **categoryId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getCategories"></a>
# **getCategories**
> GeneralResponsePageResponseCategoryEntity getCategories(page, size, sortBy, sortDirection, query, statusId)



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

[**GeneralResponsePageResponseCategoryEntity**](../Models/GeneralResponsePageResponseCategoryEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getCategory"></a>
# **getCategory**
> GeneralResponseCategoryEntity getCategory(categoryId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **categoryId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseCategoryEntity**](../Models/GeneralResponseCategoryEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="updateCategory"></a>
# **updateCategory**
> GeneralResponseObject updateCategory(categoryId, CategoryForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **categoryId** | **String**|  | [default to null] |
| **CategoryForm** | [**CategoryForm**](../Models/CategoryForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

