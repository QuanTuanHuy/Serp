/**
 * Logistics Module - Inventory Item Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Inventory Item domain types
 */

export interface InventoryItem {
  id: string;
  tenantId: number;
  productId: string;
  quantity: number;
  facilityId: string;
  expirationDate: string; // LocalDate format
  manufacturingDate: string; // LocalDate format
  statusId: string;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdStamp: number;
  lastUpdatedStamp: number;
}

export interface CreateInventoryItemRequest {
  productId: string;
  quantity: number;
  facilityId: string;
  expirationDate: string;
  manufacturingDate: string;
  statusId: string;
}

export interface UpdateInventoryItemRequest {
  quantity: number;
  expirationDate: string;
  manufacturingDate: string;
  statusId: string;
}

export interface InventoryItemFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  productId?: string;
  facilityId?: string;
  expirationDateFrom?: string;
  expirationDateTo?: string;
  manufacturingDateFrom?: string;
  manufacturingDateTo?: string;
  statusId?: string;
}
