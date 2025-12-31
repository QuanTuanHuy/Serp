# AddressControllerApi

All URIs are relative to *http://localhost:8089*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createAddress**](AddressControllerApi.md#createAddress) | **POST** /logistics/api/v1/address/create |  |
| [**getAddressesByEntityId**](AddressControllerApi.md#getAddressesByEntityId) | **GET** /logistics/api/v1/address/search/by-entity/{entityId} |  |
| [**updateAddress**](AddressControllerApi.md#updateAddress) | **PATCH** /logistics/api/v1/address/update/{addressId} |  |


<a name="createAddress"></a>
# **createAddress**
> GeneralResponseObject createAddress(AddressCreationForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **AddressCreationForm** | [**AddressCreationForm**](../Models/AddressCreationForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

<a name="getAddressesByEntityId"></a>
# **getAddressesByEntityId**
> GeneralResponseListAddressEntity getAddressesByEntityId(entityId)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **entityId** | **String**|  | [default to null] |

### Return type

[**GeneralResponseListAddressEntity**](../Models/GeneralResponseListAddressEntity.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

<a name="updateAddress"></a>
# **updateAddress**
> GeneralResponseObject updateAddress(addressId, AddressUpdateForm)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **addressId** | **String**|  | [default to null] |
| **AddressUpdateForm** | [**AddressUpdateForm**](../Models/AddressUpdateForm.md)|  | |

### Return type

[**GeneralResponseObject**](../Models/GeneralResponseObject.md)

### Authorization

[Bearer Authentication](../README.md#Bearer Authentication)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

