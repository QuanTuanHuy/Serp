/**
 * Logistics Module - Supplier Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Supplier domain types
 */

import type { Address } from './address.types';

export interface Supplier {
  id: string;
  tenantId: number;
  name: string;
  phone: string;
  email: string;
  statusId: string;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdStamp: number;
  lastUpdatedStamp: number;
}

export interface SupplierDetail extends Supplier {
  address?: Address;
}

export interface SupplierFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  statusId?: string;
}
