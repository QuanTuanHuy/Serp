/**
 * Logistics Module - Shipment Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Shipment domain types
 */

export type ShipmentStatus = 'PENDING' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';

export interface InventoryItemDetail {
  id: string;
  tenantId: number;
  productId: string;
  quantity: number;
  shipmentId: string;
  orderItemId: string;
  lotId: string;
  note: string;
  expirationDate: string; // LocalDate
  manufacturingDate: string; // LocalDate
  facilityId: string;
  unit: string;
  price: number;
  createdStamp: string; // LocalDateTime
  lastUpdatedStamp: string; // LocalDateTime
}

export interface Shipment {
  id: string;
  tenantId?: number;
  shipmentName: string;
  shipmentTypeId: string;
  statusId: string;
  fromSupplierId: string;
  toCustomerId: string;
  orderId: string;
  facilityId: string;
  note: string;
  expectedDeliveryDate: string; // LocalDate
  createdByUserId: number;
  handledByUserId: number | null;
  createdStamp: string; // LocalDateTime
  lastUpdatedStamp: string; // LocalDateTime
}

export interface ShipmentDetail extends Shipment {
  items: InventoryItemDetail[];
  inventoryDetails?: InventoryItemDetail[]; // Deprecated, use items instead
}

export interface InventoryItemDetailForm {
  productId: string;
  quantity: number;
  orderItemId: string;
  lotId: string;
  expirationDate: string;
  manufacturingDate: string;
  facilityId: string;
}

export interface CreateShipmentRequest {
  shipmentTypeId: string;
  fromSupplierId: string | null;
  toCustomerId: string | null;
  orderId: string;
  shipmentName?: string;
  note: string;
  expectedDeliveryDate: string;
  items: InventoryItemDetailForm[];
}

export interface UpdateShipmentRequest {
  shipmentName: string;
  note: string;
  expectedDeliveryDate: string;
}

export interface UpdateInventoryItemDetailRequest {
  quantity: number;
  lotId: string;
  expirationDate: string;
  manufacturingDate: string;
  facilityId: string;
}

export interface ShipmentFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  statusId?: string;
  shipmentTypeId?: string;
  toCustomerId?: string;
  fromSupplierId?: string;
  orderId?: string;
}
