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

export interface UserProfile {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  organizationId: number;
  organizationName: string;
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
  search?: string;
}

// Response types
export type UsersResponse = BasePaginatedResponse<UserProfile>;
export type UserResponse = ApiResponse<UserProfile>;
