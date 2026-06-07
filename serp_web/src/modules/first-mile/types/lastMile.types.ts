/**
 * Author: SERP Project
 * Description: Part of Serp Project - Last-mile delivery types
 */

// ─── Enums ──────────────────────────────────────────────────────────────────

export type DeliveryManifestStatus =
  | 'CREATED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export type DeliveryOrderStatus =
  | 'PENDING'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'FAILED'
  | 'RESCHEDULED'
  | 'RETURNED';

// ─── Response types ──────────────────────────────────────────────────────────

export interface InboundOrderResponse {
  orderId: number;
  orderCode: string;
  status: string;
  destinationPostOfficeCode: string;
  receiverName: string;
  receiverPhone: string;
  receiverAddressDetail: string;
  codAmount: number;
  totalShippingFee: number;
  feePayer: string;
}

export interface DeliveryManifestOrderResponse {
  id: number;
  orderId: number;
  orderCode: string;
  sequence: number;
  deliveryAttemptCount: number;
  status: DeliveryOrderStatus;
  receiverName: string;
  receiverPhone: string;
  receiverAddressDetail: string;
  receiverWardCode?: string;
  receiverProvinceCode?: string;
  receiverLat?: number;
  receiverLng?: number;
  codAmount: number;
  codCollected: number;
  shippingFee: number;
  shippingFeeCollected: number;
  feePayer?: string;
  proofPhotoUrl?: string;
  failureReason?: string;
  deliveredAt?: string;
  note?: string;
}

export interface DeliveryManifestResponse {
  id: number;
  manifestCode: string;
  postOfficeCode: string;
  courierId?: number;
  courierName?: string;
  vehicleId?: string;
  status: DeliveryManifestStatus;
  plannedDate: string;
  plannedDepartureAt?: string;
  actualDepartureAt?: string;
  actualReturnAt?: string;
  totalOrders: number;
  deliveredCount: number;
  failedCount: number;
  totalCodAmount: number;
  collectedCodAmount: number;
  totalShippingFee: number;
  collectedShippingFee: number;
  note?: string;
  createdAt?: string;
  orders: DeliveryManifestOrderResponse[];
}

// ─── Request types ──────────────────────────────────────────────────────────

export interface SortInboundOrdersRequest {
  postOfficeCode: string;
  orderCodes: string[];
}

export interface CreateDeliveryManifestRequest {
  postOfficeCode: string;
  courierId?: number;
  vehicleId?: string;
  plannedDate: string;
  plannedDepartureAt?: string;
  orderCodes: string[];
  note?: string;
}

export interface ConfirmDeliveryRequest {
  proofPhotoUrl?: string;
  codCollected?: number;
  shippingFeeCollected?: number;
  note?: string;
  deliveredAt?: string;
}

export interface ConfirmDeliveryFailureRequest {
  failureReason: string;
  note?: string;
  currentLat?: number;
  currentLng?: number;
}

export interface ReturnToSenderRequest {
  note?: string;
}

// ─── Filter types ──────────────────────────────────────────────────────────

export interface DeliveryManifestListFilters {
  postOfficeCode: string;
  status?: DeliveryManifestStatus;
  date?: string;
}
