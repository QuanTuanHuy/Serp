# FacilityControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createFacility**](FacilityControllerApi.md#createFacility) | **POST** /logistics/api/v1/facility/create |  |
| [**deleteFacility**](FacilityControllerApi.md#deleteFacility) | **DELETE** /logistics/api/v1/facility/delete/{facilityId} |  |
| [**getFacilities**](FacilityControllerApi.md#getFacilities) | **GET** /logistics/api/v1/facility/search |  |
| [**getFacilityDetail**](FacilityControllerApi.md#getFacilityDetail) | **GET** /logistics/api/v1/facility/search/{facilityId} |  |
| [**updateFacility**](FacilityControllerApi.md#updateFacility) | **PATCH** /logistics/api/v1/facility/update/{facilityId} |  |


<a name="createFacility"></a>
# **createFacility**
> GeneralResponseObject createFacility(FacilityCreationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **FacilityCreationForm** | [**FacilityCreationForm**](../Models/FacilityCreationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="deleteFacility"></a>
# **deleteFacility**
> GeneralResponseObject deleteFacility(facilityId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **facilityId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getFacilities"></a>
# **getFacilities**
> GeneralResponsePageResponseFacilityEntity getFacilities(page, size, sortBy, sortDirection, query, statusId)



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

[**GeneralResponsePageResponseFacilityEntity**](../Models/GeneralResponsePageResponseFacilityEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="getFacilityDetail"></a>
# **getFacilityDetail**
> GeneralResponseFacilityEntity getFacilityDetail(facilityId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **facilityId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseFacilityEntity**](../Models/GeneralResponseFacilityEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="updateFacility"></a>
# **updateFacility**
> GeneralResponseObject updateFacility(facilityId, FacilityUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **facilityId** | **String**|  | [default to null] |
| **FacilityUpdateForm** | [**FacilityUpdateForm**](../Models/FacilityUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

