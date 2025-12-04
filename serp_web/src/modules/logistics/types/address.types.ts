/**
 * Logistics Module - Address Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Address domain types
 */

export type EntityType = 'CUSTOMER' | 'SUPPLIER' | 'FACILITY' | 'OTHER';
export type AddressType = 'SHIPPING' | 'BILLING' | 'WAREHOUSE' | 'OFFICE';

export interface Address {
  id: string;
  tenantId: number;
  entityId: string;
  entityType: EntityType;
  addressType: AddressType;
  latitude: number;
  longitude: number;
  isDefault: boolean;
  fullAddress: string;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdStamp: number;
  lastUpdatedStamp: number;
}

export interface CreateAddressRequest {
  entityId: string;
  entityType: string;
  addressType: string;
  latitude: number;
  longitude: number;
  isDefault: boolean;
  fullAddress: string;
}

export interface UpdateAddressRequest {
  addressType: string;
  latitude: number;
  longitude: number;
  isDefault: boolean;
  fullAddress: string;
}
