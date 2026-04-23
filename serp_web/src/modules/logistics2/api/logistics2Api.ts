/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 API endpoints
*/

import { api } from '@/lib/store/api';
import type {
  Address,
  AddDeliverySlipItemRequest,
  AssignVehicleShipperRequest,
  Category,
  CategoryFilters,
  CreateDeliveryPlanRequest,
  CreateDeliverySlipRequest,
  CreateVehicleRequest,
  Customer,
  CustomerFilters,
  DeliveryPlan,
  DeliveryPlanFilters,
  DeliverySlip,
  DeliverySlipFilters,
  Facility,
  FacilityFilters,
  GeneralResponse,
  InventoryItem,
  InventoryItemFilters,
  ListQueryParams,
  OutboundShipment,
  OutboundShipmentFilters,
  PageResponse,
  PaginationParams,
  RemoveDeliverySlipItemRequest,
  Route,
  RouteFilters,
  SaleOrder,
  SaleOrderFilters,
  UpdateDeliveryPlanRequest,
  UpdateDeliverySlipItemRequest,
  UpdateVehicleRequest,
  Vehicle,
  VehicleFilters,
  VehicleUsageFilters,
  VehicleShipper,
  VehicleShipperFilters,
} from '../types';

const LOGISTICS2_SERVICE = { service: 'logistics2' as const };

const buildPaginationParams = (pagination?: PaginationParams) => ({
  page: pagination?.page ?? 0,
  size: pagination?.size ?? 10,
  sortBy: pagination?.sortBy ?? 'createdStamp',
  sortDirection: pagination?.sortDirection ?? 'desc',
});

const buildListParams = <TFilters extends object>(
  filters?: TFilters,
  pagination?: PaginationParams
) => ({
  ...buildPaginationParams(pagination),
  ...(filters || {}),
});

export const logistics2Api = api.injectEndpoints({
  endpoints: (builder) => ({
    // Address endpoints
    getAddressesByEntityId: builder.query<GeneralResponse<Address[]>, string>({
      query: (entityId) => ({
        url: `/address/search/by-entity/${entityId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    getAddressDetail: builder.query<GeneralResponse<Address>, string>({
      query: (addressId) => ({
        url: `/address/search/${addressId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    // Category endpoints
    getCategories: builder.query<
      GeneralResponse<PageResponse<Category>>,
      ListQueryParams<CategoryFilters> | void
    >({
      query: (args) => ({
        url: '/category/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    getCategoryDetail: builder.query<GeneralResponse<Category>, string>({
      query: (categoryId) => ({
        url: `/category/search/${categoryId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    // Customer endpoints
    getCustomers: builder.query<
      GeneralResponse<PageResponse<Customer>>,
      ListQueryParams<CustomerFilters> | void
    >({
      query: (args) => ({
        url: '/customer/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    getCustomerDetail: builder.query<GeneralResponse<Customer>, string>({
      query: (customerId) => ({
        url: `/customer/search/${customerId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    // Facility endpoints
    getFacilities: builder.query<
      GeneralResponse<PageResponse<Facility>>,
      ListQueryParams<FacilityFilters> | void
    >({
      query: (args) => ({
        url: '/facility/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    getFacilityDetail: builder.query<GeneralResponse<Facility>, string>({
      query: (facilityId) => ({
        url: `/facility/search/${facilityId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    // Inventory item endpoints
    getInventoryItems: builder.query<
      GeneralResponse<PageResponse<InventoryItem>>,
      ListQueryParams<InventoryItemFilters> | void
    >({
      query: (args) => ({
        url: '/inventory-item/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    getInventoryItemDetail: builder.query<
      GeneralResponse<InventoryItem>,
      string
    >({
      query: (inventoryItemId) => ({
        url: `/inventory-item/search/${inventoryItemId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    // Sale order endpoints
    getSaleOrders: builder.query<
      GeneralResponse<PageResponse<SaleOrder>>,
      ListQueryParams<SaleOrderFilters> | void
    >({
      query: (args) => ({
        url: '/sale-order/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    getSaleOrderDetail: builder.query<GeneralResponse<SaleOrder>, string>({
      query: (orderId) => ({
        url: `/sale-order/search/${orderId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
    }),

    // Delivery plan endpoints
    getDeliveryPlans: builder.query<
      GeneralResponse<PageResponse<DeliveryPlan>>,
      ListQueryParams<DeliveryPlanFilters> | void
    >({
      query: (args) => ({
        url: '/delivery-plans/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics2/DeliveryPlan' as const,
                id,
              })),
              { type: 'logistics2/DeliveryPlan', id: 'LIST' },
            ]
          : [{ type: 'logistics2/DeliveryPlan', id: 'LIST' }],
    }),

    getDeliveryPlanDetail: builder.query<GeneralResponse<DeliveryPlan>, string>(
      {
        query: (planId) => ({
          url: `/delivery-plans/search/${planId}`,
          method: 'GET',
        }),
        extraOptions: LOGISTICS2_SERVICE,
        providesTags: (result, error, planId) => [
          { type: 'logistics2/DeliveryPlan', id: planId },
        ],
      }
    ),

    createDeliveryPlan: builder.mutation<
      GeneralResponse<null>,
      CreateDeliveryPlanRequest
    >({
      query: (data) => ({
        url: '/delivery-plans/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: [{ type: 'logistics2/DeliveryPlan', id: 'LIST' }],
    }),

    updateDeliveryPlan: builder.mutation<
      GeneralResponse<null>,
      UpdateDeliveryPlanRequest
    >({
      query: ({ planId, data }) => ({
        url: `/delivery-plans/update/${planId}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, { planId }) => [
        { type: 'logistics2/DeliveryPlan', id: planId },
        { type: 'logistics2/DeliveryPlan', id: 'LIST' },
      ],
    }),

    optimizeDeliveryPlan: builder.mutation<GeneralResponse<null>, string>({
      query: (planId) => ({
        url: `/delivery-plans/optimize/${planId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, planId) => [
        { type: 'logistics2/DeliveryPlan', id: planId },
        { type: 'logistics2/DeliveryPlan', id: 'LIST' },
      ],
    }),

    deleteDeliveryPlan: builder.mutation<GeneralResponse<null>, string>({
      query: (planId) => ({
        url: `/delivery-plans/delete/${planId}`,
        method: 'DELETE',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, planId) => [
        { type: 'logistics2/DeliveryPlan', id: planId },
        { type: 'logistics2/DeliveryPlan', id: 'LIST' },
      ],
    }),

    // Delivery slip endpoints
    getDeliverySlips: builder.query<
      GeneralResponse<PageResponse<DeliverySlip>>,
      ListQueryParams<DeliverySlipFilters> | void
    >({
      query: (args) => ({
        url: '/delivery-slips/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics2/DeliverySlip' as const,
                id,
              })),
              { type: 'logistics2/DeliverySlip', id: 'LIST' },
            ]
          : [{ type: 'logistics2/DeliverySlip', id: 'LIST' }],
    }),

    getDeliverySlipDetail: builder.query<GeneralResponse<DeliverySlip>, string>(
      {
        query: (slipId) => ({
          url: `/delivery-slips/search/${slipId}`,
          method: 'GET',
        }),
        extraOptions: LOGISTICS2_SERVICE,
        providesTags: (result, error, slipId) => [
          { type: 'logistics2/DeliverySlip', id: slipId },
        ],
      }
    ),

    createDeliverySlip: builder.mutation<
      GeneralResponse<null>,
      CreateDeliverySlipRequest
    >({
      query: (data) => ({
        url: '/delivery-slips/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: [{ type: 'logistics2/DeliverySlip', id: 'LIST' }],
    }),

    returnDeliverySlip: builder.mutation<GeneralResponse<null>, string>({
      query: (slipId) => ({
        url: `/delivery-slips/return/${slipId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, slipId) => [
        { type: 'logistics2/DeliverySlip', id: slipId },
        { type: 'logistics2/DeliverySlip', id: 'LIST' },
      ],
    }),

    deleteDeliverySlip: builder.mutation<GeneralResponse<null>, string>({
      query: (slipId) => ({
        url: `/delivery-slips/delete/${slipId}`,
        method: 'DELETE',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, slipId) => [
        { type: 'logistics2/DeliverySlip', id: slipId },
        { type: 'logistics2/DeliverySlip', id: 'LIST' },
      ],
    }),

    addDeliverySlipItem: builder.mutation<
      GeneralResponse<null>,
      AddDeliverySlipItemRequest
    >({
      query: ({ slipId, data }) => ({
        url: `/delivery-slips/update/${slipId}/add`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, { slipId }) => [
        { type: 'logistics2/DeliverySlip', id: slipId },
        { type: 'logistics2/DeliverySlip', id: 'LIST' },
      ],
    }),

    updateDeliverySlipItem: builder.mutation<
      GeneralResponse<null>,
      UpdateDeliverySlipItemRequest
    >({
      query: ({ slipId, itemId, data }) => ({
        url: `/delivery-slips/update/${slipId}/update/${itemId}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, { slipId }) => [
        { type: 'logistics2/DeliverySlip', id: slipId },
        { type: 'logistics2/DeliverySlip', id: 'LIST' },
      ],
    }),

    removeDeliverySlipItem: builder.mutation<
      GeneralResponse<null>,
      RemoveDeliverySlipItemRequest
    >({
      query: ({ slipId, itemId }) => ({
        url: `/delivery-slips/update/${slipId}/remove/${itemId}`,
        method: 'DELETE',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, { slipId }) => [
        { type: 'logistics2/DeliverySlip', id: slipId },
        { type: 'logistics2/DeliverySlip', id: 'LIST' },
      ],
    }),

    // Outbound shipment endpoints
    getOutboundShipments: builder.query<
      GeneralResponse<PageResponse<OutboundShipment>>,
      ListQueryParams<OutboundShipmentFilters> | void
    >({
      query: (args) => ({
        url: '/outbound-shipment/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics2/OutboundShipment' as const,
                id,
              })),
              { type: 'logistics2/OutboundShipment', id: 'LIST' },
            ]
          : [{ type: 'logistics2/OutboundShipment', id: 'LIST' }],
    }),

    getOutboundShipmentDetail: builder.query<
      GeneralResponse<OutboundShipment>,
      string
    >({
      query: (shipmentId) => ({
        url: `/outbound-shipment/search/${shipmentId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result, error, shipmentId) => [
        { type: 'logistics2/OutboundShipment', id: shipmentId },
      ],
    }),

    // Route endpoints
    getRoutes: builder.query<
      GeneralResponse<PageResponse<Route>>,
      ListQueryParams<RouteFilters> | void
    >({
      query: (args) => ({
        url: '/routes/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics2/Route' as const,
                id,
              })),
              { type: 'logistics2/Route', id: 'LIST' },
            ]
          : [{ type: 'logistics2/Route', id: 'LIST' }],
    }),

    getRouteDetail: builder.query<GeneralResponse<Route>, string>({
      query: (routeId) => ({
        url: `/routes/search/${routeId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result, error, routeId) => [
        { type: 'logistics2/Route', id: routeId },
      ],
    }),

    selectRouteForDelivery: builder.mutation<GeneralResponse<null>, string>({
      query: (routeId) => ({
        url: `/routes/select/${routeId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, routeId) => [
        { type: 'logistics2/Route', id: routeId },
        { type: 'logistics2/Route', id: 'LIST' },
      ],
    }),

    completeRouteStop: builder.mutation<GeneralResponse<null>, string>({
      query: (routeStopId) => ({
        url: `/routes/complete/${routeStopId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: [{ type: 'logistics2/Route', id: 'LIST' }],
    }),

    abortRouteStop: builder.mutation<GeneralResponse<null>, string>({
      query: (routeStopId) => ({
        url: `/routes/abort/${routeStopId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: [{ type: 'logistics2/Route', id: 'LIST' }],
    }),

    // Vehicle endpoints
    getVehicles: builder.query<
      GeneralResponse<PageResponse<Vehicle>>,
      ListQueryParams<VehicleFilters> | void
    >({
      query: (args) => ({
        url: '/vehicles/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics2/Vehicle' as const,
                id,
              })),
              { type: 'logistics2/Vehicle', id: 'LIST' },
            ]
          : [{ type: 'logistics2/Vehicle', id: 'LIST' }],
    }),

    getVehiclesForUsage: builder.query<
      GeneralResponse<PageResponse<Vehicle>>,
      ListQueryParams<VehicleUsageFilters> | void
    >({
      query: (args) => ({
        url: '/vehicles/search-for-usage',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics2/Vehicle' as const,
                id,
              })),
              { type: 'logistics2/Vehicle', id: 'LIST' },
            ]
          : [{ type: 'logistics2/Vehicle', id: 'LIST' }],
    }),

    getVehicleDetail: builder.query<GeneralResponse<Vehicle>, string>({
      query: (vehicleId) => ({
        url: `/vehicles/search/${vehicleId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result, error, vehicleId) => [
        { type: 'logistics2/Vehicle', id: vehicleId },
      ],
    }),

    createVehicle: builder.mutation<
      GeneralResponse<null>,
      CreateVehicleRequest
    >({
      query: (data) => ({
        url: '/vehicles/create',
        method: 'POST',
        body: data,
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: [{ type: 'logistics2/Vehicle', id: 'LIST' }],
    }),

    updateVehicle: builder.mutation<
      GeneralResponse<null>,
      UpdateVehicleRequest
    >({
      query: ({ vehicleId, data }) => ({
        url: `/vehicles/update/${vehicleId}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, { vehicleId }) => [
        { type: 'logistics2/Vehicle', id: vehicleId },
        { type: 'logistics2/Vehicle', id: 'LIST' },
      ],
    }),

    activateVehicle: builder.mutation<GeneralResponse<null>, string>({
      query: (vehicleId) => ({
        url: `/vehicles/update/active/${vehicleId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, vehicleId) => [
        { type: 'logistics2/Vehicle', id: vehicleId },
        { type: 'logistics2/Vehicle', id: 'LIST' },
      ],
    }),

    deactivateVehicle: builder.mutation<GeneralResponse<null>, string>({
      query: (vehicleId) => ({
        url: `/vehicles/update/deactive/${vehicleId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, vehicleId) => [
        { type: 'logistics2/Vehicle', id: vehicleId },
        { type: 'logistics2/Vehicle', id: 'LIST' },
      ],
    }),

    // Vehicle-shipper endpoints
    getVehicleShippers: builder.query<
      GeneralResponse<PageResponse<VehicleShipper>>,
      ListQueryParams<VehicleShipperFilters> | void
    >({
      query: (args) => ({
        url: '/vehicle-shippers/search',
        method: 'GET',
        params: buildListParams(args?.filters, args?.pagination),
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'logistics2/VehicleShipper' as const,
                id,
              })),
              { type: 'logistics2/VehicleShipper', id: 'LIST' },
            ]
          : [{ type: 'logistics2/VehicleShipper', id: 'LIST' }],
    }),

    getVehicleShipperDetail: builder.query<
      GeneralResponse<VehicleShipper>,
      string
    >({
      query: (vehicleShipperId) => ({
        url: `/vehicle-shippers/search/${vehicleShipperId}`,
        method: 'GET',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      providesTags: (result, error, vehicleShipperId) => [
        { type: 'logistics2/VehicleShipper', id: vehicleShipperId },
      ],
    }),

    assignVehicleShipper: builder.mutation<
      GeneralResponse<null>,
      AssignVehicleShipperRequest
    >({
      query: ({ vehicleId, workingDate, workingDay }) => {
        const normalizedWorkingDate = workingDate || workingDay;

        return {
          url: '/vehicle-shippers/assign',
          method: 'POST',
          body: {
            vehicleId,
            ...(normalizedWorkingDate
              ? { workingDate: normalizedWorkingDate }
              : {}),
            ...(workingDay ? { workingDay } : {}),
          },
        };
      },
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: [{ type: 'logistics2/VehicleShipper', id: 'LIST' }],
    }),

    requestCancelVehicleShipper: builder.mutation<
      GeneralResponse<null>,
      string
    >({
      query: (vehicleShipperId) => ({
        url: `/vehicle-shippers/request-cancel/${vehicleShipperId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, vehicleShipperId) => [
        { type: 'logistics2/VehicleShipper', id: vehicleShipperId },
        { type: 'logistics2/VehicleShipper', id: 'LIST' },
      ],
    }),

    cancelVehicleShipper: builder.mutation<GeneralResponse<null>, string>({
      query: (vehicleShipperId) => ({
        url: `/vehicle-shippers/cancel/${vehicleShipperId}`,
        method: 'PUT',
      }),
      extraOptions: LOGISTICS2_SERVICE,
      invalidatesTags: (result, error, vehicleShipperId) => [
        { type: 'logistics2/VehicleShipper', id: vehicleShipperId },
        { type: 'logistics2/VehicleShipper', id: 'LIST' },
      ],
    }),
  }),
});

export const {
  useGetAddressesByEntityIdQuery,
  useGetAddressDetailQuery,
  useGetCategoriesQuery,
  useGetCategoryDetailQuery,
  useGetCustomersQuery,
  useGetCustomerDetailQuery,
  useGetFacilitiesQuery,
  useGetFacilityDetailQuery,
  useGetInventoryItemsQuery,
  useGetInventoryItemDetailQuery,
  useGetSaleOrdersQuery,
  useGetSaleOrderDetailQuery,
  useGetDeliveryPlansQuery,
  useGetDeliveryPlanDetailQuery,
  useCreateDeliveryPlanMutation,
  useUpdateDeliveryPlanMutation,
  useOptimizeDeliveryPlanMutation,
  useDeleteDeliveryPlanMutation,
  useGetDeliverySlipsQuery,
  useGetDeliverySlipDetailQuery,
  useCreateDeliverySlipMutation,
  useReturnDeliverySlipMutation,
  useDeleteDeliverySlipMutation,
  useAddDeliverySlipItemMutation,
  useUpdateDeliverySlipItemMutation,
  useRemoveDeliverySlipItemMutation,
  useGetOutboundShipmentsQuery,
  useGetOutboundShipmentDetailQuery,
  useGetRoutesQuery,
  useGetRouteDetailQuery,
  useSelectRouteForDeliveryMutation,
  useCompleteRouteStopMutation,
  useAbortRouteStopMutation,
  useGetVehiclesQuery,
  useGetVehiclesForUsageQuery,
  useGetVehicleDetailQuery,
  useCreateVehicleMutation,
  useUpdateVehicleMutation,
  useActivateVehicleMutation,
  useDeactivateVehicleMutation,
  useGetVehicleShippersQuery,
  useGetVehicleShipperDetailQuery,
  useAssignVehicleShipperMutation,
  useRequestCancelVehicleShipperMutation,
  useCancelVehicleShipperMutation,
} = logistics2Api;

export const useGetOrdersQuery = useGetSaleOrdersQuery;
export const useGetOrderDetailQuery = useGetSaleOrderDetailQuery;
