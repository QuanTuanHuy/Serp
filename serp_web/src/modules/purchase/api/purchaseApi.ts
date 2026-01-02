// Purchase API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import type {
  Address,
  AddressCreationForm,
  AddressUpdateForm,
  Category,
  CategoryForm,
  CategoryFilters,
  Supplier,
  SupplierCreationForm,
  SupplierUpdateForm,
  SupplierFilters,
  Facility,
  FacilityCreationForm,
  FacilityUpdateForm,
  FacilityFilters,
  Product,
  ProductCreationForm,
  ProductUpdateForm,
  ProductFilters,
  Order,
  OrderCreationForm,
  OrderUpdateForm,
  OrderCancellationForm,
  OrderItem,
  OrderItemUpdateForm,
  OrderFilters,
  Shipment,
  ShipmentFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
} from '../types';

// Define Purchase API slice extending the main API
export const purchaseApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Supplier endpoints
    getSuppliers: builder.query<
      APIResponse<PaginatedResponse<Supplier>>,
      { filters?: SupplierFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/supplier/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'PurchaseSupplier' as const,
                id,
              })),
              { type: 'PurchaseSupplier', id: 'LIST' },
            ]
          : [{ type: 'PurchaseSupplier', id: 'LIST' }],
    }),

    getSupplier: builder.query<APIResponse<Supplier>, string>({
      query: (supplierId) => ({
        url: `/supplier/search/${supplierId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result, error, id) => [{ type: 'PurchaseSupplier', id }],
    }),

    createSupplier: builder.mutation<
      APIResponse<Supplier>,
      SupplierCreationForm
    >({
      query: (data) => ({
        url: '/supplier/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: [{ type: 'PurchaseSupplier', id: 'LIST' }],
    }),

    updateSupplier: builder.mutation<
      APIResponse<Supplier>,
      { supplierId: string; data: SupplierUpdateForm }
    >({
      query: ({ supplierId, data }) => ({
        url: `/supplier/update/${supplierId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { supplierId }) => [
        { type: 'PurchaseSupplier', id: supplierId },
        { type: 'PurchaseSupplier', id: 'LIST' },
      ],
    }),

    deleteSupplier: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (supplierId) => ({
          url: `/supplier/delete/${supplierId}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'purchase-service' },
        invalidatesTags: (result, error, supplierId) => [
          { type: 'PurchaseSupplier', id: supplierId },
          { type: 'PurchaseSupplier', id: 'LIST' },
        ],
      }
    ),

    // Address endpoints
    createAddress: builder.mutation<APIResponse<Address>, AddressCreationForm>({
      query: (data) => ({
        url: '/address/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: [{ type: 'PurchaseAddress', id: 'LIST' }],
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
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { addressId }) => [
        { type: 'PurchaseAddress', id: addressId },
        { type: 'PurchaseAddress', id: 'LIST' },
      ],
    }),

    getAddressesByEntity: builder.query<APIResponse<Address[]>, string>({
      query: (entityId) => ({
        url: `/address/search/by-entity/${entityId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.map(({ id }) => ({
                type: 'PurchaseAddress' as const,
                id,
              })),
              { type: 'PurchaseAddress', id: 'LIST' },
            ]
          : [{ type: 'PurchaseAddress', id: 'LIST' }],
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
      extraOptions: { service: 'purchase-service' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'PurchaseCategory' as const,
                id,
              })),
              { type: 'PurchaseCategory', id: 'LIST' },
            ]
          : [{ type: 'PurchaseCategory', id: 'LIST' }],
    }),

    getCategory: builder.query<APIResponse<Category>, string>({
      query: (categoryId) => ({
        url: `/category/search/${categoryId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result, error, id) => [{ type: 'PurchaseCategory', id }],
    }),

    createCategory: builder.mutation<APIResponse<Category>, CategoryForm>({
      query: (data) => ({
        url: '/category/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: [{ type: 'PurchaseCategory', id: 'LIST' }],
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
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { categoryId }) => [
        { type: 'PurchaseCategory', id: categoryId },
        { type: 'PurchaseCategory', id: 'LIST' },
      ],
    }),

    deleteCategory: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (categoryId) => ({
          url: `/category/delete/${categoryId}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'purchase-service' },
        invalidatesTags: (result, error, categoryId) => [
          { type: 'PurchaseCategory', id: categoryId },
          { type: 'PurchaseCategory', id: 'LIST' },
        ],
      }
    ),

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
      extraOptions: { service: 'purchase-service' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'PurchaseFacility' as const,
                id,
              })),
              { type: 'PurchaseFacility', id: 'LIST' },
            ]
          : [{ type: 'PurchaseFacility', id: 'LIST' }],
    }),

    getFacility: builder.query<APIResponse<Facility>, string>({
      query: (facilityId) => ({
        url: `/facility/search/${facilityId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result, error, id) => [{ type: 'PurchaseFacility', id }],
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
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: [{ type: 'PurchaseFacility', id: 'LIST' }],
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
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { facilityId }) => [
        { type: 'PurchaseFacility', id: facilityId },
        { type: 'PurchaseFacility', id: 'LIST' },
      ],
    }),

    deleteFacility: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (facilityId) => ({
          url: `/facility/delete/${facilityId}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'purchase-service' },
        invalidatesTags: (result, error, facilityId) => [
          { type: 'PurchaseFacility', id: facilityId },
          { type: 'PurchaseFacility', id: 'LIST' },
        ],
      }
    ),

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
      extraOptions: { service: 'purchase-service' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'PurchaseProduct' as const,
                id,
              })),
              { type: 'PurchaseProduct', id: 'LIST' },
            ]
          : [{ type: 'PurchaseProduct', id: 'LIST' }],
    }),

    getProduct: builder.query<APIResponse<Product>, string>({
      query: (productId) => ({
        url: `/product/search/${productId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result, error, id) => [{ type: 'PurchaseProduct', id }],
    }),

    createProduct: builder.mutation<APIResponse<Product>, ProductCreationForm>({
      query: (data) => ({
        url: '/product/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: [{ type: 'PurchaseProduct', id: 'LIST' }],
    }),

    updateProduct: builder.mutation<
      APIResponse<Product>,
      { productId: string; data: ProductUpdateForm }
    >({
      query: ({ productId, data }) => ({
        url: `/product/update/${productId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { productId }) => [
        { type: 'PurchaseProduct', id: productId },
        { type: 'PurchaseProduct', id: 'LIST' },
      ],
    }),

    deleteProduct: builder.mutation<APIResponse<{ deleted: boolean }>, string>({
      query: (productId) => ({
        url: `/product/delete/${productId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, productId) => [
        { type: 'PurchaseProduct', id: productId },
        { type: 'PurchaseProduct', id: 'LIST' },
      ],
    }),

    // Order endpoints
    getOrders: builder.query<
      APIResponse<PaginatedResponse<Order>>,
      { filters?: OrderFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/order/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'PurchaseOrder' as const,
                id,
              })),
              { type: 'PurchaseOrder', id: 'LIST' },
            ]
          : [{ type: 'PurchaseOrder', id: 'LIST' }],
    }),

    getOrder: builder.query<APIResponse<Order>, string>({
      query: (orderId) => ({
        url: `/order/search/${orderId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result, error, id) => [{ type: 'PurchaseOrder', id }],
    }),

    createOrder: builder.mutation<APIResponse<Order>, OrderCreationForm>({
      query: (data) => ({
        url: '/order/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: [{ type: 'PurchaseOrder', id: 'LIST' }],
    }),

    updateOrder: builder.mutation<
      APIResponse<Order>,
      { orderId: string; data: OrderUpdateForm }
    >({
      query: ({ orderId, data }) => ({
        url: `/order/update/${orderId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'PurchaseOrder', id: orderId },
        { type: 'PurchaseOrder', id: 'LIST' },
      ],
    }),

    deleteOrder: builder.mutation<APIResponse<{ deleted: boolean }>, string>({
      query: (orderId) => ({
        url: `/order/delete/${orderId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, orderId) => [
        { type: 'PurchaseOrder', id: orderId },
        { type: 'PurchaseOrder', id: 'LIST' },
      ],
    }),

    approveOrder: builder.mutation<APIResponse<Order>, string>({
      query: (orderId) => ({
        url: `/order/manage/${orderId}/approve`,
        method: 'PATCH',
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, orderId) => [
        { type: 'PurchaseOrder', id: orderId },
        { type: 'PurchaseOrder', id: 'LIST' },
      ],
    }),

    cancelOrder: builder.mutation<
      APIResponse<Order>,
      { orderId: string; data: OrderCancellationForm }
    >({
      query: ({ orderId, data }) => ({
        url: `/order/manage/${orderId}/cancel`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'PurchaseOrder', id: orderId },
        { type: 'PurchaseOrder', id: 'LIST' },
      ],
    }),

    addProductToOrder: builder.mutation<
      APIResponse<Order>,
      { orderId: string; data: OrderItem }
    >({
      query: ({ orderId, data }) => ({
        url: `/order/create/${orderId}/add`,
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'PurchaseOrder', id: orderId },
      ],
    }),

    updateProductInOrder: builder.mutation<
      APIResponse<Order>,
      { orderId: string; orderItemId: string; data: OrderItemUpdateForm }
    >({
      query: ({ orderId, orderItemId, data }) => ({
        url: `/order/update/${orderId}/update/${orderItemId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'PurchaseOrder', id: orderId },
      ],
    }),

    deleteProductFromOrder: builder.mutation<
      APIResponse<Order>,
      { orderId: string; orderItemId: string }
    >({
      query: ({ orderId, orderItemId }) => ({
        url: `/order/update/${orderId}/delete/${orderItemId}`,
        method: 'PATCH',
      }),
      extraOptions: { service: 'purchase-service' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'PurchaseOrder', id: orderId },
      ],
    }),

    // Shipment endpoints
    getShipment: builder.query<APIResponse<Shipment>, string>({
      query: (shipmentId) => ({
        url: `/shipment/search/${shipmentId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result, error, id) => [{ type: 'PurchaseShipment', id }],
    }),

    getShipmentsByOrder: builder.query<APIResponse<Shipment[]>, string>({
      query: (orderId) => ({
        url: `/shipment/search/by-order/${orderId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'purchase-service' },
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.map(({ id }) => ({
                type: 'PurchaseShipment' as const,
                id,
              })),
              { type: 'PurchaseShipment', id: 'LIST' },
            ]
          : [{ type: 'PurchaseShipment', id: 'LIST' }],
    }),
  }),
});

// Export hooks for usage in components
export const {
  useGetSuppliersQuery,
  useGetSupplierQuery,
  useCreateSupplierMutation,
  useUpdateSupplierMutation,
  useDeleteSupplierMutation,
  useCreateAddressMutation,
  useUpdateAddressMutation,
  useGetAddressesByEntityQuery,
  useGetCategoriesQuery,
  useGetCategoryQuery,
  useCreateCategoryMutation,
  useUpdateCategoryMutation,
  useDeleteCategoryMutation,
  useGetFacilitiesQuery,
  useGetFacilityQuery,
  useCreateFacilityMutation,
  useUpdateFacilityMutation,
  useDeleteFacilityMutation,
  useGetProductsQuery,
  useGetProductQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
  useGetOrdersQuery,
  useGetOrderQuery,
  useCreateOrderMutation,
  useUpdateOrderMutation,
  useDeleteOrderMutation,
  useApproveOrderMutation,
  useCancelOrderMutation,
  useAddProductToOrderMutation,
  useUpdateProductInOrderMutation,
  useDeleteProductFromOrderMutation,
  useGetShipmentQuery,
  useGetShipmentsByOrderQuery,
} = purchaseApi;
