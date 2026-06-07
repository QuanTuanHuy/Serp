/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Last-mile delivery API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  ConfirmDeliveryFailureRequest,
  ConfirmDeliveryPaymentRequest,
  ConfirmDeliveryRequest,
  CreateDeliveryManifestRequest,
  DeliveryPaymentConfirmResponse,
  DeliveryPaymentInitResponse,
  DeliveryManifestListFilters,
  DeliveryManifestResponse,
  FirstMileApiResponse,
  InboundOrderResponse,
  ReturnToSenderRequest,
  SortInboundOrdersRequest,
} from '../types';
import { unwrapFirstMileResultOrRaw } from './transforms';

const FIRST_MILE_SERVICE = { service: 'first-mile' as const };

export const lastMileApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // ─── Inbound Sorting ───────────────────────────────────────────────
    getInboundOrders: builder.query<
      InboundOrderResponse[],
      { postOfficeCode: string; status?: string }
    >({
      query: ({ postOfficeCode, status }) => ({
        url: '/inbound-orders',
        method: 'GET',
        params: {
          post_office_code: postOfficeCode,
          ...(status ? { status } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<InboundOrderResponse[]>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    confirmInboundOrders: builder.mutation<
      InboundOrderResponse[],
      SortInboundOrdersRequest
    >({
      query: (body) => ({
        url: '/inbound-orders/confirm',
        method: 'POST',
        body: {
          post_office_code: body.postOfficeCode,
          order_codes: body.orderCodes,
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<InboundOrderResponse[]>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    // ─── Delivery Manifests ────────────────────────────────────────────
    getDeliveryManifests: builder.query<
      DeliveryManifestResponse[],
      DeliveryManifestListFilters
    >({
      query: ({ postOfficeCode, status, date }) => ({
        url: '/delivery-manifests',
        method: 'GET',
        params: {
          ...(postOfficeCode ? { post_office_code: postOfficeCode } : {}),
          ...(status ? { status } : {}),
          ...(date ? { date } : {}),
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryManifestResponse[]>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    getDeliveryManifestDetail: builder.query<
      DeliveryManifestResponse,
      { id: number }
    >({
      query: ({ id }) => ({
        url: `/delivery-manifests/${id}`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryManifestResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    createDeliveryManifest: builder.mutation<
      DeliveryManifestResponse,
      CreateDeliveryManifestRequest
    >({
      query: (body) => ({
        url: '/delivery-manifests',
        method: 'POST',
        body: {
          post_office_code: body.postOfficeCode,
          courier_id: body.courierId,
          vehicle_id: body.vehicleId,
          planned_date: body.plannedDate,
          planned_departure_at: body.plannedDepartureAt,
          order_codes: body.orderCodes,
          note: body.note,
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryManifestResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    startDelivery: builder.mutation<DeliveryManifestResponse, { id: number }>({
      query: ({ id }) => ({
        url: `/delivery-manifests/${id}/start`,
        method: 'POST',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryManifestResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    initiateDeliveryPayment: builder.mutation<
      DeliveryPaymentInitResponse,
      { manifestId: number; orderCode: string }
    >({
      query: ({ manifestId, orderCode }) => ({
        url: `/delivery-manifests/${manifestId}/orders/${orderCode}/payment/initiate`,
        method: 'POST',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryPaymentInitResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    confirmDeliveryPayment: builder.mutation<
      DeliveryPaymentConfirmResponse,
      {
        manifestId: number;
        orderCode: string;
        body: ConfirmDeliveryPaymentRequest;
      }
    >({
      query: ({ manifestId, orderCode, body }) => ({
        url: `/delivery-manifests/${manifestId}/orders/${orderCode}/payment/confirm`,
        method: 'POST',
        body,
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryPaymentConfirmResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    confirmDelivered: builder.mutation<
      DeliveryManifestResponse,
      { manifestId: number; orderCode: string; body: ConfirmDeliveryRequest }
    >({
      query: ({ manifestId, orderCode, body }) => {
        const formData = new FormData();
        formData.append('latitude', String(body.latitude));
        formData.append('longitude', String(body.longitude));
        formData.append('photo', body.photo);
        if (body.codCollected !== undefined) {
          formData.append('cod_collected', String(body.codCollected));
        }
        if (body.shippingFeeCollected !== undefined) {
          formData.append(
            'shipping_fee_collected',
            String(body.shippingFeeCollected)
          );
        }
        if (body.note) {
          formData.append('note', body.note);
        }

        return {
          url: `/delivery-manifests/${manifestId}/orders/${orderCode}/delivered`,
          method: 'POST',
          body: formData,
        };
      },
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryManifestResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    confirmDeliveryFailed: builder.mutation<
      DeliveryManifestResponse,
      {
        manifestId: number;
        orderCode: string;
        body: ConfirmDeliveryFailureRequest;
      }
    >({
      query: ({ manifestId, orderCode, body }) => ({
        url: `/delivery-manifests/${manifestId}/orders/${orderCode}/failed`,
        method: 'POST',
        body: {
          failure_reason: body.failureReason,
          note: body.note,
          current_lat: body.currentLat,
          current_lng: body.currentLng,
        },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryManifestResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    confirmReturn: builder.mutation<
      DeliveryManifestResponse,
      { manifestId: number; orderCode: string; body: ReturnToSenderRequest }
    >({
      query: ({ manifestId, orderCode, body }) => ({
        url: `/delivery-manifests/${manifestId}/orders/${orderCode}/return`,
        method: 'POST',
        body: { note: body.note },
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryManifestResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),

    getFinancialSummary: builder.query<
      DeliveryManifestResponse,
      { manifestId: number }
    >({
      query: ({ manifestId }) => ({
        url: `/delivery-manifests/${manifestId}/financial-summary`,
        method: 'GET',
      }),
      extraOptions: FIRST_MILE_SERVICE,
      transformResponse: (
        response: FirstMileApiResponse<DeliveryManifestResponse>
      ) => unwrapFirstMileResultOrRaw(response),
    }),
  }),
});

export const {
  useGetInboundOrdersQuery,
  useConfirmInboundOrdersMutation,
  useGetDeliveryManifestsQuery,
  useGetDeliveryManifestDetailQuery,
  useCreateDeliveryManifestMutation,
  useStartDeliveryMutation,
  useInitiateDeliveryPaymentMutation,
  useConfirmDeliveryPaymentMutation,
  useConfirmDeliveredMutation,
  useConfirmDeliveryFailedMutation,
  useConfirmReturnMutation,
  useGetFinancialSummaryQuery,
} = lastMileApi;
