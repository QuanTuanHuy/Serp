// Purchase Types & Interfaces (authors: QuanTuanHuy, Description: Part of Serp Project)

// Constants
export type ResponseStatus = 'SUCCESS' | 'FAILED';
export type EntityType = 'PRODUCT' | 'SUPPLIER' | 'CUSTOMER' | 'FACILITY';
export type AddressType = 'FACILITY' | 'SHIPPING' | 'BUSINESS';
export type SupplierStatus = 'ACTIVE' | 'INACTIVE';
export type FacilityStatus = 'ACTIVE' | 'INACTIVE';
export type OrderStatus =
  | 'CREATED'
  | 'APPROVED'
  | 'CANCELLED'
  | 'FULLY_DELIVERED';
export type OrderType = 'PURCHASE' | 'SALES';
export type ProductStatus = 'ACTIVE' | 'INACTIVE';
export type ShipmentStatus =
  | 'CREATED'
  | 'IN_TRANSIT'
  | 'DELIVERED'
  | 'CANCELLED';
export type ShipmentType = 'INBOUND' | 'OUTBOUND';

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

// Address related types
export interface Address {
  id: string;
  entityId: string;
  entityType: EntityType;
  addressType: AddressType;
  latitude?: number;
  longitude?: number;
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
  latitude?: number;
  longitude?: number;
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

// Category related types
export interface Category {
  id: string;
  name: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

export interface CategoryForm {
  name: string;
}

export interface CategoryFilters {
  query?: string;
}

// Supplier related types
export interface Supplier {
  id: string;
  name: string;
  currentAddressId?: string;
  email?: string;
  phone?: string;
  statusId: SupplierStatus;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
  address?: Address;
}

export interface SupplierCreationForm {
  name: string;
  email?: string;
  phone?: string;
  statusId: SupplierStatus;
  addressType: AddressType;
  latitude?: number;
  longitude?: number;
  fullAddress: string;
}

export interface SupplierUpdateForm {
  name?: string;
  email?: string;
  phone?: string;
  statusId?: SupplierStatus;
}

export interface SupplierFilters {
  query?: string;
  statusId?: SupplierStatus;
}

// Facility related types
export interface Facility {
  id: string;
  name: string;
  statusId: FacilityStatus;
  currentAddressId?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  phone?: string;
  postalCode: string;
  length?: number;
  width?: number;
  height?: number;
  tenantId: number;
  address?: Address;
  default: boolean;
}

export interface FacilityCreationForm {
  name: string;
  phone?: string;
  statusId: FacilityStatus;
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

// Product related types
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
  statusId: ProductStatus;
  imageId?: string;
  extraProps?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  vatRate?: number;
  skuCode?: string;
  tenantId: number;
}

export interface ProductCreationForm {
  name: string;
  weight?: number;
  height?: number;
  unit?: string;
  costPrice?: number;
  wholeSalePrice?: number;
  retailPrice?: number;
  categoryId?: string;
  statusId: ProductStatus;
  imageId?: string;
  extraProps?: string;
  vatRate?: number;
  skuCode?: string;
}

export interface ProductUpdateForm {
  name?: string;
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
  vatRate?: number;
  skuCode?: string;
}

export interface ProductFilters {
  query?: string;
  categoryId?: string;
  statusId?: ProductStatus;
}

// Order related types
export interface OrderItem {
  productId: string;
  quantity: number;
  unitPrice: number;
}

export interface OrderItemEntity {
  id: string;
  orderId: string;
  productId: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  statusId: string;
  note?: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
}

export interface Order {
  id: string;
  orderTypeId: OrderType;
  fromSupplierId?: string;
  toCustomerId?: string;
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
  saleChannelId?: string;
  deliveryFullAddress?: string;
  totalQuantity?: number;
  totalAmount?: number;
  costs?: string;
  userApprovedId?: number;
  userCancelledId?: number;
  cancellationNote?: string;
  tenantId: number;
  items?: OrderItemEntity[];
  shipments?: Shipment[];
}

export interface OrderCreationForm {
  orderTypeId: OrderType;
  fromSupplierId?: string;
  toCustomerId?: string;
  orderDate: string;
  orderName?: string;
  priority?: number;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  deliveryAddressId?: string;
  deliveryPhone?: string;
  deliveryFullAddress?: string;
  saleChannelId?: string;
  note?: string;
  items: OrderItem[];
}

export interface OrderUpdateForm {
  orderName?: string;
  priority?: number;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  deliveryAddressId?: string;
  deliveryPhone?: string;
  deliveryFullAddress?: string;
  note?: string;
}

export interface OrderItemUpdateForm {
  quantity?: number;
  unitPrice?: number;
  note?: string;
}

export interface OrderCancellationForm {
  cancellationNote: string;
}

export interface OrderFilters {
  query?: string;
  statusId?: OrderStatus;
  orderTypeId?: OrderType;
  fromSupplierId?: string;
  toCustomerId?: string;
  fromDate?: string;
  toDate?: string;
}

// Shipment related types
export interface InventoryItemDetailEntity {
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
}

export interface Shipment {
  id: string;
  shipmentTypeId: ShipmentType;
  fromSupplierId?: string;
  toCustomerId?: string;
  createdStamp: string;
  createdByUserId: number;
  orderId: string;
  lastUpdatedStamp: string;
  shipmentName?: string;
  statusId: ShipmentStatus;
  handledByUserId?: number;
  note?: string;
  expectedDeliveryDate?: string;
  userCancelledId?: number;
  totalWeight?: number;
  totalQuantity?: number;
  tenantId: number;
  items?: InventoryItemDetailEntity[];
}

export interface ShipmentFilters {
  query?: string;
  statusId?: ShipmentStatus;
  orderId?: string;
  fromDate?: string;
  toDate?: string;
}
