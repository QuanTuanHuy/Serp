/**
 * Logistics Module - Product Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Product domain types
 */

export type ProductStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'OUT_OF_STOCK'
  | 'DISCONTINUED';

export interface Product {
  id: string;
  tenantId: number;
  name: string;
  weight: number;
  height: number;
  unit: string;
  costPrice: number;
  wholeSalePrice: number;
  retailPrice: number;
  categoryId: string;
  statusId: string;
  imageId: string;
  extraProps: string;
  vatRate: number;
  skuCode: string;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdStamp: number;
  lastUpdatedStamp: number;
}

export interface CreateProductRequest {
  name: string;
  weight: number;
  height: number;
  unit: string;
  costPrice: number;
  wholeSalePrice: number;
  retailPrice: number;
  categoryId: string;
  statusId: string;
  imageId: string;
  extraProps: string;
  vatRate: number;
  skuCode: string;
}

export interface UpdateProductRequest {
  name: string;
  weight: number;
  height: number;
  unit: string;
  costPrice: number;
  wholeSalePrice: number;
  retailPrice: number;
  statusId: string;
  imageId: string;
  extraProps: string;
  vatRate: number;
  skuCode: string;
}

export interface ProductFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  query?: string;
  categoryId?: string;
  statusId?: string;
}
