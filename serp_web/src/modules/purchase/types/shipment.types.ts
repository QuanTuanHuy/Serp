/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipment types for purchase module
*/

import type {
  ApiResponse,
  PaginatedResponse,
  SearchParams,
} from '@/lib/store/api/types';

// Shipment entity (matches ShipmentEntity.java)
export interface Shipment {
  id: string;
  shipmentTypeId?: string;
  fromSupplierId?: string;
  toCustomerId?: string;
  createdStamp: string;
  createdByUserId?: number;
  orderId: string;
  lastUpdatedStamp: string;
  shipmentName: string;
  statusId: string;
  handledByUserId?: number;
  note?: string;
  expectedDeliveryDate?: string;
  userCancelledId?: number;
  totalWeight: number;
  totalQuantity: number;
  tenantId: number;
}

// Inventory item detail entity (matches InventoryItemDetailEntity.java)
export interface InventoryItemDetail {
  id: string;
  productId: string;
  quantity: number;
  orderItemId?: string;
  shipmentId: string;
  facilityId: string;
  note?: string;
  lotId?: string;
  expirationDate?: string;
  manufacturingDate?: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

// Shipment detail with items (matches ShipmentDetailResponse.java)
export interface ShipmentDetail {
  id: string;
  shipmentTypeId?: string;
  fromSupplierId?: string;
  toCustomerId?: string;
  createdStamp: string;
  createdByUserId?: number;
  orderId: string;
  lastUpdatedStamp: string;
  shipmentName: string;
  statusId: string;
  handledByUserId?: number;
  note?: string;
  expectedDeliveryDate?: string;
  userCancelledId?: number;
  totalWeight: number;
  totalQuantity: number;
  facilityId?: string;
  items: InventoryItemDetail[];
}

// Create shipment request (matches ShipmentCreationForm.java)
export interface CreateShipmentRequest {
  fromSupplierId?: string;
  orderId: string;
  shipmentName: string;
  note?: string;
  expectedDeliveryDate?: string;
  items: Array<{
    productId: string;
    quantity: number;
    orderItemId?: string;
    note?: string;
    lotId?: string;
    expirationDate?: string;
    manufacturingDate?: string;
  }>;
  facilityId: string;
}

// Update shipment request (matches ShipmentUpdateForm.java)
export interface UpdateShipmentRequest {
  shipmentName?: string;
  note?: string;
  expectedDeliveryDate?: string;
}

// Add item to shipment request (matches ShipmentItemAddForm.java)
export interface ShipmentItemAddRequest {
  productId: string;
  quantity: number;
  orderItemId?: string;
  note?: string;
  lotId?: string;
  expirationDate?: string;
  manufacturingDate?: string;
  facilityId: string;
}

// Update inventory item detail request (matches InventoryItemDetailUpdateForm.java)
export interface InventoryItemDetailUpdateRequest {
  quantity: number;
  note?: string;
  lotId?: string;
  expirationDate?: string;
  manufacturingDate?: string;
}

// Update shipment facility request (matches ShipmentFacilityUpdateForm.java)
export interface ShipmentFacilityUpdateRequest {
  facilityId: string;
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
