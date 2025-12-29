/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Module Types
*/

// Constants
export type ResponseStatus = 'SUCCESS' | 'FAILED';
export type EntityType = 'PRODUCT' | 'SUPPLIER' | 'CUSTOMER' | 'FACILITY';
export type AddressType = 'FACILIY' | 'SHIPPING' | 'BUSSINESS';
export type CustomerStatus = 'ACTIVE' | 'INACTIVE';
export type FacilityStatus = 'ACTIVE' | 'INACTIVE';
export type InventoryItemStatus = 'VALID' | 'EXPIRED' | 'DAMAGED';
export type OrderItemStatus = 'CREATED' | 'DELIVERED';
export type OrderStatus =
  | 'CREATED'
  | 'APPROVED'
  | 'CANCELLED'
  | 'FULLY_DELIVERED';
export type OrderType = 'PURCHASE' | 'SALES';
export type ProductStatus = 'ACTIVE' | 'INACTIVE';
export type SaleChannel = 'ONLINE' | 'PARTNER' | 'RETAIL';
export type ShipmentType = 'INBOUND' | 'OUTBOUND';
export type ShipmentStatus = 'CREATED' | 'IMPORTED' | 'EXPORTED';
export type SupplierStatus = 'ACTIVE' | 'INACTIVE';

export interface PaginationParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  items: T[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export interface APIResponse<T> {
  code: number;
  message: string;
  status: ResponseStatus;
  data?: T;
}

// Address types
export interface Address {
  id: string;
  entityId: string;
  entityType: EntityType;
  addressType: AddressType;
  latitude: number;
  longitude: number;
  fullAddress: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  default: boolean;
}

export interface AddressCreationForm {
  entityId: string;
    entityType: EntityType;
    addressType: AddressType;
    latitude: number;
    longitude: number;
  fullAddress: string;
  default?: boolean;
}

export interface AddressUpdateForm {
  addressType?: AddressType;
  latitude?: number;
  longitude?: number;
  fullAddress?: string;
  default?: boolean;
}

// Category types
export interface Category {
  id: string;
  name: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
}

export interface CategoryForm {
  name: string;
}

export interface CategoryFilters {
  query?: string;
}

// Customer types
export interface Customer {
  id: string;
  name: string;
  currentAddressId?: string;
  statusId: CustomerStatus;
  phone?: string;
  email?: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
  address?: Address;
}

export interface CustomerFilters {
  query?: string;
  statusId?: CustomerStatus;
}

// Facility types
export interface Facility {
  id: string;
  name: string;
  statusId: FacilityStatus;
  currentAddressId?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  phone?: string;
  postalCode: string;
  length: number;
  width: number;
  height: number;
  capacity: number;
  tenantId: number;
  address?: Address;
  default: boolean;
}

export interface FacilityCreationForm {
  name: string;
  phone?: string;
  statusId: string;
  postalCode: string;
  length?: number;
  width?: number;
  height?: number;
  addressType: AddressType;
  latitude?: number;
  longitude?: number;
  fullAddress: string;
}

export interface FacilityUpdateForm {
  name?: string;
  isDefault?: boolean;
  statusId?: FacilityStatus;
  phone?: string;
  postalCode?: string;
  length?: number;
  width?: number;
  height?: number;
}

export interface FacilityFilters {
  query?: string;
  statusId?: FacilityStatus;
}

// Inventory Item types
export interface InventoryItem {
  id: string;
  productId: string;
  quantityOnHand: number;
  quantityCommitted: number;
  quantityReserved: number;
  facilityId: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  lotId: string;
  expirationDate?: string;
  manufacturingDate?: string;
  statusId: InventoryItemStatus;
  receivedDate: string;
  tenantId: number;
}

export interface InventoryItemDetail {
  id: string;
  productId: string;
  inventoryItemId: string;
  quantity: number;
  shipmentId?: string;
  orderItemId: string;
  note?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  lotId: string;
  expirationDate?: string;
  manufacturingDate?: string;
  facilityId: string;
  unit: string;
  price: number;
  tenantId: number;
  inventoryItem: InventoryItem;
}

export interface InventoryItemCreationForm {
  productId: string;
  quantity: number;
  lotId: string;
  facilityId: string;
  expirationDate?: string;
  manufacturingDate?: string;
  statusId: InventoryItemStatus;
}

export interface InventoryItemUpdateForm {
  quantity?: number;
  expirationDate?: string;
  manufacturingDate?: string;
  statusId?: InventoryItemStatus;
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

// Order types
export interface Order {
  id: string;
  orderTypeId: OrderType;
  fromSupplierId?: string;
  toCustomerId: string;
  createdByUserId: number;
  createdStamp: string;
  orderDate: string;
  statusId: OrderStatus;
  lastUpdatedStamp: string;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  note?: string;
  orderName?: string;
  priority?: number;
  deliveryAddressId?: string;
  deliveryPhone?: string;
  saleChannelId: SaleChannel;
  deliveryFullAddress?: string;
  totalQuantity?: number;
  totalAmount: number;
  costs?: string;
  userApprovedId?: number;
  userCancelledId?: number;
  cancellationNote?: string;
  tenantId: number;
  items?: OrderItemEntity[];
}

export interface OrderItemEntity {
  id: string;
  orderId: string;
  orderItemSeqId: number;
  productId: string;
  quantity: number;
  quantityRemaining: number;
  amount: number;
  statusId: OrderItemStatus;
  createdStamp: string;
  lastUpdatedStamp: string;
  price: number;
  tax?: number;
  discount?: number;
  unit: string;
  tenantId: number;
  product?: Product;
  allocatedInventoryItems?: InventoryItemDetail[];
}

export interface OrderFilters {
  query?: string;
  statusId?: string;
  toCustomerId?: string;
    fromSupplierId?: string;
  saleChannelId?: SaleChannel;
  orderDateAfter?: string;
  orderDateBefore?: string;
  deliveryAfter?: string;
  deliveryBefore?: string;
}

// Product types
export interface Product {
  id: string;
  name: string;
  weight?: number;
  height?: number;
  unit: string;
  costPrice: number;
  wholeSalePrice: number;
  retailPrice: number;
  categoryId: string;
  statusId: ProductStatus;
  imageId?: string;
  extraProps?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  vatRate?: number;
  skuCode: string;
  tenantId: number;
  quantityAvailable: number;
}

export interface ProductCreationForm {
  name: string;
  weight?: number;
  height?: number;
  unit: string;
  costPrice?: number;
  wholeSalePrice?: number;
  retailPrice?: number;
  categoryId: string;
  statusId: ProductStatus;
  imageId?: string;
  extraProps?: string;
  vatRate?: number;
  skuCode: string;
}

export interface ProductUpdateForm {
  name: string;
  weight?: number;
  height?: number;
  unit?: string;
  costPrice?: number;
  wholeSalePrice?: number;
  retailPrice?: number;
  statusId?: ProductStatus;
  imageId?: string;
  extraProps?: string;
  vatRate?: number;
  skuCode?: string;
}

export interface ProductFilters {
  query?: string;
  categoryId?: string;
  statusId?: ProductStatus;
}

// Shipment types
export interface Shipment {
  id: string;
  shipmentTypeId: ShipmentType;
  fromSupplierId?: string;
  toCustomerId?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  orderId: string;
  createdByUserId: number;
  shipmentName: string;
  statusId: ShipmentStatus;
  handledByUserId?: number;
  note?: string;
  expectedDeliveryDate?: string;
  userCancelledId?: number;
  totalWeight?: number;
  totalQuantity?: number;
  tenantId: number;
  items?: InventoryItemDetail[];
}

export interface ShipmentCreationForm {
    orderId: string;
  shipmentName?: string;
  note?: string;
  expectedDeliveryDate?: string;
  items?: ShipmentItemForm[];
}

export interface ShipmentItemForm {
  quantity: number;
  orderItemId: string;
  note?: string;
  lotId: string;
    expirationDate?: string;
    manufacturingDate?: string;
    facilityId: string;
}

export interface ShipmentUpdateForm {
  shipmentName?: string;
  expectedDeliveryDate?: string;
  note?: string;
}

export interface InventoryItemDetailUpdateForm {
  quantity: number;
  note?: string;
  lotId: string;
  expirationDate?: string;
    manufacturingDate?: string;
    facilityId: string;
}

export interface ShipmentFilters {
  query?: string;
  statusId?: ShipmentStatus;
  shipmentTypeId?: ShipmentType;
  toCustomerId?: string;
  fromSupplierId?: string;
  orderId?: string;
}

// Supplier types
export interface Supplier {
  id: string;
  name: string;
  currentAddressId?: string;
  email?: string;
  phone?: string;
  statusId: SupplierStatus;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
  address?: Address;
}

export interface SupplierFilters {
  query?: string;
  statusId?: SupplierStatus;
}
