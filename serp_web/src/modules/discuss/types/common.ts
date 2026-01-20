/*
Author: QuanTuanHuy
Description: Part of Serp Project - Common types for Discuss module
*/

// ==================== Base Entities ====================

export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserInfo {
  id: string;
  name: string;
  email: string;
  avatarUrl?: string;
}

// ==================== Pagination ====================

export interface PaginationParams {
  page: number;
  limit: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

// Backend PaginatedResponse structure
export interface PaginatedResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// ==================== API Response ====================

export interface APIResponse<T> {
  status: string; // "success" | "error" from backend
  code: number; // HTTP status code (200, 201, 400, etc.)
  message: string;
  data: T;
}

// Helper to check if response is successful
export const isSuccessResponse = (response: APIResponse<any>): boolean => {
  return response.code >= 200 && response.code < 300;
};
