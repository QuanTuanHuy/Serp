/*
Author: QuanTuanHuy
Description: Part of Serp Project - Supplier types for purchase module
*/

import type { ApiResponse, PaginatedResponse, SearchParams } from '@/lib/store/api/types';
import type { Address } from './address.types';

// Supplier entity
export interface Supplier {
  id: string;
  name: string;
  currentAddressId?: string;
  email?: string;
  phone?: string;
  statusId: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

// Supplier detail response (includes address from address.types.ts)
export interface SupplierDetail extends Supplier {
  address?: Address;
}

// Create supplier request
export interface CreateSupplierRequest {
  name: string;
  email?: string;
  phone?: string;
  statusId: string;
  address?: {
    address: string;
    city?: string;
    province?: string;
    country?: string;
    postalCode?: string;
  };
}

// Update supplier request
export interface UpdateSupplierRequest {
  name?: string;
  email?: string;
  phone?: string;
  statusId?: string;
}

// Supplier filters for search
export interface SupplierFilters extends SearchParams {
  query?: string;
  statusId?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// API response types
export type SupplierResponse = ApiResponse<Supplier>;
export type SuppliersResponse = PaginatedResponse<Supplier>;
export type SupplierDetailResponse = ApiResponse<SupplierDetail>;
