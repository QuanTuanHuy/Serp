/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order types for purchase module
*/

import type { ApiResponse, PaginatedResponse, SearchParams } from '@/lib/store/api/types';
import type { Product } from './product.types';

// Order entity
export interface Order {
  id: string;
  orderTypeId: string;
  fromSupplierId?: string;
  toCustomerId?: string;
  createdByUserId: number;
  createdStamp: string;
  orderDate: string;
  statusId: string;
  lastUpdatedStamp: string;
  deliveryBeforeDate: string;
  deliveryAfterDate: string;
  note?: string;
  orderName: string;
  priority: number;
  saleChannelId: string;
  userApprovedId?: number;
  userCancelledId?: number;
  cancellationNote?: string;
}

// Order item entity
export interface OrderItem {
  id: string;
  orderId: string;
  orderItemSeqId: number;
  productId: string;
  quantity: number;
  amount: number;
  statusId: string;
  createdStamp: string;
  lastUpdatedStamp?: string;
  price: number;
  tax: number;
  discount: number;
  unit: string;
}

// Order detail with items
export interface OrderDetail extends Order {
  orderItems: OrderItem[];
  id: string;
  orderTypeId: string;
  fromSupplierId?: string;
  toCustomerId?: string;
  createdByUserId: number;
  createdStamp: string;
  orderDate: string;
  statusId: string;
  lastUpdatedStamp: string;
  deliveryBeforeDate: string;
  deliveryAfterDate: string;
  note?: string;
  orderName: string;
  priority: number;
  saleChannelId: string;
  userApprovedId?: number;
  userCancelledId?: number;
  cancellationNote?: string;
}

// Create order request
export interface CreateOrderRequest {
  fromSupplierId: string;
  deliveryBeforeDate: string;
  deliveryAfterDate: string;
  orderName: string;
  note?: string;
  priority: number;
  saleChannelId?: string;
  orderItems: Array<{
    productId: string;
    orderItemSeqId: number;
    quantity: number;
    tax: number;
    discount: number;
  }>;
}

// Update order request
export interface UpdateOrderRequest {
  deliveryBeforeDate: string;
  deliveryAfterDate: string;
  orderName: string;
  note?: string;
  priority: number;
  saleChannelId?: string;
}

// Update order item request
export interface UpdateOrderItemRequest {
  orderItemSeqId: number;
  quantity?: number;
  tax?: number;
  discount?: number;
}

// Add product to order request
export interface AddOrderItemRequest {
  productId: string;
  orderItemSeqId: number;
  quantity: number;
  tax: number;
  discount: number;
}

// Order cancellation request
export interface CancelOrderRequest {
  note: string;
}

// Order filters for search
export interface OrderFilters extends SearchParams {
  query?: string;
  statusId?: string;
  fromSupplierId?: string;
  saleChannelId?: string;
  orderDateAfter?: string;
  orderDateBefore?: string;
  deliveryAfter?: string;
  deliveryBefore?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// Order status enum
export enum OrderStatus {
  CREATED = 'CREATED',
  APPROVED = 'APPROVED',
  CANCELLED = 'CANCELLED',
  READY_FOR_DELIVERY = 'READY_FOR_DELIVERY',
  FULLY_DELIVERED = 'FULLY_DELIVERED',
}

// API response types
export type OrderResponse = ApiResponse<Order>;
export type OrdersResponse = PaginatedResponse<Order>;
export type OrderDetailResponse = ApiResponse<OrderDetail>;
export type OrderItemResponse = ApiResponse<OrderItem>;
