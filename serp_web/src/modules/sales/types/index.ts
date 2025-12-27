// Sales Types & Interfaces (authors: QuanTuanHuy, Description: Part of Serp Project)

// Constants
export type ResponseStatus = "SUCCESS" | "FAILED";
export type EntityType = "PRODUCT" | "SUPPLIER" | "CUSTOMER" | "FACILITY";
export type AddressType = "FACILIY" | "SHIPPING" | "BUSSINESS";
export type CustomerStatus = "ACTIVE" | "INACTIVE";
export type FacilityStatus = "ACTIVE" | "INACTIVE";
export type InventoryItemStatus = "VALID" | "EXPIRED" | "DAMAGED";
export type OrderItemStatus = "CREATED" | "DELIVERED";
export type OrderStatus = "CREATED" | "APPROVED" | "CANCELLED" | "FULLY_DELIVERED";
export type OrderType = "PURCHASE" | "SALES";
export type ProductStatus = "ACTIVE" | "INACTIVE";
export type SaleChannel = "ONLINE" | "PARTNER" | "RETAIL";

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

// Customer related types
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

export interface CustomerCreationForm {
  name: string;
  phone?: string;
  email?: string;
  statusId: CustomerStatus;
  addressType: AddressType;
}

export interface CustomerUpdateForm {
  name?: string;
  phone?: string;
  email?: string;
  statusId?: CustomerStatus;
}

export interface CustomerFilters {
  query?: string;
  statusId?: CustomerStatus;
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

// Inventory Item related types
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

// Product related types
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

// Order related types
export interface OrderItem {
  productId: string;
  orderItemSeqId: number;
  quantity: number;
  tax?: number;
  discount?: number;
  expireAfter?: string;
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

export interface OrderCreationForm {
  toCustomerId: string;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  note?: string;
  orderName?: string;
  priority?: number;
  saleChannelId: SaleChannel;
  items: OrderItem[];
}

export interface OrderUpdateForm {
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  note?: string;
  orderName?: string;
  priority?: number;
  saleChannelId?: SaleChannel;
}

export interface OrderCancellationForm {
  cancellationNote: string;
}

export interface OrderFilters {
  query?: string;
  statusId?: string;
  toCustomerId?: string;
  saleChannelId?: SaleChannel;
  orderDateAfter?: string;
  orderDateBefore?: string;
  deliveryDateAfter?: string;
  deliveryDateBefore?: string;
}
