// Sales Types & Interfaces (authors: QuanTuanHuy, Description: Part of Serp Project)

// Base types
export interface PaginationParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  data: T[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface APIResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

// Address related types
export interface Address {
  id?: string;
  entityId?: string;
  entityType?: string;
  addressType?: string;
  latitude?: number;
  longitude?: number;
  fullAddress?: string;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  tenantId?: number;
  default?: boolean;
}

export interface AddressCreationForm {
  entityId: string;
  entityType: string;
  addressType?: string;
  latitude?: number;
  longitude?: number;
  fullAddress: string;
  default?: boolean;
}

export interface AddressUpdateForm {
  addressType?: string;
  latitude?: number;
  longitude?: number;
  fullAddress?: string;
  default?: boolean;
}

// Category related types
export interface Category {
  id?: string;
  name?: string;
  tenantId?: number;
  createdStamp?: string;
  lastUpdatedStamp?: string;
}

export interface CategoryForm {
  name: string;
}

export interface CategoryFilters {
  query?: string;
  name?: string;
}

// Customer related types
export interface Customer {
  id?: string;
  name?: string;
  currentAddressId?: string;
  statusId?: string;
  phone?: string;
  email?: string;
  tenantId?: number;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  address?: Address;
}

export interface CustomerCreationForm {
  name: string;
  phone?: string;
  email?: string;
  statusId?: string;
}

export interface CustomerUpdateForm {
  name?: string;
  currentAddressId?: string;
  phone?: string;
  email?: string;
  statusId?: string;
}

export interface CustomerFilters {
  query?: string;
  name?: string;
  phone?: string;
  email?: string;
  statusId?: string;
}

// Facility related types
export interface Facility {
  id?: string;
  name?: string;
  addressId?: string;
  facilityTypeId?: string;
  statusId?: string;
  capacity?: number;
  phone?: string;
  email?: string;
  tenantId?: number;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  address?: Address;
}

export interface FacilityCreationForm {
  name: string;
  facilityTypeId?: string;
  statusId?: string;
  capacity?: number;
  phone?: string;
  email?: string;
}

export interface FacilityUpdateForm {
  name?: string;
  addressId?: string;
  facilityTypeId?: string;
  statusId?: string;
  capacity?: number;
  phone?: string;
  email?: string;
}

export interface FacilityFilters {
  query?: string;
  name?: string;
  facilityTypeId?: string;
  statusId?: string;
}

// Inventory Item related types
export interface InventoryItem {
  id?: string;
  productId?: string;
  facilityId?: string;
  quantityOnHand?: number;
  quantityAvailable?: number;
  quantityReserved?: number;
  minStock?: number;
  maxStock?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  statusId?: string;
  tenantId?: number;
  createdStamp?: string;
  lastUpdatedStamp?: string;
}

export interface InventoryItemDetail extends InventoryItem {
  productName?: string;
  facilityName?: string;
}

export interface InventoryItemCreationForm {
  productId: string;
  facilityId: string;
  quantityOnHand?: number;
  minStock?: number;
  maxStock?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  statusId?: string;
}

export interface InventoryItemUpdateForm {
  quantityOnHand?: number;
  minStock?: number;
  maxStock?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  statusId?: string;
}

export interface InventoryItemFilters {
  query?: string;
  productId?: string;
  facilityId?: string;
  statusId?: string;
}

// Product related types
export interface Product {
  id?: string;
  name?: string;
  weight?: number;
  height?: number;
  unit?: string;
  costPrice?: number;
  wholeSalePrice?: number;
  retailPrice?: number;
  categoryId?: string;
  statusId?: string;
  imageId?: string;
  extraProps?: string;
  createdStamp?: string;
  lastUpdatedStamp?: string;
  vatRate?: number;
  skuCode?: string;
  tenantId?: number;
  quantityAvailable?: number;
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
  statusId?: string;
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
  statusId?: string;
  imageId?: string;
  extraProps?: string;
  vatRate?: number;
  skuCode?: string;
}

export interface ProductFilters {
  query?: string;
  categoryId?: string;
  statusId?: string;
  name?: string;
  skuCode?: string;
}

// Order related types
export interface OrderItem {
  productId: string;
  quantity: number;
  unitPrice?: number;
  discount?: number;
}

export interface OrderItemEntity {
  id?: string;
  orderId?: string;
  productId?: string;
  quantity?: number;
  unitPrice?: number;
  totalPrice?: number;
  discount?: number;
  note?: string;
  createdStamp?: string;
  lastUpdatedStamp?: string;
}

export interface Order {
  id?: string;
  orderTypeId?: string;
  fromSupplierId?: string;
  toCustomerId?: string;
  createdByUserId?: number;
  createdStamp?: string;
  orderDate?: string;
  statusId?: string;
  lastUpdatedStamp?: string;
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
  tenantId?: number;
  items?: OrderItemEntity[];
}

export interface OrderCreationForm {
  orderTypeId: string;
  toCustomerId?: string;
  fromSupplierId?: string;
  orderDate?: string;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  note?: string;
  orderName?: string;
  priority?: number;
  deliveryAddressId?: string;
  deliveryPhone?: string;
  saleChannelId?: string;
  deliveryFullAddress?: string;
  items: OrderItem[];
}

export interface OrderUpdateForm {
  orderTypeId?: string;
  toCustomerId?: string;
  fromSupplierId?: string;
  orderDate?: string;
  deliveryBeforeDate?: string;
  deliveryAfterDate?: string;
  note?: string;
  orderName?: string;
  priority?: number;
  deliveryAddressId?: string;
  deliveryPhone?: string;
  saleChannelId?: string;
  deliveryFullAddress?: string;
}

export interface OrderCancellationForm {
  cancellationNote: string;
}

export interface OrderFilters {
  query?: string;
  orderTypeId?: string;
  statusId?: string;
  toCustomerId?: string;
  fromSupplierId?: string;
  orderDateFrom?: string;
  orderDateTo?: string;
  priority?: number;
}
