// Sales API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import type {
  Address,
  AddressCreationForm,
  AddressUpdateForm,
  Category,
  CategoryForm,
  CategoryFilters,
  Customer,
  CustomerCreationForm,
  CustomerUpdateForm,
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
  Product,
  ProductCreationForm,
  ProductUpdateForm,
  ProductFilters,
  Order,
  OrderCreationForm,
  OrderUpdateForm,
  OrderCancellationForm,
  OrderItem,
  OrderFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
} from '../types';

// Define Sales API slice extending the main API
export const salesApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Customer endpoints
    getCustomers: builder.query<
      APIResponse<PaginatedResponse<Customer>>,
      { filters?: CustomerFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/customer/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'SalesCustomer' as const,
                id,
              })),
              { type: 'SalesCustomer', id: 'LIST' },
            ]
          : [{ type: 'SalesCustomer', id: 'LIST' }],
    }),

    getCustomer: builder.query<APIResponse<Customer>, string>({
      query: (customerId) => ({
        url: `/customer/search/${customerId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result, error, id) => [{ type: 'SalesCustomer', id }],
    }),

    createCustomer: builder.mutation<
      APIResponse<Customer>,
      CustomerCreationForm
    >({
      query: (data) => ({
        url: '/customer/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'sales' },
      invalidatesTags: [{ type: 'SalesCustomer', id: 'LIST' }],
    }),

    updateCustomer: builder.mutation<
      APIResponse<Customer>,
      { customerId: string; data: CustomerUpdateForm }
    >({
      query: ({ customerId, data }) => ({
        url: `/customer/update/${customerId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { customerId }) => [
        { type: 'SalesCustomer', id: customerId },
        { type: 'SalesCustomer', id: 'LIST' },
      ],
    }),

    deleteCustomer: builder.mutation<
      APIResponse<{ deleted: boolean }>,
      string
    >({
      query: (customerId) => ({
        url: `/customer/delete/${customerId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, customerId) => [
        { type: 'SalesCustomer', id: customerId },
        { type: 'SalesCustomer', id: 'LIST' },
      ],
    }),

    // Address endpoints
    createAddress: builder.mutation<APIResponse<Address>, AddressCreationForm>(
      {
        query: (data) => ({
          url: '/address/create',
          method: 'POST',
          body: data,
        }),
        extraOptions: { service: 'sales' },
        invalidatesTags: [{ type: 'Address', id: 'LIST' }],
      }
    ),

    updateAddress: builder.mutation<
      APIResponse<Address>,
      { addressId: string; data: AddressUpdateForm }
    >({
      query: ({ addressId, data }) => ({
        url: `/address/update/${addressId}`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { addressId }) => [
        { type: 'Address', id: addressId },
        { type: 'Address', id: 'LIST' },
      ],
    }),

    deleteAddress: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (addressId) => ({
          url: `/address/delete/${addressId}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'sales' },
        invalidatesTags: (result, error, addressId) => [
          { type: 'Address', id: addressId },
          { type: 'Address', id: 'LIST' },
        ],
      }
    ),

    getAddresses: builder.query<
      APIResponse<Address[]>,
      { entityId: string; entityType: string }
    >({
      query: ({ entityId, entityType }) => ({
        url: '/address/search',
        method: 'GET',
        params: { entityId, entityType },
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result) =>
        result?.data
          ? [
              ...result.data.map(({ id }) => ({
                type: 'Address' as const,
                id,
              })),
              { type: 'Address', id: 'LIST' },
            ]
          : [{ type: 'Address', id: 'LIST' }],
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
      extraOptions: { service: 'sales' },
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Category' as const,
                id,
              })),
              { type: 'Category', id: 'LIST' },
            ]
          : [{ type: 'Category', id: 'LIST' }],
    }),

    getCategory: builder.query<APIResponse<Category>, string>({
      query: (categoryId) => ({
        url: `/category/search/${categoryId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result, error, id) => [{ type: 'Category', id }],
    }),

    createCategory: builder.mutation<APIResponse<Category>, CategoryForm>({
      query: (data) => ({
        url: '/category/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'sales' },
      invalidatesTags: [{ type: 'Category', id: 'LIST' }],
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { categoryId }) => [
        { type: 'Category', id: categoryId },
        { type: 'Category', id: 'LIST' },
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, categoryId) => [
        { type: 'Category', id: categoryId },
        { type: 'Category', id: 'LIST' },
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
      extraOptions: { service: 'sales' },
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Facility' as const,
                id,
              })),
              { type: 'Facility', id: 'LIST' },
            ]
          : [{ type: 'Facility', id: 'LIST' }],
    }),

    getFacility: builder.query<APIResponse<Facility>, string>({
      query: (facilityId) => ({
        url: `/facility/search/${facilityId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result, error, id) => [{ type: 'Facility', id }],
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
      extraOptions: { service: 'sales' },
      invalidatesTags: [{ type: 'Facility', id: 'LIST' }],
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { facilityId }) => [
        { type: 'Facility', id: facilityId },
        { type: 'Facility', id: 'LIST' },
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, facilityId) => [
        { type: 'Facility', id: facilityId },
        { type: 'Facility', id: 'LIST' },
      ],
    }),

    // Inventory Item endpoints
    getInventoryItems: builder.query<
      APIResponse<PaginatedResponse<InventoryItemDetail>>,
      { filters?: InventoryItemFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/inventory-item/search',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'InventoryItem' as const,
                id,
              })),
              { type: 'InventoryItem', id: 'LIST' },
            ]
          : [{ type: 'InventoryItem', id: 'LIST' }],
    }),

    getInventoryItem: builder.query<APIResponse<InventoryItemDetail>, string>({
      query: (inventoryItemId) => ({
        url: `/inventory-item/search/${inventoryItemId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result, error, id) => [{ type: 'InventoryItem', id }],
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
      extraOptions: { service: 'sales' },
      invalidatesTags: [{ type: 'InventoryItem', id: 'LIST' }],
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { inventoryItemId }) => [
        { type: 'InventoryItem', id: inventoryItemId },
        { type: 'InventoryItem', id: 'LIST' },
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, inventoryItemId) => [
        { type: 'InventoryItem', id: inventoryItemId },
        { type: 'InventoryItem', id: 'LIST' },
      ],
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
      extraOptions: { service: 'sales' },
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Product' as const,
                id,
              })),
              { type: 'Product', id: 'LIST' },
            ]
          : [{ type: 'Product', id: 'LIST' }],
    }),

    getProduct: builder.query<APIResponse<Product>, string>({
      query: (productId) => ({
        url: `/product/search/${productId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result, error, id) => [{ type: 'Product', id }],
    }),

    createProduct: builder.mutation<APIResponse<Product>, ProductCreationForm>(
      {
        query: (data) => ({
          url: '/product/create',
          method: 'POST',
          body: data,
        }),
        extraOptions: { service: 'sales' },
        invalidatesTags: [{ type: 'Product', id: 'LIST' }],
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { productId }) => [
        { type: 'Product', id: productId },
        { type: 'Product', id: 'LIST' },
      ],
    }),

    deleteProduct: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (productId) => ({
          url: `/product/delete/${productId}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'sales' },
        invalidatesTags: (result, error, productId) => [
          { type: 'Product', id: productId },
          { type: 'Product', id: 'LIST' },
        ],
      }
    ),

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
      extraOptions: { service: 'sales' },
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Order' as const,
                id,
              })),
              { type: 'Order', id: 'LIST' },
            ]
          : [{ type: 'Order', id: 'LIST' }],
    }),

    getOrder: builder.query<APIResponse<Order>, string>({
      query: (orderId) => ({
        url: `/order/search/${orderId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'sales' },
      providesTags: (result, error, id) => [{ type: 'Order', id }],
    }),

    createOrder: builder.mutation<APIResponse<Order>, OrderCreationForm>({
      query: (data) => ({
        url: '/order/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'sales' },
      invalidatesTags: [{ type: 'Order', id: 'LIST' }],
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
      ],
    }),

    deleteOrder: builder.mutation<APIResponse<{ deleted: boolean }>, string>({
      query: (orderId) => ({
        url: `/order/delete/${orderId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, orderId) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
      ],
    }),

    approveOrder: builder.mutation<APIResponse<Order>, string>({
      query: (orderId) => ({
        url: `/order/manage/${orderId}/approve`,
        method: 'PATCH',
      }),
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, orderId) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
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
      extraOptions: { service: 'sales' },
      invalidatesTags: (result, error, { orderId }) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
      ],
    }),
  }),
});

// Export hooks for each endpoint
export const {
  // Customer hooks
  useGetCustomersQuery,
  useGetCustomerQuery,
  useCreateCustomerMutation,
  useUpdateCustomerMutation,
  useDeleteCustomerMutation,
  // Address hooks
  useCreateAddressMutation,
  useUpdateAddressMutation,
  useDeleteAddressMutation,
  useGetAddressesQuery,
  // Category hooks
  useGetCategoriesQuery,
  useGetCategoryQuery,
  useCreateCategoryMutation,
  useUpdateCategoryMutation,
  useDeleteCategoryMutation,
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
  // Product hooks
  useGetProductsQuery,
  useGetProductQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
  // Order hooks
  useGetOrdersQuery,
  useGetOrderQuery,
  useCreateOrderMutation,
  useUpdateOrderMutation,
  useDeleteOrderMutation,
  useAddProductToOrderMutation,
  useDeleteProductFromOrderMutation,
  useApproveOrderMutation,
  useCancelOrderMutation,
} = salesApi;
