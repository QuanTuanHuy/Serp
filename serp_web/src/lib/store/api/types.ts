/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Common types and utilities for API calls
 */

// Common API response types
export interface ApiResponse<T = any> {
  code: number;
  status: string;
  message: string;
  data: T;
}

export interface PaginatedResponse<T = any> {
  code: number;
  status: string;
  message: string;
  data: {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
  };
}

// Common request types
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

export interface SearchParams extends PaginationParams {
  search?: string;
  filters?: Record<string, any>;
}

// Error response type
export interface ApiError {
  code: number;
  status: string;
  message: string;
  data?: {
    errors?: Record<string, string[]>;
    details?: any;
  };
}

// Service endpoints configuration
export const SERVICE_ENDPOINTS = {
  AUTH: '/auth',
  ACCOUNT: '/account',
  PTM_TASK: '/ptm-task',
  PTM_SCHEDULE: '/ptm-schedule',
  CRM: '/crm',
  INVENTORY: '/inventory',
  ACCOUNTING: '/accounting',
  LOGGING: '/logging',
} as const;

// HTTP methods
export const HTTP_METHODS = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  PATCH: 'PATCH',
  DELETE: 'DELETE',
} as const;

// Common query tags for cache invalidation
export const CACHE_TAGS = {
  User: 'User',
  Task: 'Task',
  Project: 'Project',
  Schedule: 'Schedule',
  Customer: 'Customer',
  Lead: 'Lead',
  Contact: 'Contact',
  Product: 'Product',
  Invoice: 'Invoice',
  Order: 'Order',
  Inventory: 'Inventory',
  Organization: 'Organization',
} as const;
