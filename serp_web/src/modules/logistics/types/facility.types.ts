/**
 * Logistics Module - Facility Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Facility domain types
 */

import type { Address } from './address.types';

export type FacilityStatus = 'ACTIVE' | 'INACTIVE' | 'UNDER_MAINTENANCE';

export interface Facility {
  id: string;
  tenantId: number;
  name: string;
  phone: string;
  isDefault: boolean;
  statusId: string;
  postalCode: string;
  length: number;
  width: number;
  height: number;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdStamp: number;
  lastUpdatedStamp: number;
}

export interface FacilityDetail extends Facility {
  address?: Address;
}

export interface CreateFacilityRequest {
  name: string;
  phone: string;
  statusId: string;
  postalCode: string;
  length: number;
  width: number;
  height: number;
  addressType: string;
  latitude: number;
  longitude: number;
  isAddressDefault: boolean;
  fullAddress: string;
}

export interface UpdateFacilityRequest {
  name: string;
  isDefault: boolean;
  statusId: string;
  phone: string;
  postalCode: string;
  length: number;
  width: number;
  height: number;
}

export interface FacilityFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  statusId?: string;
}
