/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics API Endpoints
*/

import { api } from '@/lib/store/api';
import type {
  Address,
  AddressCreationForm,
  AddressUpdateForm,
  Category,
  CategoryForm,
  CategoryFilters,
  Customer,
  CustomerFilters,
  Facility,
  FacilityCreationForm,
  FacilityUpdateForm,
  FacilityFilters,
  InventoryItem,
  InventoryItemDetail,
  InventoryItemCreationForm,
  InventoryItemUpdateForm,
  InventoryItemFilters,
  Order,
  OrderFilters,
  Product,
  ProductCreationForm,
  ProductUpdateForm,
  ProductFilters,
  Shipment,
  ShipmentCreationForm,
  ShipmentUpdateForm,
  ShipmentFilters,
  ShipmentItemForm,
  Supplier,
  SupplierFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
} from '../types';

// Define Logistics API slice extending the main API
export const logisticsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Address endpoints
    createAddress: builder.mutation<APIResponse<Address>, AddressCreationForm>(
      {
        query: (data) => ({
          url: '/address/create',
          method: 'POST',
          body: data,
        }),
        extraOptions: { service: 'logistics' },
        invalidatesTags: [{ type: 'logistics/Address', id: 'LIST' }],
      }
    ),

    getAddressesByEntity: builder.query<
      APIResponse<Address[]>,
      { entityId: string }
    >({
      query: ({ entityId }) => ({
        url: `/address/search/by-entity/${entityId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.map(({ id }) => ({
                type: 'logistics/Address' as const,
                id,
              })),
              { type: 'logistics/Address', id: 'LIST' },
            ]
          : [{ type: 'logistics/Address', id: 'LIST' }],
    }),

    updateAddress: builder.mutation<
      APIResponse<Address>,
      { addressId: string; data: AddressUpdateForm }
    >({
      query: ({ addressId, data }) => ({
        url: `/address/update/${addressId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { addressId }) => [
        { type: 'logistics/Address', id: addressId },
        { type: 'logistics/Address', id: 'LIST' },
      ],
    }),

    // Category endpoints
    getCategories: builder.query<
      APIResponse<PaginatedResponse<Category>>,
      { filters?: CategoryFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/category/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics/Category' as const,
                id,
              })),
              { type: 'logistics/Category', id: 'LIST' },
            ]
          : [{ type: 'logistics/Category', id: 'LIST' }],
    }),

    getCategory: builder.query<APIResponse<Category>, string>({
      query: (categoryId) => ({
        url: `/category/search/${categoryId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result, error, id) => [
        { type: 'logistics/Category', id },
      ],
    }),

    createCategory: builder.mutation<APIResponse<Category>, CategoryForm>({
      query: (data) => ({
        url: '/category/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: [{ type: 'logistics/Category', id: 'LIST' }],
    }),

    updateCategory: builder.mutation<
      APIResponse<Category>,
      { categoryId: string; data: CategoryForm }
    >({
      query: ({ categoryId, data }) => ({
        url: `/category/update/${categoryId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { categoryId }) => [
        { type: 'logistics/Category', id: categoryId },
        { type: 'logistics/Category', id: 'LIST' },
      ],
    }),

    deleteCategory: builder.mutation<
      APIResponse<{ deleted: boolean }>,
      string
    >({
      query: (categoryId) => ({
        url: `/category/delete/${categoryId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, categoryId) => [
        { type: 'logistics/Category', id: categoryId },
        { type: 'logistics/Category', id: 'LIST' },
      ],
    }),

    // Customer endpoints (read-only from logistics perspective)
    getCustomers: builder.query<
      APIResponse<PaginatedResponse<Customer>>,
      { filters?: CustomerFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/customer/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics/Customer' as const,
                id,
              })),
              { type: 'logistics/Customer', id: 'LIST' },
            ]
          : [{ type: 'logistics/Customer', id: 'LIST' }],
    }),

    getCustomer: builder.query<APIResponse<Customer>, string>({
      query: (customerId) => ({
        url: `/customer/search/${customerId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result, error, id) => [
        { type: 'logistics/Customer', id },
      ],
    }),

    // Facility endpoints
    getFacilities: builder.query<
      APIResponse<PaginatedResponse<Facility>>,
      { filters?: FacilityFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/facility/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics/Facility' as const,
                id,
              })),
              { type: 'logistics/Facility', id: 'LIST' },
            ]
          : [{ type: 'logistics/Facility', id: 'LIST' }],
    }),

    getFacility: builder.query<APIResponse<Facility>, string>({
      query: (facilityId) => ({
        url: `/facility/search/${facilityId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result, error, id) => [{ type: 'logistics/Facility', id }],
    }),

    createFacility: builder.mutation<
      APIResponse<Facility>,
      FacilityCreationForm
    >({
      query: (data) => ({
        url: '/facility/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: [{ type: 'logistics/Facility', id: 'LIST' }],
    }),

    updateFacility: builder.mutation<
      APIResponse<Facility>,
      { facilityId: string; data: FacilityUpdateForm }
    >({
      query: ({ facilityId, data }) => ({
        url: `/facility/update/${facilityId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { facilityId }) => [
        { type: 'logistics/Facility', id: facilityId },
        { type: 'logistics/Facility', id: 'LIST' },
      ],
    }),

    deleteFacility: builder.mutation<
      APIResponse<{ deleted: boolean }>,
      string
    >({
      query: (facilityId) => ({
        url: `/facility/delete/${facilityId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, facilityId) => [
        { type: 'logistics/Facility', id: facilityId },
        { type: 'logistics/Facility', id: 'LIST' },
      ],
    }),

    // Inventory Item endpoints
    getInventoryItems: builder.query<
      APIResponse<PaginatedResponse<InventoryItem>>,
      { filters?: InventoryItemFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/inventory-item/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics/InventoryItem' as const,
                id,
              })),
              { type: 'logistics/InventoryItem', id: 'LIST' },
            ]
          : [{ type: 'logistics/InventoryItem', id: 'LIST' }],
    }),

    getInventoryItem: builder.query<APIResponse<InventoryItem>, string>({
      query: (inventoryItemId) => ({
        url: `/inventory-item/search/${inventoryItemId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result, error, id) => [{ type: 'logistics/InventoryItem', id }],
    }),

    createInventoryItem: builder.mutation<
      APIResponse<InventoryItem>,
      InventoryItemCreationForm
    >({
      query: (data) => ({
        url: '/inventory-item/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: [{ type: 'logistics/InventoryItem', id: 'LIST' }],
    }),

    updateInventoryItem: builder.mutation<
      APIResponse<InventoryItem>,
      { inventoryItemId: string; data: InventoryItemUpdateForm }
    >({
      query: ({ inventoryItemId, data }) => ({
        url: `/inventory-item/update/${inventoryItemId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { inventoryItemId }) => [
        { type: 'logistics/InventoryItem', id: inventoryItemId },
        { type: 'logistics/InventoryItem', id: 'LIST' },
      ],
    }),

    deleteInventoryItem: builder.mutation<
      APIResponse<{ deleted: boolean }>,
      string
    >({
      query: (inventoryItemId) => ({
        url: `/inventory-item/delete/${inventoryItemId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, inventoryItemId) => [
        { type: 'logistics/InventoryItem', id: inventoryItemId },
        { type: 'logistics/InventoryItem', id: 'LIST' },
      ],
    }),

    // Order endpoints (read-only from logistics perspective)
    getOrders: builder.query<
      APIResponse<PaginatedResponse<Order>>,
      { filters?: OrderFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/order/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics/Order' as const,
                id,
              })),
              { type: 'logistics/Order', id: 'LIST' },
            ]
          : [{ type: 'logistics/Order', id: 'LIST' }],
    }),

    getOrder: builder.query<APIResponse<Order>, string>({
      query: (orderId) => ({
        url: `/order/search/${orderId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result, error, id) => [{ type: 'logistics/Order', id }],
    }),

    // Product endpoints
    getProducts: builder.query<
      APIResponse<PaginatedResponse<Product>>,
      { filters?: ProductFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/product/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics/Product' as const,
                id,
              })),
              { type: 'logistics/Product', id: 'LIST' },
            ]
          : [{ type: 'logistics/Product', id: 'LIST' }],
    }),

    getProduct: builder.query<APIResponse<Product>, string>({
      query: (productId) => ({
        url: `/product/search/${productId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result, error, id) => [{ type: 'logistics/Product', id }],
    }),

    createProduct: builder.mutation<APIResponse<Product>, ProductCreationForm>(
      {
        query: (data) => ({
          url: '/product/create',
          method: 'POST',
          body: data,
        }),
        extraOptions: { service: 'logistics' },
        invalidatesTags: [{ type: 'logistics/Product', id: 'LIST' }],
      }
    ),

    updateProduct: builder.mutation<
      APIResponse<Product>,
      { productId: string; data: ProductUpdateForm }
    >({
      query: ({ productId, data }) => ({
        url: `/product/update/${productId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { productId }) => [
        { type: 'logistics/Product', id: productId },
        { type: 'logistics/Product', id: 'LIST' },
      ],
    }),

    deleteProduct: builder.mutation<
      APIResponse<{ deleted: boolean }>,
      string
    >({
      query: (productId) => ({
        url: `/product/delete/${productId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, productId) => [
        { type: 'logistics/Product', id: productId },
        { type: 'logistics/Product', id: 'LIST' },
      ],
    }),

    // Shipment endpoints
    getShipments: builder.query<
      APIResponse<PaginatedResponse<Shipment>>,
      { filters?: ShipmentFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/shipment/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics/Shipment' as const,
                id,
              })),
              { type: 'logistics/Shipment', id: 'LIST' },
            ]
          : [{ type: 'logistics/Shipment', id: 'LIST' }],
    }),

    getShipment: builder.query<APIResponse<Shipment>, string>({
      query: (shipmentId) => ({
        url: `/shipment/search/${shipmentId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result, error, id) => [{ type: 'logistics/Shipment', id }],
    }),

    createShipment: builder.mutation<
      APIResponse<Shipment>,
      ShipmentCreationForm
    >({
      query: (data) => ({
        url: '/shipment/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: [{ type: 'logistics/Shipment', id: 'LIST' }],
    }),

    updateShipment: builder.mutation<
      APIResponse<Shipment>,
      { shipmentId: string; data: ShipmentUpdateForm }
    >({
      query: ({ shipmentId, data }) => ({
        url: `/shipment/update/${shipmentId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
        { type: 'logistics/Shipment', id: 'LIST' },
      ],
    }),

    deleteShipment: builder.mutation<
      APIResponse<{ deleted: boolean }>,
      string
    >({
      query: (shipmentId) => ({
        url: `/shipment/delete/${shipmentId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, shipmentId) => [
        { type: 'logistics/Shipment', id: shipmentId },
        { type: 'logistics/Shipment', id: 'LIST' },
      ],
    }),

    addItemToShipment: builder.mutation<
      APIResponse<Shipment>,
      { shipmentId: string; data: ShipmentItemForm }
    >({
      query: ({ shipmentId, data }) => ({
        url: `/shipment/create/${shipmentId}/add`,
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
        { type: 'logistics/Shipment', id: 'LIST' },
      ],
    }),

    updateItemInShipment: builder.mutation<
      APIResponse<Shipment>,
      { shipmentId: string; itemId: string; data: ShipmentItemForm }
    >({
      query: ({ shipmentId, itemId, data }) => ({
        url: `/shipment/update/${shipmentId}/update/${itemId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
        { type: 'logistics/Shipment', id: 'LIST' },
      ],
    }),

    deleteItemFromShipment: builder.mutation<
      APIResponse<Shipment>,
      { shipmentId: string; itemId: string }
    >({
      query: ({ shipmentId, itemId }) => ({
        url: `/shipment/update/${shipmentId}/delete/${itemId}`,
        method: 'PATCH',
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
        { type: 'logistics/Shipment', id: 'LIST' },
      ],
    }),

    importShipment: builder.mutation<
      APIResponse<Shipment>,
      { shipmentId: string }
    >({
      query: ({ shipmentId }) => ({
        url: `/shipment/manage/${shipmentId}/import`,
        method: 'PATCH',
      }),
      extraOptions: { service: 'logistics' },
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'logistics/Shipment', id: shipmentId },
        { type: 'logistics/Shipment', id: 'LIST' },
        { type: 'logistics/InventoryItem', id: 'LIST' },
      ],
    }),

    // Supplier endpoints (read-only)
    getSuppliers: builder.query<
      APIResponse<PaginatedResponse<Supplier>>,
      { filters?: SupplierFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/supplier/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics/Supplier' as const,
                id,
              })),
              { type: 'logistics/Supplier', id: 'LIST' },
            ]
          : [{ type: 'logistics/Supplier', id: 'LIST' }],
    }),

    getSupplier: builder.query<APIResponse<Supplier>, string>({
      query: (supplierId) => ({
        url: `/supplier/search/${supplierId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'logistics' },
      providesTags: (result, error, id) => [{ type: 'logistics/Supplier', id }],
    }),
  }),
});

// Export hooks for usage in functional components
export const {
  // Address hooks
  useCreateAddressMutation,
  useGetAddressesByEntityQuery,
  useUpdateAddressMutation,

  // Category hooks
  useGetCategoriesQuery,
  useGetCategoryQuery,
  useCreateCategoryMutation,
  useUpdateCategoryMutation,
  useDeleteCategoryMutation,

  // Customer hooks
  useGetCustomersQuery,
  useGetCustomerQuery,

  // Facility hooks
  useGetFacilitiesQuery,
  useGetFacilityQuery,
  useCreateFacilityMutation,
  useUpdateFacilityMutation,
  useDeleteFacilityMutation,

  // Inventory Item hooks
  useGetInventoryItemsQuery,
  useGetInventoryItemQuery,
  useCreateInventoryItemMutation,
  useUpdateInventoryItemMutation,
  useDeleteInventoryItemMutation,

  // Order hooks
  useGetOrdersQuery,
  useGetOrderQuery,

  // Product hooks
  useGetProductsQuery,
  useGetProductQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,

  // Shipment hooks
  useGetShipmentsQuery,
  useGetShipmentQuery,
  useCreateShipmentMutation,
  useUpdateShipmentMutation,
  useDeleteShipmentMutation,
  useAddItemToShipmentMutation,
  useUpdateItemInShipmentMutation,
  useDeleteItemFromShipmentMutation,
  useImportShipmentMutation,

  // Supplier hooks
  useGetSuppliersQuery,
  useGetSupplierQuery,
} = logisticsApi;
