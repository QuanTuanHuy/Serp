/**
 * Logistics Module - Order Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Order domain types
 */

export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';
export type SaleChannel = 'ONLINE' | 'OFFLINE' | 'WHOLESALE' | 'RETAIL';

export interface OrderItem {
  id: string;
  tenantId: number;
  orderId: string;
  orderItemSeqId: number;
  productId: string;
  quantity: number;
  price: number; // Unit price
  amount: number; // Total amount for this item
  discount: number;
  tax: number;
  unit: string;
  statusId: string;
  createdStamp: string; // LocalDateTime
  lastUpdatedStamp: string; // LocalDateTime
}

export interface Order {
  id: string;
  tenantId?: number;
  orderName: string;
  orderTypeId: string;
  toCustomerId: string | null;
  fromSupplierId: string | null;
  saleChannelId: string;
  statusId: string;
  orderDate: string; // LocalDate
  deliveryAfterDate: string; // LocalDate
  deliveryBeforeDate: string; // LocalDate
  deliveryAddressId: string | null;
  deliveryFullAddress: string | null;
  deliveryPhone: string | null;
  totalAmount: number | null;
  totalQuantity: number;
  priority: number;
  note: string;
  cancellationNote: string;
  costs: any | null;
  createdByUserId: number;
  userApprovedId: number | null;
  userCancelledId: number | null;
  createdStamp: string; // LocalDateTime
  lastUpdatedStamp: string; // LocalDateTime
}

export interface OrderDetail extends Order {
  orderItems: OrderItem[];
}

export interface OrderFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  statusId?: string;
  orderTypeId?: string;
  toCustomerId?: string;
  fromSupplierId?: string;
  saleChannelId?: string;
  orderDateAfter?: string;
  orderDateBefore?: string;
  deliveryBefore?: string;
  deliveryAfter?: string;
}
