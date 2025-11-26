/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase API service with RTK Query
*/

import { api } from '@/lib/store/api';
import type { ApiResponse } from '@/lib/store/api/types';
import type {
  // Supplier types
  Supplier,
  SupplierDetail,
  CreateSupplierRequest,
  UpdateSupplierRequest,
  SupplierFilters,
  SupplierResponse,
  SuppliersResponse,
  SupplierDetailResponse,
  // Product types
  Product,
  Category,
  CreateProductRequest,
  UpdateProductRequest,
  ProductFilters,
  ProductResponse,
  ProductsResponse,
  CategoryResponse,
  CategoriesResponse,
  // Order types
  Order,
  OrderDetail,
  CreateOrderRequest,
  UpdateOrderRequest,
  UpdateOrderItemRequest,
  AddOrderItemRequest,
  CancelOrderRequest,
  OrderFilters,
  OrderResponse,
  OrdersResponse,
  OrderDetailResponse,
  // Facility types
  Facility,
  FacilityDetail,
  CreateFacilityRequest,
  UpdateFacilityRequest,
  FacilityFilters,
  FacilityResponse,
  FacilitiesResponse,
  FacilityDetailResponse,
  // Shipment types
  Shipment,
  ShipmentDetail,
  CreateShipmentRequest,
  UpdateShipmentRequest,
  ShipmentItemAddRequest,
  InventoryItemDetailUpdateRequest,
  ShipmentFacilityUpdateRequest,
  ShipmentFilters,
  ShipmentResponse,
  ShipmentsResponse,
  ShipmentDetailResponse,
  // Address types
  Address,
  CreateAddressRequest,
  UpdateAddressRequest,
  AddressFilters,
  AddressResponse,
  AddressesResponse,
} from '../types';

// Base URL for Purchase Service
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
const PURCHASE_API_BASE_URL = `${API_BASE_URL}/purchase-service/api/v1`;

// Purchase API endpoints
export const purchaseApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // ============= Supplier Endpoints =============
    getSuppliers: builder.query<SuppliersResponse, SupplierFilters>({
      query: (filters) => ({
        url: `${PURCHASE_API_BASE_URL}/supplier/search`,
        params: {
          page: filters.page || 1,
          size: filters.size || 10,
          sortBy: filters.sortBy || 'createdStamp',
          sortDirection: filters.sortDirection || 'desc',
          query: filters.query,
          statusId: filters.statusId,
        },
      }),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'purchase/Supplier' as const,
                id,
              })),
              { type: 'purchase/Supplier', id: 'LIST' },
            ]
          : [{ type: 'purchase/Supplier', id: 'LIST' }],
    }),

    getSupplierById: builder.query<SupplierDetailResponse, string>({
      query: (supplierId) => ({
        url: `${PURCHASE_API_BASE_URL}/supplier/search/${supplierId}`,
      }),
      providesTags: (result, error, supplierId) => [
        { type: 'purchase/Supplier', id: supplierId },
      ],
    }),

    createSupplier: builder.mutation<SupplierResponse, CreateSupplierRequest>({
      query: (data) => ({
        url: `${PURCHASE_API_BASE_URL}/supplier/create`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'purchase/Supplier', id: 'LIST' }],
    }),

    updateSupplier: builder.mutation<
      SupplierResponse,
      { supplierId: string; data: UpdateSupplierRequest }
    >({
      query: ({ supplierId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/supplier/update/${supplierId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { supplierId }) => [
        { type: 'purchase/Supplier', id: supplierId },
        { type: 'purchase/Supplier', id: 'LIST' },
      ],
    }),

    deleteSupplier: builder.mutation<void, string>({
      query: (supplierId) => ({
        url: `${PURCHASE_API_BASE_URL}/supplier/delete/${supplierId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, supplierId) => [
        { type: 'purchase/Supplier', id: supplierId },
        { type: 'purchase/Supplier', id: 'LIST' },
      ],
    }),

    // ============= Product Endpoints =============
    getProducts: builder.query<ProductsResponse, ProductFilters>({
      query: (filters) => ({
        url: `${PURCHASE_API_BASE_URL}/product/search`,
        params: {
          page: filters.page || 1,
          size: filters.size || 10,
          sortBy: filters.sortBy || 'createdStamp',
          sortDirection: filters.sortDirection || 'desc',
          query: filters.query,
          categoryId: filters.categoryId,
          statusId: filters.statusId,
        },
      }),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'purchase/Product' as const,
                id,
              })),
              { type: 'purchase/Product', id: 'LIST' },
            ]
          : [{ type: 'purchase/Product', id: 'LIST' }],
    }),

    getProductById: builder.query<ProductResponse, string>({
      query: (productId) => ({
        url: `${PURCHASE_API_BASE_URL}/product/search/${productId}`,
      }),
      providesTags: (result, error, productId) => [
        { type: 'purchase/Product', id: productId },
      ],
    }),

    createProduct: builder.mutation<ProductResponse, CreateProductRequest>({
      query: (data) => ({
        url: `${PURCHASE_API_BASE_URL}/product/create`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'purchase/Product', id: 'LIST' }],
    }),

    updateProduct: builder.mutation<
      ProductResponse,
      { productId: string; data: UpdateProductRequest }
    >({
      query: ({ productId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/product/update/${productId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { productId }) => [
        { type: 'purchase/Product', id: productId },
        { type: 'purchase/Product', id: 'LIST' },
      ],
    }),

    deleteProduct: builder.mutation<void, string>({
      query: (productId) => ({
        url: `${PURCHASE_API_BASE_URL}/product/delete/${productId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, productId) => [
        { type: 'purchase/Product', id: productId },
        { type: 'purchase/Product', id: 'LIST' },
      ],
    }),

    // ============= Category Endpoints =============
    getCategories: builder.query<CategoriesResponse, void>({
      query: () => ({
        url: `${PURCHASE_API_BASE_URL}/category/search?page=1&size=1000`,
      }),
      providesTags: [{ type: 'purchase/Category', id: 'LIST' }],
    }),

    createCategory: builder.mutation<CategoryResponse, { name: string; parentCategoryId?: string }>({
      query: (data) => ({
        url: `${PURCHASE_API_BASE_URL}/category/create`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'purchase/Category', id: 'LIST' }],
    }),

    // ============= Order Endpoints =============
    getOrders: builder.query<OrdersResponse, OrderFilters>({
      query: (filters) => ({
        url: `${PURCHASE_API_BASE_URL}/order/search`,
        params: {
          page: filters.page || 1,
          size: filters.size || 10,
          sortBy: filters.sortBy || 'createdStamp',
          sortDirection: filters.sortDirection || 'desc',
          query: filters.query,
          statusId: filters.statusId,
          fromSupplierId: filters.fromSupplierId,
          saleChannelId: filters.saleChannelId,
          orderDateAfter: filters.orderDateAfter,
          orderDateBefore: filters.orderDateBefore,
          deliveryAfter: filters.deliveryAfter,
          deliveryBefore: filters.deliveryBefore,
        },
      }),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'purchase/Order' as const,
                id,
              })),
              { type: 'purchase/Order', id: 'LIST' },
            ]
          : [{ type: 'purchase/Order', id: 'LIST' }],
    }),

    getOrderById: builder.query<OrderDetailResponse, string>({
      query: (orderId) => ({
        url: `${PURCHASE_API_BASE_URL}/order/search/${orderId}`,
      }),
      providesTags: (result, error, orderId) => [
        { type: 'purchase/Order', id: orderId },
      ],
    }),

    createOrder: builder.mutation<OrderResponse, CreateOrderRequest>({
      query: (data) => ({
        url: `${PURCHASE_API_BASE_URL}/order/create`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'purchase/Order', id: 'LIST' }],
    }),

    updateOrder: builder.mutation<
      OrderResponse,
      { orderId: string; data: UpdateOrderRequest }
    >({
      query: ({ orderId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/order/update/${orderId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'purchase/Order', id: orderId },
        { type: 'purchase/Order', id: 'LIST' },
      ],
    }),

    approveOrder: builder.mutation<OrderResponse, string>({
      query: (orderId) => ({
        url: `${PURCHASE_API_BASE_URL}/order/manage/${orderId}/approve`,
        method: 'PATCH',
      }),
      invalidatesTags: (result, error, orderId) => [
        { type: 'purchase/Order', id: orderId },
        { type: 'purchase/Order', id: 'LIST' },
      ],
    }),

    cancelOrder: builder.mutation<
      OrderResponse,
      { orderId: string; data?: CancelOrderRequest }
    >({
      query: ({ orderId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/order/manage/${orderId}/cancel`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'purchase/Order', id: orderId },
        { type: 'purchase/Order', id: 'LIST' },
      ],
    }),

    markOrderAsReady: builder.mutation<OrderResponse, string>({
      query: (orderId) => ({
        url: `${PURCHASE_API_BASE_URL}/order/update/${orderId}/ready`,
        method: 'PATCH',
      }),
      invalidatesTags: (result, error, orderId) => [
        { type: 'purchase/Order', id: orderId },
        { type: 'purchase/Order', id: 'LIST' },
      ],
    }),

    addProductToOrder: builder.mutation<
      OrderResponse,
      { orderId: string; data: AddOrderItemRequest }
    >({
      query: ({ orderId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/order/create/${orderId}/add`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'purchase/Order', id: orderId },
      ],
    }),

    updateOrderItem: builder.mutation<
      OrderResponse,
      { orderId: string; orderItemId: string; data: UpdateOrderItemRequest }
    >({
      query: ({ orderId, orderItemId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/order/update/${orderId}/update/${orderItemId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'purchase/Order', id: orderId },
      ],
    }),

    deleteOrderItem: builder.mutation<
      OrderResponse,
      { orderId: string; orderItemId: string }
    >({
      query: ({ orderId, orderItemId }) => ({
        url: `${PURCHASE_API_BASE_URL}/order/update/${orderId}/delete/${orderItemId}`,
        method: 'PATCH',
      }),
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'purchase/Order', id: orderId },
      ],
    }),

    // ============= Facility Endpoints =============
    getFacilities: builder.query<FacilitiesResponse, FacilityFilters>({
      query: (filters) => ({
        url: `${PURCHASE_API_BASE_URL}/facility/search`,
        params: {
          page: filters.page || 1,
          size: filters.size || 10,
          sortBy: filters.sortBy || 'createdStamp',
          sortDirection: filters.sortDirection || 'desc',
          query: filters.query,
          type: filters.type,
          statusId: filters.statusId,
        },
      }),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'purchase/Facility' as const,
                id,
              })),
              { type: 'purchase/Facility', id: 'LIST' },
            ]
          : [{ type: 'purchase/Facility', id: 'LIST' }],
    }),

    getFacilityById: builder.query<FacilityDetailResponse, string>({
      query: (facilityId) => ({
        url: `${PURCHASE_API_BASE_URL}/facility/search/${facilityId}`,
      }),
      providesTags: (result, error, facilityId) => [
        { type: 'purchase/Facility', id: facilityId },
      ],
    }),

    createFacility: builder.mutation<FacilityResponse, CreateFacilityRequest>({
      query: (data) => ({
        url: `${PURCHASE_API_BASE_URL}/facility/create`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'purchase/Facility', id: 'LIST' }],
    }),

    updateFacility: builder.mutation<
      FacilityResponse,
      { facilityId: string; data: UpdateFacilityRequest }
    >({
      query: ({ facilityId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/facility/update/${facilityId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { facilityId }) => [
        { type: 'purchase/Facility', id: facilityId },
        { type: 'purchase/Facility', id: 'LIST' },
      ],
    }),

    deleteFacility: builder.mutation<void, string>({
      query: (facilityId) => ({
        url: `${PURCHASE_API_BASE_URL}/facility/delete/${facilityId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, facilityId) => [
        { type: 'purchase/Facility', id: facilityId },
        { type: 'purchase/Facility', id: 'LIST' },
      ],
    }),

    // ============= Shipment Endpoints =============
    getShipments: builder.query<ShipmentsResponse, ShipmentFilters>({
      query: (filters) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/search`,
        params: {
          page: filters.page || 1,
          size: filters.size || 10,
          sortBy: filters.sortBy || 'createdStamp',
          sortDirection: filters.sortDirection || 'desc',
          query: filters.query,
          statusId: filters.statusId,
          orderId: filters.orderId,
          fromFacilityId: filters.fromFacilityId,
          toFacilityId: filters.toFacilityId,
          fromDate: filters.fromDate,
          toDate: filters.toDate,
        },
      }),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'purchase/Shipment' as const,
                id,
              })),
              { type: 'purchase/Shipment', id: 'LIST' },
            ]
          : [{ type: 'purchase/Shipment', id: 'LIST' }],
    }),

    getShipmentById: builder.query<ShipmentDetailResponse, string>({
      query: (shipmentId) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/search/${shipmentId}`,
      }),
      providesTags: (result, error, shipmentId) => [
        { type: 'purchase/Shipment', id: shipmentId },
      ],
    }),

    createShipment: builder.mutation<ShipmentResponse, CreateShipmentRequest>({
      query: (data) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/create`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [
        { type: 'purchase/Shipment', id: 'LIST' },
        { type: 'purchase/Order', id: 'LIST' },
      ],
    }),

    updateShipment: builder.mutation<
      ShipmentResponse,
      { shipmentId: string; data: UpdateShipmentRequest }
    >({
      query: ({ shipmentId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/update/${shipmentId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'purchase/Shipment', id: shipmentId },
        { type: 'purchase/Shipment', id: 'LIST' },
      ],
    }),

    deleteShipment: builder.mutation<void, string>({
      query: (shipmentId) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/delete/${shipmentId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, shipmentId) => [
        { type: 'purchase/Shipment', id: shipmentId },
        { type: 'purchase/Shipment', id: 'LIST' },
      ],
    }),

    // Import shipment (matches /manage/{shipmentId}/import endpoint)
    importShipment: builder.mutation<ShipmentResponse, string>({
      query: (shipmentId) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/manage/${shipmentId}/import`,
        method: 'PATCH',
      }),
      invalidatesTags: (result, error, shipmentId) => [
        { type: 'purchase/Shipment', id: shipmentId },
        { type: 'purchase/Shipment', id: 'LIST' },
        { type: 'purchase/Order', id: 'LIST' },
      ],
    }),

    // Add item to shipment (matches /create/{shipmentId}/add endpoint)
    addItemToShipment: builder.mutation<
      ShipmentResponse,
      { shipmentId: string; data: ShipmentItemAddRequest }
    >({
      query: ({ shipmentId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/create/${shipmentId}/add`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'purchase/Shipment', id: shipmentId },
        { type: 'purchase/Shipment', id: 'LIST' },
      ],
    }),

    // Update item in shipment (matches /update/{shipmentId}/update/{itemId} endpoint)
    updateItemInShipment: builder.mutation<
      ShipmentResponse,
      { shipmentId: string; itemId: string; data: InventoryItemDetailUpdateRequest }
    >({
      query: ({ shipmentId, itemId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/update/${shipmentId}/update/${itemId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'purchase/Shipment', id: shipmentId },
        { type: 'purchase/Shipment', id: 'LIST' },
      ],
    }),

    // Delete item from shipment (matches /update/{shipmentId}/delete/{itemId} endpoint)
    deleteItemFromShipment: builder.mutation<
      ShipmentResponse,
      { shipmentId: string; itemId: string }
    >({
      query: ({ shipmentId, itemId }) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/update/${shipmentId}/delete/${itemId}`,
        method: 'PATCH',
      }),
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'purchase/Shipment', id: shipmentId },
        { type: 'purchase/Shipment', id: 'LIST' },
      ],
    }),

    // Update shipment facility (matches /update/{shipmentId}/facility endpoint)
    updateShipmentFacility: builder.mutation<
      ShipmentResponse,
      { shipmentId: string; data: ShipmentFacilityUpdateRequest }
    >({
      query: ({ shipmentId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/update/${shipmentId}/facility`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { shipmentId }) => [
        { type: 'purchase/Shipment', id: shipmentId },
        { type: 'purchase/Shipment', id: 'LIST' },
      ],
    }),

    // Get shipments by order ID (matches /search/by-order/{orderId} endpoint)
    getShipmentsByOrderId: builder.query<ApiResponse<Shipment[]>, string>({
      query: (orderId) => ({
        url: `${PURCHASE_API_BASE_URL}/shipment/search/by-order/${orderId}`,
      }),
      providesTags: (result, error, orderId) => [
        { type: 'purchase/Shipment', id: `ORDER_${orderId}` },
        { type: 'purchase/Shipment', id: 'LIST' },
      ],
    }),

    // ============= Address Endpoints =============
    getAddresses: builder.query<AddressesResponse, AddressFilters>({
      query: (filters) => ({
        url: `${PURCHASE_API_BASE_URL}/address/search`,
        params: {
          page: filters.page || 1,
          size: filters.size || 10,
          sortBy: filters.sortBy || 'createdStamp',
          sortDirection: filters.sortDirection || 'desc',
          entityId: filters.entityId,
          entityType: filters.entityType,
          addressType: filters.addressType,
        },
      }),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'purchase/Address' as const,
                id,
              })),
              { type: 'purchase/Address', id: 'LIST' },
            ]
          : [{ type: 'purchase/Address', id: 'LIST' }],
    }),

    getAddressById: builder.query<AddressResponse, string>({
      query: (addressId) => ({
        url: `${PURCHASE_API_BASE_URL}/address/search/${addressId}`,
      }),
      providesTags: (result, error, addressId) => [
        { type: 'purchase/Address', id: addressId },
      ],
    }),

    createAddress: builder.mutation<AddressResponse, CreateAddressRequest>({
      query: (data) => ({
        url: `${PURCHASE_API_BASE_URL}/address/create`,
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'purchase/Address', id: 'LIST' }],
    }),

    updateAddress: builder.mutation<
      AddressResponse,
      { addressId: string; data: UpdateAddressRequest }
    >({
      query: ({ addressId, data }) => ({
        url: `${PURCHASE_API_BASE_URL}/address/update/${addressId}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: (result, error, { addressId }) => [
        { type: 'purchase/Address', id: addressId },
        { type: 'purchase/Address', id: 'LIST' },
      ],
    }),
  }),
});

// Export hooks for usage in functional components
export const {
  // Supplier hooks
  useGetSuppliersQuery,
  useGetSupplierByIdQuery,
  useCreateSupplierMutation,
  useUpdateSupplierMutation,
  useDeleteSupplierMutation,
  // Product hooks
  useGetProductsQuery,
  useGetProductByIdQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
  // Category hooks
  useGetCategoriesQuery,
  useCreateCategoryMutation,
  // Order hooks
  useGetOrdersQuery,
  useGetOrderByIdQuery,
  useCreateOrderMutation,
  useUpdateOrderMutation,
  useApproveOrderMutation,
  useCancelOrderMutation,
  useMarkOrderAsReadyMutation,
  useAddProductToOrderMutation,
  useUpdateOrderItemMutation,
  useDeleteOrderItemMutation,
  // Facility hooks
  useGetFacilitiesQuery,
  useGetFacilityByIdQuery,
  useCreateFacilityMutation,
  useUpdateFacilityMutation,
  useDeleteFacilityMutation,
  // Shipment hooks
  useGetShipmentsQuery,
  useGetShipmentByIdQuery,
  useCreateShipmentMutation,
  useUpdateShipmentMutation,
  useDeleteShipmentMutation,
  useImportShipmentMutation,
  useAddItemToShipmentMutation,
  useUpdateItemInShipmentMutation,
  useDeleteItemFromShipmentMutation,
  useUpdateShipmentFacilityMutation,
  useGetShipmentsByOrderIdQuery,
  // Address hooks
  useGetAddressesQuery,
  useGetAddressByIdQuery,
  useCreateAddressMutation,
  useUpdateAddressMutation,
} = purchaseApi;
