/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipment types for purchase module
*/

import type { ApiResponse, PaginatedResponse, SearchParams } from '@/lib/store/api/types';

// Shipment entity
export interface Shipment {
  id: string;
  orderId: string;
  fromFacilityId?: string;
  toFacilityId?: string;
  statusId: string;
  shipmentDate: string;
  estimatedDeliveryDate?: string;
  actualDeliveryDate?: string;
  trackingNumber?: string;
  notes?: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

// Shipment item entity
export interface ShipmentItem {
  id: string;
  shipmentId: string;
  productId: string;
  quantity: number;
  productName?: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

// Shipment detail with items
export interface ShipmentDetail extends Shipment {
  items: ShipmentItem[];
  orderNumber?: string;
  fromFacilityName?: string;
  toFacilityName?: string;
}

// Create shipment request
export interface CreateShipmentRequest {
  orderId: string;
  fromFacilityId?: string;
  toFacilityId?: string;
  shipmentDate: string;
  estimatedDeliveryDate?: string;
  trackingNumber?: string;
  notes?: string;
  items: Array<{
    productId: string;
    quantity: number;
  }>;
}

// Update shipment request
export interface UpdateShipmentRequest {
  fromFacilityId?: string;
  toFacilityId?: string;
  shipmentDate?: string;
  estimatedDeliveryDate?: string;
  actualDeliveryDate?: string;
  trackingNumber?: string;
  notes?: string;
  statusId?: string;
}

// Shipment filters for search
export interface ShipmentFilters extends SearchParams {
  query?: string;
  statusId?: string;
  orderId?: string;
  fromFacilityId?: string;
  toFacilityId?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// Shipment status enum
export enum ShipmentStatus {
  CREATED = 'CREATED',
  READY = 'READY',
  IMPORTED = 'IMPORTED',
  EXPORTED = 'EXPORTED',
}

// API response types
export type ShipmentResponse = ApiResponse<Shipment>;
export type ShipmentsResponse = PaginatedResponse<Shipment>;
export type ShipmentDetailResponse = ApiResponse<ShipmentDetail>;
