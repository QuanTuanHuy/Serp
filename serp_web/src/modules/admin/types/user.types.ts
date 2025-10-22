/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User types
 */

import type {
  SearchParams,
  ApiResponse,
  PaginatedResponse as BasePaginatedResponse,
} from '@/lib/store/api/types';

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED';

export type UserType = 'INTERNAL' | 'EXTERNAL' | 'SYSTEM';

// User
export interface AdminUser {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  keycloakId: string;
  isSuperAdmin: boolean;
  primaryOrganizationId?: number;
  primaryDepartmentId?: number;
  organizationName?: string;
  userType: UserType;
  status: UserStatus;
  lastLoginAt?: string;
  avatarUrl?: string;
  timezone?: string;
  preferredLanguage?: string;
  createdAt: string;
  updatedAt: string;
}

// Filters
export interface UserFilters extends SearchParams {
  status?: UserStatus;
  organizationId?: number;
}

// Response types
export type UsersResponse = BasePaginatedResponse<AdminUser>;
export type UserResponse = ApiResponse<AdminUser>;
