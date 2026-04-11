/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile module types
 */

export interface FirstMileApiResponse<T> {
  code: number;
  message: string;
  detail?: string;
  path?: string;
  timestamp?: string;
  result?: T;
}

export interface FirstMilePageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface FirstMilePaginatedData<T> {
  items: T[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export type PostOfficeStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'MAINTENANCE'
  | 'SUSPENDED';
export type ImportType = 'ORDER' | 'POST_OFFICE';
export type ImportHistoryStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'PARTIAL_SUCCESS'
  | 'FAILED';

export interface PostOffice {
  id: number;
  code: string;
  name: string;
  provinceCode: string;
  wardCode: string;
  addressDetail: string;
  phoneNumber?: string;
  imageUrl?: string;
  operationalStartDate?: string;
  operationalEndDate?: string;
  workingStartTime?: string;
  workingEndTime?: string;
  serviceRadiusM: number;
  dailyCapacity?: number;
  currentLoad?: number;
  priority?: number;
  latitude?: number;
  longitude?: number;
  status: PostOfficeStatus;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface PostOfficeListFilters {
  keyword?: string;
  code?: string;
  name?: string;
  provinceCode?: string;
  wardCode?: string;
  status?: PostOfficeStatus;
  hasLocation?: boolean;
  minServiceRadiusM?: number;
  maxServiceRadiusM?: number;
  minDailyCapacity?: number;
  maxDailyCapacity?: number;
  minCurrentLoad?: number;
  maxCurrentLoad?: number;
  minPriority?: number;
  maxPriority?: number;
}

export interface CreatePostOfficeRequest {
  code: string;
  name: string;
  province_code: string;
  ward_code: string;
  address_detail: string;
  phone_number?: string;
  operational_start_date?: string;
  operational_end_date?: string;
  working_start_time?: string;
  working_end_time?: string;
  service_radius_m: number;
  daily_capacity?: number;
  current_load?: number;
  priority?: number;
  latitude?: number;
  longitude?: number;
  status: PostOfficeStatus;
}

export type UpdatePostOfficeRequest = CreatePostOfficeRequest;

export interface ProductType {
  id: number;
  code: string;
  name: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface CreateProductTypeRequest {
  code: string;
  name: string;
  is_active?: boolean;
}

export type UpdateProductTypeRequest = CreateProductTypeRequest;

export interface ImportHistory {
  id: number;
  file_id: string;
  file_name: string;
  status: ImportHistoryStatus;
  total_records: number;
  success_records: number;
  failed_records: number;
  error_message?: string;
  started_at?: string;
  finished_at?: string;
  type: ImportType;
}

export interface ValidateImportFileResponse<T> {
  header: Record<string, string>;
  file_id: string;
  is_success: boolean;
  error_message?: string;
  type: number;
  data: T[];
}

export interface PostOfficeImportItem {
  name?: string;
  code?: string;
  province_code?: string;
  ward_code?: string;
  address_detail?: string;
  phone_number?: string;
  operational_start_date?: string;
  operational_end_date?: string;
  working_start_time?: string;
  working_end_time?: string;
  service_radius_m?: number;
  status?: PostOfficeStatus;
  source_rows?: number[];
}

export interface OrderImportProductItem {
  name?: string;
  value?: number;
  quantity?: number;
  weight_gram?: number;
  product_type_id?: number;
  product_type_code?: string;
  product_type_name?: string;
}

export interface OrderImportItem {
  customer_order_code?: string;
  sender_name?: string;
  sender_phone?: string;
  sender_province_code?: string;
  sender_ward_code?: string;
  sender_address_detail?: string;
  receiver_name?: string;
  receiver_phone?: string;
  receiver_province_code?: string;
  receiver_ward_code?: string;
  receiver_address_detail?: string;
  order_product_category?: string;
  order_type?: string;
  note?: string;
  pickup_date?: string;
  pickup_request_time?: string;
  pickup_time_start?: string;
  pickup_time_end?: string;
  delivery_request_time?: string;
  is_cod?: boolean;
  dimension_length_cm?: number;
  dimension_width_cm?: number;
  dimension_height_cm?: number;
  total_volume_m3?: number;
  fee_payer?: string;
  source_rows?: number[];
  products?: OrderImportProductItem[];
}

export interface Province {
  provinceCode: string;
  name: string;
  shortName?: string;
  code?: string;
  placeType?: string;
  countryCode?: string;
}

export interface Ward {
  wardCode: string;
  name: string;
  provinceCode: string;
}

export interface GeocodeAddressRequest {
  address: string;
}

export interface GeocodeAddressResponse {
  address: string;
  latitude: number;
  longitude: number;
}

export interface PostOfficeGeocodeBatchResponse {
  requestedBatch: number;
  processed: number;
  updated: number;
  skipped: number;
}
