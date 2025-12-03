/**
 * Logistics Module - Category Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Category domain types
 */

export interface Category {
  id: string;
  tenantId: number;
  name: string;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdStamp: number;
  lastUpdatedStamp: number;
}

export interface CreateCategoryRequest {
  name: string;
}

export interface UpdateCategoryRequest {
  name: string;
}

export interface CategoryFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  statusId?: string;
}
