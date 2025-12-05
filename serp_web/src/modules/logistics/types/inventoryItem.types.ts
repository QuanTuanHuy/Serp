/**
 * Logistics Module - Inventory Item Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Inventory Item domain types
 */

export interface InventoryItem {
  id: string;
  productId: string;
  quantity: number;
  facilityId: string;
  createdStamp: string; // ISO DateTime format
  lotId: string;
  lastUpdatedStamp: string; // ISO DateTime format
  expirationDate: string; // LocalDate format (YYYY-MM-DD)
  manufacturingDate: string; // LocalDate format (YYYY-MM-DD)
  statusId: string;
  receivedDate: string; // LocalDate format (YYYY-MM-DD)
  tenantId: number;
}

export interface CreateInventoryItemRequest {
  productId: string;
  quantity: number;
  facilityId: string;
  lotId?: string;
  expirationDate?: string;
  manufacturingDate?: string;
  statusId?: string;
  receivedDate?: string;
}

export interface UpdateInventoryItemRequest {
  quantity?: number;
  lotId?: string;
  expirationDate?: string;
  manufacturingDate?: string;
  statusId?: string;
  receivedDate?: string;
}

export interface InventoryItemFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  productId?: string;
  facilityId?: string;
  lotId?: string;
  expirationDateFrom?: string;
  expirationDateTo?: string;
  manufacturingDateFrom?: string;
  manufacturingDateTo?: string;
  receivedDateFrom?: string;
  receivedDateTo?: string;
  statusId?: string;
}
