/*
Author: QuanTuanHuy
Description: Part of Serp Project - Product types for purchase module
*/

import type { ApiResponse, PaginatedResponse, SearchParams } from '@/lib/store/api/types';

// Product entity
export interface Product {
  id: string;
  name: string;
  weight: number;
  height: number;
  unit: string;
  costPrice: number;
  wholeSalePrice: number;
  retailPrice: number;
  categoryId?: string;
  statusId: string;
  imageId?: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

// Category entity
export interface Category {
  id: string;
  name: string;
  parentCategoryId?: string;
  tenantId: number;
  createdStamp: string;
  lastUpdatedStamp: string;
}

// Create product request
export interface CreateProductRequest {
  name: string;
  weight?: number;
  height?: number;
  unit: string;
  costPrice: number;
  wholeSalePrice?: number;
  retailPrice?: number;
  categoryId?: string;
  statusId: string;
  imageId?: string;
}

// Update product request
export interface UpdateProductRequest {
  name?: string;
  weight?: number;
  height?: number;
  unit?: string;
  costPrice?: number;
  wholeSalePrice?: number;
  retailPrice?: number;
  categoryId?: string;
  statusId?: string;
  imageId?: string;
}

// Product filters for search
export interface ProductFilters extends SearchParams {
  query?: string;
  categoryId?: string;
  statusId?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// API response types
export type ProductResponse = ApiResponse<Product>;
export type ProductsResponse = PaginatedResponse<Product>;
export type CategoryResponse = ApiResponse<Category>;
export type CategoriesResponse = PaginatedResponse<Category>;
