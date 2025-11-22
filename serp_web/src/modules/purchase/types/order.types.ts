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
  description?: string;
  totalPrice?: number;
  totalQuantity?: number;
  tenantId: number;
}

// Order item entity
export interface OrderItem {
  id: string;
  orderId: string;
  productId: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  statusId: string;
  product?: Product;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

// Order detail with items
export interface OrderDetail extends Order {
  items: OrderItem[];
  supplierName?: string;
  customerName?: string;
}

// Create order request
export interface CreateOrderRequest {
  orderTypeId: string;
  fromSupplierId?: string;
  toCustomerId?: string;
  orderDate: string;
  description?: string;
  items: Array<{
    productId: string;
    quantity: number;
    unitPrice: number;
  }>;
}

// Update order request
export interface UpdateOrderRequest {
  orderTypeId?: string;
  fromSupplierId?: string;
  toCustomerId?: string;
  orderDate?: string;
  description?: string;
}

// Update order item request
export interface UpdateOrderItemRequest {
  quantity?: number;
  unitPrice?: number;
}

// Add product to order request
export interface AddOrderItemRequest {
  productId: string;
  quantity: number;
  unitPrice: number;
}

// Order cancellation request
export interface CancelOrderRequest {
  reason?: string;
}

// Order filters for search
export interface OrderFilters extends SearchParams {
  query?: string;
  statusId?: string;
  orderTypeId?: string;
  fromSupplierId?: string;
  toCustomerId?: string;
  fromDate?: string;
  toDate?: string;
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
