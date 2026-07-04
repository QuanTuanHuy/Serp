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
export type OutboundShipmentStatus = ShipmentStatus;
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

export interface Category {
  id: string;
  name: string;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  tenantId?: number;
}

export interface InventoryItem {
  id: string;
  productId: string;
  quantityOnHand: number;
  quantityReserved: number;
  quantityCommitted: number;
  facilityId: string;
  lotId?: string;
  expirationDate?: string;
  manufacturingDate?: string;
  statusId?: InventoryItemStatus;
  receivedDate?: string;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  tenantId?: number;
}

export interface InventoryItemDetail {
  id: string;
  productId: string;
  inventoryItemId: string;
  quantity: number;
  notYetOutboundQuantity?: number;
  shipmentId?: string;
  orderItemId: string;
  note?: string;
  lotId?: string;
  expirationDate?: string;
  manufacturingDate?: string;
  facilityId: string;
  unit: string;
  price: number;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  tenantId?: number;
  inventoryItem?: InventoryItem;
}

export interface SaleOrderItem {
  id: string;
  orderId: string;
  orderItemSeqId: number;
  productId: string;
  quantity: number;
  quantityRemaining: number;
  amount: number;
  statusId: OrderItemStatus;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  price: number;
  tax?: number;
  discount?: number;
  unit?: string;
  tenantId?: number;
  product?: Product;
  allocatedInventoryItems?: InventoryItemDetail[];
}

export interface SaleOrder {
  id: string;
  orderTypeId: OrderType;
  fromSupplierId?: string;
  toCustomerId?: string;
  createdByUserId?: number;
  createdStamp?: string;
  orderDate?: string;
  statusId: OrderStatus;
  lastUpdatedStamp?: string;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  note?: string;
  orderName?: string;
  priority?: number;
  deliveryAddressId?: string;
  deliveryPhone?: string;
  saleChannelId?: SaleChannel;
  deliveryFullAddress?: string;
  totalQuantity?: number;
  totalAmount?: number;
  costs?: string;
  userApprovedId?: number;
  userCancelledId?: number;
  cancellationNote?: string;
  tenantId?: number;
  items?: SaleOrderItem[];
}

export type Order = SaleOrder;
export type OrderItem = SaleOrderItem;

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
  weightKg: number;
  heightM: number;
  widthM: number;
  lengthM: number;
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
  inventoryItem?: InventoryItem;
}

export interface OutboundShipment {
  id: string;
  orderId: string;
  customerId?: string;
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
  vehicleShipper: VehicleShipper;
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
  workingDate?: string;
  workingDay?: string;
}

// Query filter types
export interface CategoryFilters {
  query?: string;
  statusId?: string;
}

export interface CustomerFilters {
  query?: string;
  statusId?: string;
}

export interface FacilityFilters {
  query?: string;
  statusId?: FacilityStatus;
}

export interface InventoryItemFilters {
  query?: string;
  productId?: string;
  facilityId?: string;
  expirationDateFrom?: string;
  expirationDateTo?: string;
  manufacturingDateFrom?: string;
  manufacturingDateTo?: string;
  statusId?: InventoryItemStatus;
}

export interface SaleOrderFilters {
  query?: string;
  statusId?: OrderStatus;
  orderTypeId?: OrderType;
  toCustomerId?: string;
  fromSupplierId?: string;
  saleChannelId?: SaleChannel;
  orderDateAfter?: string;
  orderDateBefore?: string;
  deliveryBefore?: string;
  deliveryAfter?: string;
}

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

export interface VehicleUsageFilters {
  query?: string;
  vehicleType?: VehicleType;
  workingDate?: string;
}

export interface VehicleShipperFilters {
  shipperId?: number;
  vehicleId?: string;
  workingDate?: string;
}
