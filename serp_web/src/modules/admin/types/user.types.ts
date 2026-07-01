/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User types
 */

import type {
  SearchParams,
  ApiResponse,
  PaginatedResponse as BasePaginatedResponse,
} from '@/lib/store/api/types';

export type UserStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'INVITED'
  | 'SUSPENDED'
  | 'DELETED';

// Backend UserType: OWNER, ADMIN, EMPLOYEE, CONTRACTOR, EXTERNAL, GUEST
export type UserType =
  | 'OWNER'
  | 'ADMIN'
  | 'EMPLOYEE'
  | 'CONTRACTOR'
  | 'EXTERNAL'
  | 'GUEST';

export type AdminTimestamp = string | number;

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
  lastLoginAt?: AdminTimestamp;
  avatarUrl?: string;
  timezone?: string;
  preferredLanguage?: string;
  roles?: string[];
  primaryDepartmentId?: number;
  primaryDepartmentName?: string;
  moduleAccessCount?: number;
  createdAt: AdminTimestamp;
  updatedAt: AdminTimestamp;
}

export interface UserStats {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  suspendedUsers: number;
  invitedUsers: number;
  adminUsers: number;
  newUsersThisMonth: number;
  newUsersLastMonth: number;
}

export interface UserDetailRole {
  id: number;
  name: string;
  scope?: string;
  description?: string;
  moduleName?: string;
}

export interface UserDetailDepartment {
  id: number;
  name?: string;
  isPrimary?: boolean;
  jobTitle?: string;
}

export interface UserDetailModuleAccess {
  moduleId: number;
  moduleName?: string;
  moduleCode?: string;
  isActive?: boolean;
  grantedAt?: AdminTimestamp;
}

export interface UserDetailResponse {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  avatarUrl?: string;
  userType: UserType;
  status: UserStatus;
  lastLoginAt?: AdminTimestamp;
  createdAt?: AdminTimestamp;
  timezone?: string;
  preferredLanguage?: string;
  organizationId: number;
  organizationName?: string;
  roles: UserDetailRole[];
  departments: UserDetailDepartment[];
  moduleAccesses: UserDetailModuleAccess[];
}

export interface DepartmentOption {
  id: number;
  name: string;
}

// Filters
export interface UserFilters extends SearchParams {
  status?: UserStatus;
  organizationId?: number;
  userType?: UserType;
  roleId?: number;
  departmentId?: number;
  moduleId?: number;
  search?: string;
}

// Response types
export type UsersResponse = BasePaginatedResponse<UserProfile>;
export type UserResponse = ApiResponse<UserProfile>;
export type UserStatsResponse = ApiResponse<UserStats>;
export type UserDetailApiResponse = ApiResponse<UserDetailResponse>;

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
