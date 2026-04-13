/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 module types
*/

// Common response types
export type ResponseStatus = 'SUCCESS' | 'FAILED';
export type SortDirection = 'asc' | 'desc';

export interface PaginationParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: SortDirection;
}

export interface PageResponse<T> {
  totalItems: number;
  totalPages: number;
  currentPage: number;
  items: T[];
}

export interface GeneralResponse<T> {
  code: number;
  status: ResponseStatus;
  message: string;
  data?: T;
}

export interface ListQueryParams<TFilters> {
  filters?: TFilters;
  pagination?: PaginationParams;
}

// Shared domain enums from logistics2 service
export type EntityType = 'PRODUCT' | 'SUPPLIER' | 'CUSTOMER' | 'FACILITY';
export type AddressType = 'FACILITY' | 'SHIPPING' | 'BUSINESS';
export type FacilityStatus = 'ACTIVE' | 'INACTIVE';
export type ProductStatus = 'ACTIVE' | 'INACTIVE';
export type InventoryItemStatus = 'VALID' | 'EXPIRED' | 'DAMAGED';
export type OrderItemStatus = 'CREATED' | 'DELIVERED';
export type OrderStatus =
  | 'CREATED'
  | 'APPROVED'
  | 'CANCELLED'
  | 'FULLY_DELIVERED';
export type OrderType = 'PURCHASE' | 'SALES';
export type SaleChannel = 'PARTNER' | 'ONLINE' | 'RETAIL';
export type ShipmentType = 'INBOUND' | 'OUTBOUND';
export type ShipmentStatus =
  | 'CREATED'
  | 'IMPORTED'
  | 'READY_TO_EXPORT'
  | 'DELIVERED';
export type SupplierStatus = 'ACTIVE' | 'INACTIVE';

export type PlanOptimizationStatus =
  | 'DRAFT'
  | 'OPTIMIZING'
  | 'COMPLETED'
  | 'FAILED';
export type DeliverySlipStatus =
  | 'PENDING'
  | 'ASSIGNED'
  | 'DELIVERING'
  | 'DELIVERED'
  | 'RECALLING';
export type RouteStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'ABORTED';
export type RouteStopStatus = 'WAITING' | 'ARRIVED' | 'FAILED';
export type VehicleType =
  | 'MOTORBIKE'
  | 'VAN_500KG'
  | 'VAN_1000KG'
  | 'LIGHT_TRUCK_1T'
  | 'LIGHT_TRUCK_2T';
export type VehicleStatus = 'IN_USE' | 'MAINTENANCE';
export type VehicleShipperStatus =
  | 'ACTIVE'
  | 'INACTIVATE_REQUESTED'
  | 'CANCELED';

// Shared entities returned by nested responses
export interface Address {
  id: string;
  entityId: string;
  entityType: EntityType;
  addressType: AddressType;
  latitude: number;
  longitude: number;
  isDefault: boolean;
  fullAddress: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
}

export interface Facility {
  id: string;
  name: string;
  statusId: FacilityStatus;
  currentAddressId: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  isDefault: boolean;
  phone?: string;
  postalCode?: string;
  length?: number;
  width?: number;
  height?: number;
  tenantId: number;
  address?: Address;
}

export interface Product {
  id: string;
  name: string;
  weight?: number;
  height?: number;
  unit?: string;
  costPrice?: number;
  wholeSalePrice?: number;
  retailPrice?: number;
  categoryId?: string;
  statusId?: ProductStatus;
  imageId?: string;
  extraProps?: string;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  vatRate?: number;
  skuCode?: string;
  tenantId?: number;
}

export interface Customer {
  id: string;
  name: string;
  currentAddressId: string;
  statusId?: string;
  phone?: string;
  email?: string;
  tenantId?: number;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  address?: Address;
}

// Main logistics2 entities
export interface DeliveryItem {
  id: string;
  code: string;
  deliverySlipId: string;
  outboundShipmentItemId: string;
  inventoryItemId: string;
  productId: string;
  quantity: number;
  productName?: string;
  weightKg?: number;
  heightM?: number;
  widthM?: number;
  lengthM?: number;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  tenantId?: number;
}

export interface DeliverySlip {
  id: string;
  code: string;
  outboundShipmentId: string;
  customerId: string;
  facilityId: string;
  status: DeliverySlipStatus;
  totalWeightKg: number;
  totalVolumeCbm: number;
  customerName?: string;
  customerPhone?: string;
  customerAddressId?: string;
  facilityAddressId?: string;
  createdByUserId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  facility?: Facility;
  customerAddress?: Address;
  facilityAddress?: Address;
  items?: DeliveryItem[];
}

export interface Vehicle {
  id: string;
  licensePlate: string;
  vehicleType: VehicleType;
  maxWeightKg: number;
  maxVolumeCbm: number;
  status: VehicleStatus;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
}

export interface VehicleShipper {
  id: string;
  vehicleId: string;
  shipperId: number;
  workingDate: string;
  status: VehicleShipperStatus;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  vehicle?: Vehicle;
}

export interface OutboundShipmentItem {
  id: string;
  outboundShipmentId: string;
  inventoryItemDetailId: string;
  inventoryItemId: string;
  productId: string;
  quantity: number;
  quantityRemaining?: number;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  tenantId?: number;
  product?: Product;
}

export interface OutboundShipment {
  id: string;
  orderId: string;
  facilityId: string;
  name: string;
  status: ShipmentStatus;
  createdByUserId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  items?: OutboundShipmentItem[];
  facility?: Facility;
}

export interface RouteStop {
  id: string;
  routeId: string;
  deliverySlipId: string;
  sequence: number;
  status: RouteStopStatus;
  encodedPolyline?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  deliverySlip?: DeliverySlip;
}

export interface Route {
  id: string;
  deliveryPlanId: string;
  vehicleShipperId: string;
  totalDistanceKm?: number;
  totalWeightLoadedKg?: number;
  totalVolumeLoadedCbm?: number;
  status: RouteStatus;
  routeStopCount: number;
  deliveryDate: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  routeStops?: RouteStop[];
}

export interface DeliveryPlan {
  id: string;
  facilityId: string;
  planCode: string;
  deliveryDate: string;
  optimizationStatus: PlanOptimizationStatus;
  totalSlips: number;
  totalVehicles: number;
  createdByUserId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  slips?: DeliverySlip[];
  vehicleShippers?: VehicleShipper[];
  facility?: Facility;
}

// Request payloads
export interface CreateDeliveryPlanRequest {
  facilityId: string;
  deliveryDate: string;
  deliverySlipIds: string[];
  vehicleShipperIds: string[];
}

export interface UpdateDeliveryPlanRequestBody {
  additionalDeliverySlipIds?: string[];
  additionalVehicleShipperIds?: string[];
  removedDeliverySlipIds?: string[];
  removedVehicleShipperIds?: string[];
}

export interface UpdateDeliveryPlanRequest {
  planId: string;
  data: UpdateDeliveryPlanRequestBody;
}

export interface DeliverySlipItemForm {
  outboundShipmentItemId: string;
  inventoryItemId: string;
  productId: string;
  quantity: number;
}

export interface CreateDeliverySlipRequest {
  outboundShipmentId: string;
  customerId: string;
  facilityId: string;
  items: DeliverySlipItemForm[];
}

export interface UpdateDeliveryItemRequestBody {
  quantity: number;
}

export interface AddDeliverySlipItemRequest {
  slipId: string;
  data: DeliverySlipItemForm;
}

export interface UpdateDeliverySlipItemRequest {
  slipId: string;
  itemId: string;
  data: UpdateDeliveryItemRequestBody;
}

export interface RemoveDeliverySlipItemRequest {
  slipId: string;
  itemId: string;
}

export interface CreateVehicleRequest {
  licensePlate: string;
  vehicleType: VehicleType;
  maxWeightKg: number;
  maxVolumeCbm: number;
}

export interface UpdateVehicleRequestBody {
  licensePlate?: string;
  vehicleType?: VehicleType;
  maxWeightKg?: number;
  maxVolumeCbm?: number;
}

export interface UpdateVehicleRequest {
  vehicleId: string;
  data: UpdateVehicleRequestBody;
}

export interface AssignVehicleShipperRequest {
  vehicleId: string;
  workingDate: string;
}

// Query filter types
export interface DeliveryPlanFilters {
  query?: string;
  facilityId?: string;
  deliveryDate?: string;
  optimizationStatus?: PlanOptimizationStatus;
}

export interface DeliverySlipFilters {
  status?: DeliverySlipStatus;
  outboundShipmentId?: string;
  customerId?: string;
  facilityId?: string;
  query?: string;
}

export interface OutboundShipmentFilters {
  query?: string;
  status?: ShipmentStatus;
  facilityId?: string;
  orderId?: string;
}

export interface RouteFilters {
  deliveryPlanId?: string;
  vehicleShipperId?: string;
  status?: RouteStatus;
  deliveryDate?: string;
}

export interface VehicleFilters {
  query?: string;
  vehicleType?: VehicleType;
  vehicleStatus?: VehicleStatus;
}

export interface VehicleShipperFilters {
  shipperId?: number;
  vehicleId?: string;
  workingDate?: string;
}
