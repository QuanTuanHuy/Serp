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
export type ImportType = 'ORDER' | 'POST_OFFICE' | 'VEHICLE';
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

export type PostOfficeStaffRole = 'COURIER' | 'MANAGER';

export type PostOfficeStaffStatus = 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE';

export interface PostOfficeStaff {
  id: number;
  code?: string;
  fullName?: string;
  phoneNumber?: string;
  email?: string;
  avatarUrl?: string;
  role?: PostOfficeStaffRole;
  status?: PostOfficeStaffStatus;
  hireDate?: string;
  maxDailyStops?: number;
  maxDailyParcels?: number;
  notes?: string;
  userId?: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export type VehicleStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'MAINTENANCE'
  | 'IN_USE'
  | 'FULL';

export type VehicleType = 'BIKE' | 'TRUCK';

export interface Vehicle {
  id: number;
  licensePlate: string;
  maxWeight?: number;
  maxVolume?: number;
  imageUrl?: string;
  postOfficeId?: number;
  postOfficeCode?: string;
  postOfficeName?: string;
  postOfficeStaffId?: number;
  status: VehicleStatus;
  vehicleType: VehicleType;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface CreateVehicleRequest {
  license_plate: string;
  max_weight?: number;
  max_volume?: number;
  post_office_id?: number;
  post_office_staff_id?: number;
  status: VehicleStatus;
  vehicle_type: VehicleType;
}

export type UpdateVehicleRequest = CreateVehicleRequest;

export interface VehicleImportItem {
  license_plate?: string;
  max_weight?: number;
  max_volume?: number;
  post_office_id?: number;
  post_office_code?: string;
  post_office_name?: string;
  post_office_staff_id?: number;
  post_office_staff_code?: string;
  post_office_staff_name?: string;
  vehicle_type?: VehicleType;
  status?: VehicleStatus;
  source_rows?: number[];
}

export type FirstMileOrderStatus =
  | 'CREATED'
  | 'ASSIGNED_TO_PICKUP'
  | 'PICKING_UP'
  | 'PICKUP_FAILED'
  | 'PICKED_UP'
  | 'AT_ORIGIN_POST_OFFICE'
  | 'CANCELLED'
  | 'LOST_OR_DAMAGED';

export type FirstMileDeliveryRequestTime =
  | 'FULL_DAY'
  | 'MORNING'
  | 'AFTERNOON'
  | 'SUNDAY'
  | 'HOLIDAY'
  | 'BUSINESS_HOURS';

export type FirstMileOrderType = 'EXPRESS_ORDER' | 'STANDARD_ORDER';

export type FirstMileFeePayer = 'SENDER' | 'RECEIVER';

export type FirstMilePaymentStatus = 'UNPAID' | 'PAID';

export type FirstMileOrderProductCategory =
  | 'HIGH_VALUE'
  | 'FRAGILE'
  | 'SOLID'
  | 'OVERSIZED'
  | 'LIQUID'
  | 'MAGNETIC_BATTERY';

export interface FirstMileOrderProductItem {
  id?: number;
  name?: string;
  value?: number;
  quantity?: number;
  weight?: number;
  productTypeId?: number;
  productTypeCode?: string;
  productTypeName?: string;
}

export interface FirstMileOrderDetail {
  id: number;
  orderCode: string;
  customerOrderCode?: string;
  status: FirstMileOrderStatus;
  isConfirm?: boolean;
  senderName?: string;
  senderPhone?: string;
  senderProvinceCode?: string;
  senderWardCode?: string;
  senderAddressDetail?: string;
  senderLatitude?: number;
  senderLongitude?: number;
  receiverName?: string;
  receiverPhone?: string;
  receiverProvinceCode?: string;
  receiverWardCode?: string;
  receiverAddressDetail?: string;
  receiverLatitude?: number;
  receiverLongitude?: number;
  pickupTimeStart?: string;
  pickupTimeEnd?: string;
  deliveryRequestTime?: FirstMileDeliveryRequestTime;
  orderProductCategory?: FirstMileOrderProductCategory;
  orderType?: FirstMileOrderType;
  feePayer?: FirstMileFeePayer;
  paymentStatus?: FirstMilePaymentStatus;
  codAmount?: number;
  totalWeight?: number;
  totalValue?: number;
  totalVolume?: number;
  originPostOfficeCode?: string;
  destinationPostOfficeCode?: string;
  note?: string;
  products?: FirstMileOrderProductItem[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface FirstMileOrderListFilters {
  keyword?: string;
  orderCode?: string;
  customerOrderCode?: string;
  senderPhone?: string;
  receiverPhone?: string;
  originPostOfficeCode?: string;
  destinationPostOfficeCode?: string;
  status?: FirstMileOrderStatus;
  isConfirm?: boolean;
  createdFrom?: string;
  createdTo?: string;
  pickupFrom?: string;
  pickupTo?: string;
}

export interface CreateOrderProductItemRequest {
  name: string;
  value: number;
  quantity: number;
  weight_gram: number;
  product_type_id: number;
}

export interface CreateOrderRequest {
  customer_order_code: string;
  sender_name: string;
  sender_phone: string;
  sender_province_code: string;
  sender_ward_code: string;
  sender_address_detail: string;
  sender_latitude: number;
  sender_longitude: number;
  receiver_name: string;
  receiver_phone: string;
  receiver_province_code: string;
  receiver_ward_code: string;
  receiver_address_detail: string;
  receiver_latitude: number;
  receiver_longitude: number;
  pickup_time_start?: string;
  pickup_time_end?: string;
  delivery_request_time: FirstMileDeliveryRequestTime;
  order_product_category?: FirstMileOrderProductCategory;
  order_type: FirstMileOrderType;
  fee_payer: FirstMileFeePayer;
  is_cod?: boolean;
  dimension_length_cm?: number;
  dimension_width_cm?: number;
  dimension_height_cm?: number;
  total_volume_m3?: number;
  note?: string;
  products?: CreateOrderProductItemRequest[];
}

export type UpdateOrderRequest = CreateOrderRequest;

export interface CancelOrderRequest {
  cancel_reason?: string;
}

export interface OrderConfirmationOriginPostOffice {
  id: number;
  code: string;
  name: string;
  currentLoad?: number;
  dailyCapacity?: number;
}

export interface OrderConfirmationResponse {
  orderId: number;
  orderCode: string;
  customerOrderCode?: string;
  status: FirstMileOrderStatus;
  alreadyConfirmed: boolean;
  originPostOffice?: OrderConfirmationOriginPostOffice | null;
}

export type PickupShift = 'MORNING' | 'AFTERNOON' | 'EVENING';

export interface OptimizePickupPlanRequest {
  post_office_id: number;
  planning_start_time?: string;
  planning_end_time?: string;
  courier_ids?: number[];
  candidate_statuses?: FirstMileOrderStatus[];
  vehicle?: string;
  order_limit?: number;
  average_speed_kmph?: number;
  service_minutes_per_stop?: number;
  max_iterations?: number;
  max_runtime_millis?: number;
  destroy_rate?: number;
  initial_temperature?: number;
  cooling_rate?: number;
  allow_lateness?: boolean;
  enforce_planning_end?: boolean;
  enforce_capacity?: boolean;
  distance_weight?: number;
  lateness_weight?: number;
  unassigned_penalty?: number;
  used_route_penalty?: number;
}

export interface AutoAssignPickupPlanRequest {
  post_office_id: number;
  shift: PickupShift;
  trip_date?: string;
  planning_start_time?: string;
  planning_end_time?: string;
  courier_ids?: number[];
  candidate_statuses?: FirstMileOrderStatus[];
  vehicle?: string;
  order_limit?: number;
  average_speed_kmph?: number;
  service_minutes_per_stop?: number;
  max_iterations?: number;
  max_runtime_millis?: number;
  destroy_rate?: number;
  initial_temperature?: number;
  cooling_rate?: number;
  allow_lateness?: boolean;
  enforce_planning_end?: boolean;
  enforce_capacity?: boolean;
  distance_weight?: number;
  lateness_weight?: number;
  unassigned_penalty?: number;
  used_route_penalty?: number;
}

export interface ManualAssignPickupOrdersRequest {
  post_office_id: number;
  courier_staff_id: number;
  order_ids: number[];
  shift: PickupShift;
  trip_date?: string;
  planning_start_time?: string;
  planning_end_time?: string;
  vehicle?: string;
  average_speed_kmph?: number;
  service_minutes_per_stop?: number;
  allow_lateness?: boolean;
  enforce_planning_end?: boolean;
  enforce_capacity?: boolean;
}

export interface PickupOptimizationStop {
  sequence: number;
  orderId: number;
  orderCode?: string;
  customerOrderCode?: string;
  senderName?: string;
  senderPhone?: string;
  latitude?: number;
  longitude?: number;
  pickupTimeStart?: string;
  pickupTimeEnd?: string;
  arrivalTime?: string;
  startServiceTime?: string;
  departureTime?: string;
  distanceFromPreviousKm?: number;
  travelMinutes?: number;
  latenessMinutes?: number;
}

export interface PickupOptimizationRoute {
  courierStaffId?: number;
  courierCode?: string;
  courierName?: string;
  vehicleId?: number;
  vehicleLicensePlate?: string;
  vehicleMaxWeight?: number;
  vehicleMaxVolume?: number;
  totalStops?: number;
  totalWeight?: number;
  totalVolume?: number;
  totalDistanceKm?: number;
  totalTravelMinutes?: number;
  totalServiceMinutes?: number;
  totalLatenessMinutes?: number;
  startTime?: string;
  endTime?: string;
  stops?: PickupOptimizationStop[];
}

export interface PickupOptimizationUnassignedOrder {
  orderId: number;
  orderCode?: string;
  customerOrderCode?: string;
  reason?: string;
}

export interface PickupOptimizationResponse {
  postOfficeId: number;
  postOfficeCode?: string;
  postOfficeName?: string;
  planningStartTime?: string;
  planningEndTime?: string;
  totalOrders?: number;
  assignedOrders?: number;
  unassignedOrders?: number;
  totalDistanceKm?: number;
  totalTravelMinutes?: number;
  totalServiceMinutes?: number;
  totalLatenessMinutes?: number;
  objectiveScore?: number;
  totalRoutes?: number;
  usedRoutes?: number;
  routes?: PickupOptimizationRoute[];
  unassignedOrderDetails?: PickupOptimizationUnassignedOrder[];
}

export interface PickupAssignedStop {
  sequence: number;
  orderId: number;
  orderCode?: string;
  customerOrderCode?: string;
  plannedArrivalTime?: string;
  plannedStartServiceTime?: string;
  plannedDepartureTime?: string;
  distanceFromPreviousKm?: number;
  travelMinutes?: number;
  latenessMinutes?: number;
}

export interface PickupAssignedTrip {
  tripId?: number;
  tripCode?: string;
  courierStaffId?: number;
  courierCode?: string;
  courierName?: string;
  vehicleId?: number;
  vehicleLicensePlate?: string;
  totalStops?: number;
  totalDistanceKm?: number;
  totalTravelMinutes?: number;
  totalServiceMinutes?: number;
  totalLatenessMinutes?: number;
  plannedStartTime?: string;
  plannedEndTime?: string;
  stops?: PickupAssignedStop[];
}

export interface PickupAssignmentUnassignedOrder {
  orderId: number;
  orderCode?: string;
  customerOrderCode?: string;
  reason?: string;
}

export interface PickupAssignmentResponse {
  postOfficeId: number;
  postOfficeCode?: string;
  postOfficeName?: string;
  shift: PickupShift;
  tripDate?: string;
  totalRequestedOrders?: number;
  assignedOrders?: number;
  unassignedOrders?: number;
  createdTrips?: number;
  trips?: PickupAssignedTrip[];
  unassignedOrderDetails?: PickupAssignmentUnassignedOrder[];
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
