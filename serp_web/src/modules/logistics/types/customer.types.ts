/**
 * Logistics Module - Customer Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Customer domain types
 */

export interface Customer {
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

export interface CustomerFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  statusId?: string;
}
