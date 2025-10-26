/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User types
 */

import type {
  SearchParams,
  ApiResponse,
  PaginatedResponse as BasePaginatedResponse,
} from '@/lib/store/api/types';

// Keep frontend union in sync with backend enums (account service)
// Backend UserStatus: ACTIVE, INACTIVE, INVITED, SUSPENDED, DELETED
// We also accept 'PENDING' from legacy UI and map it to INVITED when calling APIs
export type UserStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'INVITED'
  | 'SUSPENDED'
  | 'DELETED'
  | 'PENDING';

// Backend UserType: OWNER, ADMIN, EMPLOYEE, CONTRACTOR, EXTERNAL, GUEST
export type UserType =
  | 'OWNER'
  | 'ADMIN'
  | 'EMPLOYEE'
  | 'CONTRACTOR'
  | 'EXTERNAL'
  | 'GUEST';

// User

export interface UserProfile {
  id: number;
  keycloakId?: string;
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

// Requests
export interface UpdateUserInfoRequest {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  avatarUrl?: string;
  timezone?: string;
  preferredLanguage?: string;
  keycloakUserId?: string;
}

export interface CreateUserForOrganizationRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  userType?: UserType; // default EMPLOYEE on backend
  roleIds?: number[];
}
