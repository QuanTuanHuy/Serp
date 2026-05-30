/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile API transform helpers
 */

import { transformTimestampFields } from '@/lib/store/api/utils';
import type {
  FirstMileApiResponse,
  FirstMilePageResponse,
  FirstMilePaginatedData,
  HandoverManifest,
  HandoverManifestOrderItem,
  HubPostOfficeMapping,
  ImportHistory,
  ImportHistoryStatus,
  ImportType,
  SecondMileOrder,
} from '../types';

const readField = <T>(
  raw: Record<string, unknown>,
  snakeKey: string,
  camelKey: string
): T | undefined => {
  const value = raw[snakeKey] ?? raw[camelKey];
  return value as T | undefined;
};

export const normalizeHubPostOfficeMapping = (
  raw: unknown
): HubPostOfficeMapping => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    hubId: Number(readField(record, 'hub_id', 'hubId') ?? 0),
    postOfficeCode: String(
      readField(record, 'post_office_code', 'postOfficeCode') ?? ''
    ),
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
    tenantId: readField<number>(record, 'tenant_id', 'tenantId'),
  };
};

export const normalizeSecondMileOrder = (raw: unknown): SecondMileOrder => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    orderCode: readField<string>(record, 'order_code', 'orderCode'),
    customerOrderCode: readField<string>(
      record,
      'customer_order_code',
      'customerOrderCode'
    ),
    originPostOfficeCode: readField<string>(
      record,
      'origin_post_office_code',
      'originPostOfficeCode'
    ),
    destinationPostOfficeCode: readField<string>(
      record,
      'destination_post_office_code',
      'destinationPostOfficeCode'
    ),
    status: readField(record, 'status', 'status'),
    totalWeight: readField<number>(record, 'total_weight', 'totalWeight'),
    totalVolume: readField<number>(record, 'total_volume', 'totalVolume'),
  };
};

export const normalizeHandoverManifestOrder = (
  raw: unknown
): HandoverManifestOrderItem => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: readField<number>(record, 'id', 'id'),
    orderId: readField<number>(record, 'order_id', 'orderId'),
    orderCode: readField<string>(record, 'order_code', 'orderCode'),
    scanOutTime: readField<string>(record, 'scan_out_time', 'scanOutTime'),
    scanInTime: readField<string>(record, 'scan_in_time', 'scanInTime'),
  };
};

export const normalizeHandoverManifest = (raw: unknown): HandoverManifest => {
  const record = (raw ?? {}) as Record<string, unknown>;
  const ordersRaw = record.orders;
  const orders = Array.isArray(ordersRaw)
    ? ordersRaw.map(normalizeHandoverManifestOrder)
    : undefined;

  return {
    id: Number(record.id ?? 0),
    manifestCode: readField<string>(record, 'manifest_code', 'manifestCode'),
    originPostOfficeCode: readField<string>(
      record,
      'origin_post_office_code',
      'originPostOfficeCode'
    ),
    targetHubId: readField<number>(record, 'target_hub_id', 'targetHubId'),
    vehicleId: readField<number>(record, 'vehicle_id', 'vehicleId'),
    vehicleLicensePlate: readField<string>(
      record,
      'vehicle_license_plate',
      'vehicleLicensePlate'
    ),
    routeId: readField<number>(record, 'route_id', 'routeId'),
    routeCode: readField<string>(record, 'route_code', 'routeCode'),
    status: readField(record, 'status', 'status'),
    orders,
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
  };
};

export const normalizeHubPostOfficeMappingPage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<HubPostOfficeMapping>>
    | FirstMilePageResponse<HubPostOfficeMapping>
): FirstMilePaginatedData<HubPostOfficeMapping> => {
  const page = unwrapFirstMilePageResultOrRaw<HubPostOfficeMapping>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeHubPostOfficeMapping(item)),
  };
};

export const normalizeSecondMileOrderPage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<SecondMileOrder>>
    | FirstMilePageResponse<SecondMileOrder>
): FirstMilePaginatedData<SecondMileOrder> => {
  const page = unwrapFirstMilePageResultOrRaw<SecondMileOrder>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeSecondMileOrder(item)),
  };
};

export const normalizeHandoverManifestPage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<HandoverManifest>>
    | FirstMilePageResponse<HandoverManifest>
): FirstMilePaginatedData<HandoverManifest> => {
  const page = unwrapFirstMilePageResultOrRaw<HandoverManifest>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeHandoverManifest(item)),
  };
};

export const unwrapFirstMileResult = <T>(
  response: FirstMileApiResponse<T>
): T => {
  return transformTimestampFields((response?.result ?? null) as T);
};

export const unwrapFirstMileResultOrRaw = <T>(
  response: FirstMileApiResponse<T> | T
): T => {
  if (
    response &&
    typeof response === 'object' &&
    'result' in (response as Record<string, unknown>)
  ) {
    return transformTimestampFields(
      ((response as FirstMileApiResponse<T>).result ?? null) as T
    );
  }

  return transformTimestampFields(response as T);
};

export const unwrapFirstMilePageResult = <T>(
  response: FirstMileApiResponse<FirstMilePageResponse<T>>
): FirstMilePaginatedData<T> => {
  return unwrapFirstMilePageResultOrRaw(response);
};

/**
 * second-mile hub import returns {@link ImportHistoryResponse} without ApiResponse wrapper.
 */
export const normalizeSecondMileHubImportHistory = (
  raw: unknown
): ImportHistory => {
  const r = raw as Record<string, unknown>;
  return {
    id: Number(r.id ?? 0),
    file_id: String(r.file_id ?? r.fileId ?? ''),
    file_name: String(r.file_name ?? r.fileName ?? ''),
    status: (r.status ?? 'PENDING') as ImportHistoryStatus,
    total_records: Number(r.total_records ?? r.totalRecords ?? 0),
    success_records: Number(r.success_records ?? r.successRecords ?? 0),
    failed_records: Number(r.failed_records ?? r.failedRecords ?? 0),
    error_message: (r.error_message ?? r.errorMessage) as string | undefined,
    started_at: (r.started_at ?? r.startedAt) as string | undefined,
    finished_at: (r.finished_at ?? r.finishedAt) as string | undefined,
    type: (r.type ?? 'HUB') as ImportType,
  };
};

export const unwrapFirstMilePageResultOrRaw = <T>(
  response:
    | FirstMileApiResponse<FirstMilePageResponse<T>>
    | FirstMilePageResponse<T>
): FirstMilePaginatedData<T> => {
  const wrappedResponse = response as FirstMileApiResponse<
    FirstMilePageResponse<T>
  >;
  const pageData = wrappedResponse?.result
    ? wrappedResponse.result
    : (response as FirstMilePageResponse<T>);

  if (!pageData) {
    return {
      items: [],
      totalItems: 0,
      totalPages: 0,
      currentPage: 0,
      pageSize: 0,
      hasNext: false,
      hasPrevious: false,
    };
  }

  return {
    items: transformTimestampFields(pageData.items || []),
    totalItems: pageData.totalElements || 0,
    totalPages: pageData.totalPages || 0,
    currentPage: pageData.page || 0,
    pageSize: pageData.size || 0,
    hasNext: !!pageData.hasNext,
    hasPrevious: !!pageData.hasPrevious,
  };
};
