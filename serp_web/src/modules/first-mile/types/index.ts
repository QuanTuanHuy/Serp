/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile module types
 */

export * from './billing.types';
export * from './dashboard';
export * from './lastMile.types';

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
export type ImportType = 'ORDER' | 'POST_OFFICE' | 'VEHICLE' | 'HUB';
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
  deliveryCapacity?: number;
  currentDeliveryLoad?: number;
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
  hubId?: number;
}

export type HubType = 'REGIONAL' | 'LOCAL';

export type HubStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE';

export interface Hub {
  id: number;
  code: string;
  name: string;
  hubType: HubType;
  provinceCode: string;
  wardCode: string;
  addressDetail: string;
  phoneNumber?: string;
  latitude?: number;
  longitude?: number;
  operationalStartDate?: string;
  operationalEndDate?: string;
  workingStartTime?: string;
  workingEndTime?: string;
  dailyCapacity?: number;
  currentLoad?: number;
  status: HubStatus;
  version?: number;
  imageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface HubPostOfficeMapping {
  id: number;
  hubId: number;
  postOfficeCode: string;
  createdAt?: string;
  updatedAt?: string;
  tenantId?: number;
}

export type SecondMileHubStaffRole = 'MANAGER' | 'EMPLOYEE' | 'DRIVER';

export type SecondMileHubStaffStatus = 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE';

export interface SecondMileHubStaffAssignment {
  id: number;
  hubId?: number;
  hubCode?: string;
  hubName?: string;
  staffId?: number;
  staffCode?: string;
  staffFullName?: string;
  staffRole?: SecondMileHubStaffRole;
  staffStatus?: SecondMileHubStaffStatus;
  assignedFrom?: string;
  assignedTo?: string;
  isPrimary?: boolean;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface SecondMileHubStaff {
  id: number;
  code?: string;
  fullName?: string;
  role?: SecondMileHubStaffRole;
  status?: SecondMileHubStaffStatus;
}

export interface AssignHubPostOfficeRequest {
  post_office_code: string;
}

export interface CreateHubRequest {
  code: string;
  name: string;
  hub_type: HubType;
  province_code: string;
  ward_code: string;
  address_detail: string;
  phone_number?: string;
  working_start_time?: string;
  working_end_time?: string;
  daily_capacity?: number;
  current_load?: number;
  latitude?: number;
  longitude?: number;
  status: HubStatus;
}

export interface UpdateHubRequest {
  code: string;
  name: string;
  hub_type: HubType;
  province_code: string;
  ward_code: string;
  address_detail: string;
  phone_number?: string;
  working_start_time?: string;
  working_end_time?: string;
  daily_capacity: number;
  current_load: number;
  latitude?: number;
  longitude?: number;
  status: HubStatus;
}

export interface HubImportItem {
  stt?: string;
  name?: string;
  code?: string;
  province_code?: string;
  ward_code?: string;
  address_detail?: string;
  phone_number?: string;
  hub_type?: HubType;
  operational_start_date?: string;
  operational_end_date?: string;
  working_start_time?: string;
  working_end_time?: string;
  status?: HubStatus;
  source_rows?: number[];
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

export interface PostOfficeStaffAssignment {
  id: number;
  postOfficeId?: number;
  postOfficeCode?: string;
  postOfficeName?: string;
  staffId?: number;
  staffCode?: string;
  staffFullName?: string;
  staffRole?: PostOfficeStaffRole;
  assignedFrom?: string;
  assignedTo?: string;
  isPrimary?: boolean;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface UpdatePostOfficeStaffAssignmentRequest {
  assigned_from?: string;
  assigned_to?: string;
  is_primary?: boolean;
  notes?: string;
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

export type SecondMileVehicleStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE';

export type SecondMileVehicleType = 'TRUCK' | 'VAN';

export interface SecondMileVehicle {
  id: number;
  licensePlate: string;
  vehicleType: SecondMileVehicleType;
  maxWeight: number;
  maxVolume: number;
  maxBags: number;
  imageUrl?: string;
  hubId: number;
  assignedStaffId?: number;
  assignedStaffCode?: string;
  assignedStaffFullName?: string;
  status: SecondMileVehicleStatus;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface SecondMileVehicleListFilters {
  keyword?: string;
  licensePlate?: string;
  vehicleType?: SecondMileVehicleType;
  hubId?: number;
  assignedStaffId?: number;
  status?: SecondMileVehicleStatus;
}

export interface SecondMileCreateVehicleRequest {
  license_plate: string;
  vehicle_type: SecondMileVehicleType;
  max_weight: number;
  max_volume: number;
  max_bags: number;
  hub_id: number;
  assigned_staff_id?: number;
  status: SecondMileVehicleStatus;
  image_url?: string;
}

export type SecondMileUpdateVehicleRequest = SecondMileCreateVehicleRequest;

export interface SecondMileVehicleImportItem {
  license_plate?: string;
  max_bags?: number;
  max_weight?: number;
  max_volume?: number;
  hub_id?: number;
  hub_code?: string;
  hub_name?: string;
  assigned_staff_id?: number;
  driver_code?: string;
  driver_name?: string;
  vehicle_type?: SecondMileVehicleType;
  status?: SecondMileVehicleStatus;
  source_rows?: number[];
}

export type SecondMileBagDestinationType = 'HUB' | 'POST_OFFICE';

export type SecondMileBagStatus =
  | 'CREATED'
  | 'SEALED'
  | 'IN_TRANSIT'
  | 'ARRIVED'
  | 'CANCELLED';

export interface SecondMileBagOrder {
  id: number;
  orderId?: number;
  orderCode?: string;
}

export interface SecondMileBag {
  id: number;
  bagCode?: string;
  originHubId?: number;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  vehicleId?: number;
  routeId?: number;
  maxWeight?: number;
  maxVolume?: number;
  maxOrders?: number;
  currentWeight?: number;
  currentVolume?: number;
  currentOrders?: number;
  status?: SecondMileBagStatus;
  sealedAt?: string;
  note?: string;
  orders?: SecondMileBagOrder[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface SecondMileBagCapacitySettings {
  id?: number;
  maxWeight: number;
  maxVolume: number;
  maxOrders: number;
}

export interface UpdateSecondMileBagCapacitySettingsRequest {
  max_weight: number;
  max_volume: number;
  max_orders: number;
}

export interface SecondMileBagListFilters {
  keyword?: string;
  bagCode?: string;
  originHubId?: number;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  vehicleId?: number;
  status?: SecondMileBagStatus;
}

export interface CreateSecondMileBagRequest {
  bag_code: string;
  origin_hub_id: number;
  destination_type: SecondMileBagDestinationType;
  destination_hub_id?: number;
  destination_post_office_code?: string;
  vehicle_id?: number;
  route_id?: number;
  max_weight?: number;
  max_volume?: number;
  max_orders?: number;
  status?: SecondMileBagStatus;
  note?: string;
}

export type UpdateSecondMileBagRequest = CreateSecondMileBagRequest & {
  status: SecondMileBagStatus;
};

export interface AddSecondMileBagOrderRequest {
  order_code: string;
}

export interface ReopenSecondMileBagRequest {
  reason: string;
}

export interface ValidateSecondMileBaggingRequest {
  bag_id: number;
  order_codes: string[];
}

export interface SecondMileBaggingValidationItem {
  orderCode: string;
  accepted: boolean;
  reason?: string;
}

export interface SecondMileBaggingValidation {
  bagId: number;
  acceptedCount: number;
  rejectedCount: number;
  items: SecondMileBaggingValidationItem[];
}

export interface AutoSecondMileBaggingPlanRequest {
  origin_hub_id: number;
  destination_type: SecondMileBagDestinationType;
  destination_hub_id?: number;
  destination_post_office_code?: string;
  order_codes: string[];
  execute?: boolean;
}

export interface AutoSecondMileBaggingPlanItem {
  bagCode: string;
  orderCodes: string[];
  totalWeight?: number;
  totalVolume?: number;
}

export interface AutoSecondMileBaggingPlan {
  executed: boolean;
  bagCount: number;
  items: AutoSecondMileBaggingPlanItem[];
}

export interface SecondMileBagSuggestion {
  bagId: number;
  bagCode?: string;
  remainingWeight?: number;
  remainingVolume?: number;
  remainingOrders?: number;
}

export interface SecondMileBaggingKpi {
  originHubId: number;
  sealedBagCount: number;
  avgFillRateWeight: number;
  avgFillRateVolume: number;
  avgOrdersPerBag: number;
}

export type SecondMileRouteDestinationType = 'HUB' | 'POST_OFFICE';

export type SecondMileRouteEndpointType = 'HUB' | 'POST_OFFICE';

export type SecondMileRouteStatus = 'ACTIVE' | 'INACTIVE';

export interface SecondMileRoute {
  id: number;
  routeCode: string;
  routeName: string;
  originType: SecondMileRouteEndpointType;
  originHubId?: number;
  originPostOfficeCode?: string;
  destinationType: SecondMileRouteDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  vehicleId?: number;
  estimatedDistanceKm?: number;
  estimatedDurationMinutes?: number;
  fixedDepartureTime?: string;
  status: SecondMileRouteStatus;
  note?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface SecondMileRouteListFilters {
  keyword?: string;
  routeCode?: string;
  originType?: SecondMileRouteEndpointType;
  originHubId?: number;
  originPostOfficeCode?: string;
  destinationType?: SecondMileRouteDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  vehicleId?: number;
  status?: SecondMileRouteStatus;
}

export interface SecondMileCreateRouteRequest {
  route_code: string;
  route_name: string;
  origin_type?: SecondMileRouteEndpointType;
  origin_hub_id?: number;
  origin_post_office_code?: string;
  destination_type: SecondMileRouteDestinationType;
  destination_hub_id?: number;
  destination_post_office_code?: string;
  vehicle_id?: number;
  estimated_distance_km?: number;
  estimated_duration_minutes?: number;
  fixed_departure_time?: string;
  status?: SecondMileRouteStatus;
  note?: string;
}

export type SecondMileUpdateRouteRequest = SecondMileCreateRouteRequest & {
  status: SecondMileRouteStatus;
};

export type HandoverManifestStatus =
  | 'CREATED'
  | 'OUTBOUND_CONFIRMED'
  | 'INBOUND_CONFIRMED'
  | 'CANCELLED';

export interface HandoverManifestOrderItem {
  id?: number;
  orderId?: number;
  orderCode?: string;
  customerOrderCode?: string;
  status?: FirstMileOrderStatus | SecondMileOrderStatus;
  scanOutTime?: string;
  scanInTime?: string;
}

export interface HandoverManifest {
  id: number;
  manifestCode?: string;
  originPostOfficeId?: number;
  originPostOfficeCode?: string;
  targetHubId?: number;
  vehicleId?: number;
  vehicleLicensePlate?: string;
  assignedDriverId?: number;
  routeId?: number;
  routeCode?: string;
  plannedDepartureAt?: string;
  plannedArrivalAt?: string;
  originPostOfficeLatitude?: number;
  originPostOfficeLongitude?: number;
  targetHubLatitude?: number;
  targetHubLongitude?: number;
  driverStartCheckinAt?: string;
  driverStartLatitude?: number;
  driverStartLongitude?: number;
  driverStartDistanceM?: number;
  driverStartPhotoUrl?: string;
  driverEndCheckinAt?: string;
  driverEndLatitude?: number;
  driverEndLongitude?: number;
  driverEndDistanceM?: number;
  driverEndPhotoUrl?: string;
  status?: HandoverManifestStatus;
  totalOrders?: number;
  scannedOutOrders?: number;
  scannedInOrders?: number;
  dispatchedAt?: string;
  inboundConfirmedAt?: string;
  sealCode?: string;
  note?: string;
  orders?: HandoverManifestOrderItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface HandoverManifestListFilters {
  postOfficeId?: number;
  originPostOfficeCode?: string;
  targetHubId?: number;
  vehicleId?: number;
  status?: HandoverManifestStatus;
}

export type BagDistributionManifestStatus = HandoverManifestStatus;

export interface BagDistributionManifestBag {
  id: number;
  bagId?: number;
  bagCode?: string;
  originHubId?: number;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  totalWeightSnapshot?: number;
  totalVolumeSnapshot?: number;
  totalOrdersSnapshot?: number;
  scanOutTime?: string;
  scanInTime?: string;
}

export interface BagDistributionManifest {
  id: number;
  manifestCode?: string;
  originHubId?: number;
  originHubCode?: string;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationHubCode?: string;
  destinationPostOfficeCode?: string;
  routeId?: number;
  routeCode?: string;
  vehicleId?: number;
  vehicleLicensePlate?: string;
  assignedDriverId?: number;
  plannedDepartureAt?: string;
  plannedArrivalAt?: string;
  actualDepartureAt?: string;
  actualArrivalAt?: string;
  driverStartLatitude?: number;
  driverStartLongitude?: number;
  driverStartDistanceM?: number;
  driverStartPhotoUrl?: string;
  driverEndLatitude?: number;
  driverEndLongitude?: number;
  driverEndDistanceM?: number;
  driverEndPhotoUrl?: string;
  status?: BagDistributionManifestStatus;
  note?: string;
  bags?: BagDistributionManifestBag[];
  createdAt?: string;
  updatedAt?: string;
}

export interface BagDistributionManifestListFilters {
  originHubId?: number;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  routeId?: number;
  vehicleId?: number;
  assignedDriverId?: number;
  status?: BagDistributionManifestStatus;
}

export interface CreateBagDistributionManifestRequest {
  origin_hub_id: number;
  destination_type: SecondMileBagDestinationType;
  destination_hub_id?: number;
  destination_post_office_code?: string;
  route_id: number;
  vehicle_id: number;
  planned_departure_at: string;
  planned_arrival_at: string;
  bag_ids: number[];
  note?: string;
}

export interface AutoPlanBagDistributionRequest {
  origin_hub_id: number;
  destination_type?: SecondMileBagDestinationType;
  destination_hub_id?: number;
  destination_post_office_code?: string;
  planned_departure_at: string;
  planned_arrival_at: string;
  sealed_sla_hours?: number;
  execute?: boolean;
  note?: string;
}

export interface ConfirmBagDistributionInboundRequest {
  bag_ids?: number[];
}

export interface BagDistributionPlanItem {
  originHubId?: number;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  routeId?: number;
  routeCode?: string;
  vehicleId?: number;
  vehicleLicensePlate?: string;
  assignedDriverId?: number;
  plannedDepartureAt?: string;
  plannedArrivalAt?: string;
  bagIds: number[];
  bagCodes: string[];
  totalWeight?: number;
  totalVolume?: number;
  totalOrders?: number;
  score?: number;
  hints: string[];
  createdManifestId?: number;
  createdManifestCode?: string;
}

export interface BagDistributionPlan {
  executed: boolean;
  manifestCount: number;
  items: BagDistributionPlanItem[];
}

export interface CreateHandoverManifestRequest {
  origin_post_office_code: string;
  target_hub_id: number;
  vehicle_id: number;
  route_id: number;
  planned_departure_at: string;
  planned_arrival_at: string;
  origin_post_office_latitude?: number;
  origin_post_office_longitude?: number;
  order_codes: string[];
}

export interface CreatePostOfficeHandoverManifestRequest {
  post_office_id: number;
  target_hub_id?: number;
  order_codes: string[];
  note?: string;
}

export interface DispatchPostOfficeHandoverManifestRequest {
  vehicle_id: number;
  route_id: number;
  planned_departure_at: string;
  planned_arrival_at: string;
  seal_code?: string;
  note?: string;
}

export interface ScanOutHandoverOrderRequest {
  order_code: string;
}

export interface DriverHandoverCheckinRequest {
  latitude: number;
  longitude: number;
  location_label?: string;
}

export type SecondMileOrderStatus =
  | 'CREATED'
  | 'ASSIGNED_TO_PICKUP'
  | 'PICKING_UP'
  | 'PICKUP_FAILED'
  | 'PICKED_UP'
  | 'PENDING_ORIGIN_POST_OFFICE_INBOUND'
  | 'AT_ORIGIN_POST_OFFICE'
  | 'OUTBOUND_READY_FROM_PO'
  | 'INBOUND_AT_ORIGIN_HUB'
  | 'BAGGING_IN_PROGRESS'
  | 'BAGGED'
  | 'BAG_SEALED'
  | 'CANCELLED'
  | 'LOST_OR_DAMAGED';

export interface SecondMileOrder {
  id: number;
  orderCode?: string;
  customerOrderCode?: string;
  originPostOfficeCode?: string;
  destinationPostOfficeCode?: string;
  status?: SecondMileOrderStatus;
  totalWeight?: number;
  totalVolume?: number;
}

export interface SecondMileOrderListFilters {
  keyword?: string;
  orderCode?: string;
  originPostOfficeCode?: string;
  originPostOfficeCodes?: string[];
  status?: SecondMileOrderStatus;
  statuses?: SecondMileOrderStatus[];
}

export type FirstMileOrderStatus =
  | 'CREATED'
  | 'ASSIGNED_TO_PICKUP'
  | 'PICKING_UP'
  | 'PICKUP_FAILED'
  | 'PICKED_UP'
  | 'PENDING_ORIGIN_POST_OFFICE_INBOUND'
  | 'AT_ORIGIN_POST_OFFICE'
  | 'OUTBOUND_READY_FROM_PO'
  | 'INBOUND_AT_ORIGIN_HUB'
  | 'BAGGING_IN_PROGRESS'
  | 'BAGGED'
  | 'BAG_SEALED'
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

export type FirstMileOrderPickupMethod =
  | 'COURIER_PICKUP'
  | 'DROP_OFF_AT_POST_OFFICE';

export type FirstMileFeePayer = 'SENDER' | 'RECEIVER';

export type FirstMilePaymentStatus = 'UNPAID' | 'PAID';

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
  pickupMethod?: FirstMileOrderPickupMethod;
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
  orderType?: FirstMileOrderType;
  feePayer?: FirstMileFeePayer;
  paymentStatus?: FirstMilePaymentStatus;
  codAmount?: number;
  totalWeight?: number;
  totalValue?: number;
  totalVolume?: number;
  dimensionLengthCm?: number;
  dimensionWidthCm?: number;
  dimensionHeightCm?: number;
  baseShippingFee?: number;
  codFee?: number;
  extraFee?: number;
  totalShippingFee?: number;
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

export interface FirstMileOrderTimelineItem {
  id?: number;
  orderId?: number;
  orderCode?: string;
  customerOrderCode?: string;
  orderStatus?: FirstMileOrderStatus;
  description?: string;
  eventTime?: string;
  recordedBy?: string;
  tripId?: number;
  tripCode?: string;
  postOfficeId?: number;
  postOfficeCode?: string;
  postOfficeName?: string;
  courierStaffId?: number;
  courierCode?: string;
  courierName?: string;
  vehicleId?: number;
  vehicleLicensePlate?: string;
  latitude?: number;
  longitude?: number;
  locationLabel?: string;
}

export interface FirstMileOrderListFilters {
  keyword?: string;
  orderCode?: string;
  customerOrderCode?: string;
  senderPhone?: string;
  receiverPhone?: string;
  originPostOfficeCode?: string;
  originPostOfficeCodes?: string[];
  destinationPostOfficeCode?: string;
  status?: FirstMileOrderStatus;
  statuses?: FirstMileOrderStatus[];
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
  pickup_method?: FirstMileOrderPickupMethod;
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

export interface OrderConfirmationDestinationPostOffice {
  id: number;
  code: string;
  name: string;
  currentDeliveryLoad?: number;
  deliveryCapacity?: number;
}

export interface OrderConfirmationResponse {
  orderId: number;
  orderCode: string;
  customerOrderCode?: string;
  status: FirstMileOrderStatus;
  alreadyConfirmed: boolean;
  originPostOffice?: OrderConfirmationOriginPostOffice | null;
  destinationPostOffice?: OrderConfirmationDestinationPostOffice | null;
}

export interface InitiateOrderPaymentRequest {
  amount?: number;
}

export interface OrderPaymentInitResponse {
  orderId: number;
  orderCode: string;
  amount: number;
  appTransId: string;
  paymentUrl?: string;
  status?: string;
  message?: string;
}

export interface ConfirmOrderPaymentRequest {
  appTransId: string;
}

export interface OrderPaymentConfirmResponse {
  orderId: number;
  orderCode: string;
  paymentStatus: FirstMilePaymentStatus;
  appTransId: string;
  gatewayStatus?: string;
  message?: string;
}

export interface OrderDropOffPostOfficeSuggestion {
  id: number;
  code: string;
  name: string;
  provinceCode?: string;
  wardCode?: string;
  addressDetail?: string;
  priority?: number;
  currentLoad?: number;
  dailyCapacity?: number;
  remainingCapacity?: number;
  latitude?: number;
  longitude?: number;
  distanceMeters?: number;
}

export type PickupShift = 'MORNING' | 'AFTERNOON' | 'EVENING';

export type PickupOptimizationGoal =
  | 'BALANCED'
  | 'ON_TIME_PRIORITY'
  | 'COST_EFFICIENCY'
  | 'MAX_ASSIGNMENT';

export interface OptimizePickupPlanRequest {
  post_office_id: number;
  planning_start_time?: string;
  planning_end_time?: string;
  courier_ids?: number[];
  candidate_statuses?: FirstMileOrderStatus[];
  vehicle?: string;
  order_limit?: number;
  optimization_goal?: PickupOptimizationGoal;
  average_speed_kmph?: number;
  service_minutes_per_stop?: number;
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
  optimization_goal?: PickupOptimizationGoal;
  average_speed_kmph?: number;
  service_minutes_per_stop?: number;
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
  optimization_goal?: PickupOptimizationGoal;
  average_speed_kmph?: number;
  service_minutes_per_stop?: number;
  allow_lateness?: boolean;
  enforce_planning_end?: boolean;
  enforce_capacity?: boolean;
  force_assign?: boolean;
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

export type PickupTrackingActorScope =
  | 'ADMIN_ALL'
  | 'MANAGER_SCOPED'
  | 'COURIER_SELF';

export type PickupTripStatus =
  | 'PLANNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface PickupTrackingTrip {
  tripId: number;
  tripCode?: string;
  tripStatus?: PickupTripStatus;
  shift?: PickupShift;
  postOfficeId?: number;
  postOfficeCode?: string;
  postOfficeName?: string;
  courierStaffId?: number;
  courierCode?: string;
  courierName?: string;
  plannedStartTime?: string;
  plannedEndTime?: string;
  totalOrders?: number;
  checkedInOrders?: number;
  pendingCheckinOrders?: number;
  returnableToPostOfficeOrders?: number;
  pendingPostOfficeInboundOrders?: number;
}

export interface PickupTrackingOrder {
  tripOrderId: number;
  tripId?: number;
  tripCode?: string;
  tripStatus?: PickupTripStatus;
  sequenceNo?: number;
  orderId: number;
  orderCode?: string;
  customerOrderCode?: string;
  orderStatus?: FirstMileOrderStatus;
  senderName?: string;
  senderPhone?: string;
  senderAddressDetail?: string;
  senderLatitude?: number;
  senderLongitude?: number;
  pickupTimeStart?: string;
  pickupTimeEnd?: string;
  plannedArrivalTime?: string;
  plannedStartServiceTime?: string;
  plannedDepartureTime?: string;
  courierStaffId?: number;
  courierCode?: string;
  courierName?: string;
  postOfficeId?: number;
  postOfficeCode?: string;
  postOfficeName?: string;
  checkedIn?: boolean;
  checkinId?: number;
  checkinTime?: string;
  checkinLatitude?: number;
  checkinLongitude?: number;
  checkinPhotoUrl?: string;
  checkinDistanceM?: number;
  allowedRadiusM?: number;
}

export interface PickupTrackingOverviewResponse {
  tripDate: string;
  actorScope: PickupTrackingActorScope;
  selectedPostOfficeId?: number;
  selectedCourierStaffId?: number;
  totalTrips?: number;
  totalOrders?: number;
  checkedInOrders?: number;
  pendingCheckinOrders?: number;
  pickingUpOrders?: number;
  pickedUpOrders?: number;
  pickupFailedOrders?: number;
  pendingPostOfficeInboundOrders?: number;
  trips?: PickupTrackingTrip[];
  orders?: PickupTrackingOrder[];
}

export interface PickupCheckinResponse {
  checkinId?: number;
  orderId?: number;
  orderCode?: string;
  orderStatus?: FirstMileOrderStatus;
  tripId?: number;
  tripCode?: string;
  courierStaffId?: number;
  checkinTime?: string;
  photoUrl?: string;
  checkinLatitude?: number;
  checkinLongitude?: number;
  pickupLatitude?: number;
  pickupLongitude?: number;
  distanceMeters?: number;
  allowedRadiusMeters?: number;
}

export interface PickupCheckinDetailResponse {
  checkinId?: number;
  orderId?: number;
  orderCode?: string;
  customerOrderCode?: string;
  orderStatus?: FirstMileOrderStatus;
  tripId?: number;
  tripCode?: string;
  tripStatus?: PickupTripStatus;
  courierStaffId?: number;
  courierCode?: string;
  courierName?: string;
  postOfficeId?: number;
  postOfficeCode?: string;
  postOfficeName?: string;
  senderName?: string;
  senderPhone?: string;
  senderAddressDetail?: string;
  pickupLatitude?: number;
  pickupLongitude?: number;
  checkinTime?: string;
  checkinLatitude?: number;
  checkinLongitude?: number;
  photoUrl?: string;
  distanceMeters?: number;
  allowedRadiusMeters?: number;
}

export interface PickupTripLifecycleResponse {
  tripId?: number;
  tripCode?: string;
  tripStatus?: PickupTripStatus;
  totalOrders?: number;
  checkedInOrders?: number;
  pendingCheckinOrders?: number;
  returnedToPostOfficeOrders?: number;
  pendingPostOfficeInboundOrders?: number;
  receivedAtPostOfficeOrders?: number;
  allOrdersCheckedIn?: boolean;
}

export interface ConfirmPostOfficeInboundRequest {
  orderCodes: string[];
}

export interface HubListFilters {
  keyword?: string;
  code?: string;
  name?: string;
  hubType?: HubType;
  provinceCode?: string;
  wardCode?: string;
  status?: HubStatus;
  hasLocation?: boolean;
  minLatitude?: number;
  maxLatitude?: number;
  minLongitude?: number;
  maxLongitude?: number;
  minDailyCapacity?: number;
  maxDailyCapacity?: number;
  minCurrentLoad?: number;
  maxCurrentLoad?: number;
}

export interface PostOfficeListFilters {
  keyword?: string;
  code?: string;
  name?: string;
  hubId?: number;
  provinceCode?: string;
  wardCode?: string;
  status?: PostOfficeStatus;
  hasLocation?: boolean;
  minLatitude?: number;
  maxLatitude?: number;
  minLongitude?: number;
  maxLongitude?: number;
  minServiceRadiusM?: number;
  maxServiceRadiusM?: number;
  minDailyCapacity?: number;
  maxDailyCapacity?: number;
  minCurrentLoad?: number;
  maxCurrentLoad?: number;
  minDeliveryCapacity?: number;
  maxDeliveryCapacity?: number;
  minCurrentDeliveryLoad?: number;
  maxCurrentDeliveryLoad?: number;
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
  delivery_capacity?: number;
  current_delivery_load?: number;
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
