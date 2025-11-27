/*
Author: QuanTuanHuy
Description: Part of Serp Project - Facility types for purchase module
*/

import type {
  ApiResponse,
  PaginatedResponse,
  SearchParams,
} from '@/lib/store/api/types';
import type { Address } from './address.types';

// Facility entity
export interface Facility {
  id: string;
  name: string;
  type: string;
  currentAddressId?: string;
  statusId: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

// Facility detail with address
export interface FacilityDetail extends Facility {
  address?: Address;
}

// Create facility request
export interface CreateFacilityRequest {
  name: string;
  type: string;
  statusId: string;
  address?: {
    address: string;
    city?: string;
    province?: string;
    country?: string;
    postalCode?: string;
  };
}

// Update facility request
export interface UpdateFacilityRequest {
  name?: string;
  type?: string;
  statusId?: string;
}

// Facility filters for search
export interface FacilityFilters extends SearchParams {
  query?: string;
  type?: string;
  statusId?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// Facility status enum
export enum FacilityStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

// API response types
export type FacilityResponse = ApiResponse<Facility>;
export type FacilitiesResponse = PaginatedResponse<Facility>;
export type FacilityDetailResponse = ApiResponse<FacilityDetail>;
