# InventoryItemControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createInventoryItem**](InventoryItemControllerApi.md#createInventoryItem) | **POST** /logistics/api/v1/inventory-item/create |  |
| [**deleteInventoryItem**](InventoryItemControllerApi.md#deleteInventoryItem) | **DELETE** /logistics/api/v1/inventory-item/delete/{inventoryItemId} |  |
| [**searchInventoryItem**](InventoryItemControllerApi.md#searchInventoryItem) | **GET** /logistics/api/v1/inventory-item/search/{inventoryItemId} |  |
| [**searchInventoryItems**](InventoryItemControllerApi.md#searchInventoryItems) | **GET** /logistics/api/v1/inventory-item/search |  |
| [**updateInventoryItem**](InventoryItemControllerApi.md#updateInventoryItem) | **PATCH** /logistics/api/v1/inventory-item/update/{inventoryItemId} |  |


<a name="createInventoryItem"></a>
# **createInventoryItem**
> GeneralResponseObject createInventoryItem(InventoryItemCreationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **InventoryItemCreationForm** | [**InventoryItemCreationForm**](../Models/InventoryItemCreationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="deleteInventoryItem"></a>
# **deleteInventoryItem**
> GeneralResponseObject deleteInventoryItem(inventoryItemId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **inventoryItemId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="searchInventoryItem"></a>
# **searchInventoryItem**
> GeneralResponseInventoryItemEntity searchInventoryItem(inventoryItemId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **inventoryItemId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseInventoryItemEntity**](../Models/GeneralResponseInventoryItemEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="searchInventoryItems"></a>
# **searchInventoryItems**
> GeneralResponsePageResponseInventoryItemEntity searchInventoryItems(page, size, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sortBy** | **String**|  | [optional] [default to createdStamp] |
| **sortDirection** | **String**|  | [optional] [default to desc] |
| **query** | **String**|  | [optional] [default to null] |
| **productId** | **String**|  | [optional] [default to null] |
| **facilityId** | **String**|  | [optional] [default to null] |
| **expirationDateFrom** | **date**|  | [optional] [default to null] |
| **expirationDateTo** | **date**|  | [optional] [default to null] |
| **manufacturingDateFrom** | **date**|  | [optional] [default to null] |
| **manufacturingDateTo** | **date**|  | [optional] [default to null] |
| **statusId** | **String**|  | [optional] [default to null] |

### Return type

[**GeneralResponsePageResponseInventoryItemEntity**](../Models/GeneralResponsePageResponseInventoryItemEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="updateInventoryItem"></a>
# **updateInventoryItem**
> GeneralResponseObject updateInventoryItem(inventoryItemId, InventoryItemUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **inventoryItemId** | **String**|  | [default to null] |
| **InventoryItemUpdateForm** | [**InventoryItemUpdateForm**](../Models/InventoryItemUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

