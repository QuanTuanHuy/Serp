/*
Author: QuanTuanHuy
Description: Part of Serp Project - Address types for purchase module
*/

import type { ApiResponse, PaginatedResponse, SearchParams } from '@/lib/store/api/types';

// Address entity
export interface Address {
  id: string;
  entityId: string;
  entityType: string;
  addressType: string;
  latitude: number;
  longitude: number;
  isDefault: boolean;
  fullAddress: string;
  createdStamp: string;
  lastUpdatedStamp: string;
  tenantId: number;
}

// Create address request
export interface CreateAddressRequest {
  entityId: string;
  entityType: string;
  addressType: string;
  latitude: number;
  longitude: number;
  isDefault: boolean;
  fullAddress: string;
}

// Update address request
export interface UpdateAddressRequest {
  entityId?: string;
  entityType?: string;
  addressType?: string;
  latitude?: number;
  longitude?: number;
  isDefault?: boolean;
  fullAddress?: string;
}

// Address filters for search
export interface AddressFilters extends SearchParams {
  entityId?: string;
  entityType?: string;
  addressType?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// Response types
export type AddressResponse = ApiResponse<Address>;
export type AddressesResponse = PaginatedResponse<Address>;
