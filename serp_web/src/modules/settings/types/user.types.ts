/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User management types for settings
 */

import type { UserType, UserStatus } from '@/modules/admin/types';
import type { ApiResponse, PaginatedResponse } from '@/lib/store/api/types';

// ==================== User Stats ====================

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

export type UserStatsResponse = ApiResponse<UserStats>;

// ==================== User Detail ====================

export interface RoleDetail {
  id: number;
  name: string;
  scope?: string;
  description?: string;
  moduleName?: string;
}

export interface DepartmentDetail {
  id: number;
  name?: string;
  isPrimary?: boolean;
  jobTitle?: string;
}

export interface ModuleAccessDetail {
  moduleId: number;
  moduleName?: string;
  moduleCode?: string;
  isActive: boolean;
  grantedAt?: string;
}

export interface UserDetail {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  avatarUrl?: string;
  userType: UserType;
  status: UserStatus;
  lastLoginAt?: string;
  createdAt?: string;
  timezone?: string;
  preferredLanguage?: string;
  organizationId: number;
  organizationName?: string;
  roles: RoleDetail[];
  departments: DepartmentDetail[];
  moduleAccesses: ModuleAccessDetail[];
}

export type UserDetailResponse = ApiResponse<UserDetail>;

// ==================== Invitations ====================

export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'CANCELLED';

export interface UserInvitation {
  id: number;
  organizationId: number;
  email: string;
  firstName?: string;
  lastName?: string;
  userType?: string;
  roleIds?: number[];
  departmentId?: number;
  moduleIds?: number[];
  message?: string;
  token?: string;
  status: InvitationStatus;
  invitedBy?: number;
  invitedAt?: string;
  expiresAt?: string;
  acceptedAt?: string;
}

export interface InviteUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  userType?: string;
  roleIds?: number[];
  departmentId?: number;
  moduleIds?: number[];
  message?: string;
}

export type InvitationResponse = ApiResponse<UserInvitation>;
export type InvitationsResponse = PaginatedResponse<UserInvitation>;

// ==================== User Actions ====================

export interface UpdateUserRolesRequest {
  roleIds: number[];
}

export interface UpdateUserTypeRequest {
  userType: string;
}

// ==================== Filters ====================

export interface SettingsUserFilters {
  organizationId: number;
  search?: string;
  status?: string;
  userType?: string;
  roleId?: number;
  departmentId?: number;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDir?: 'ASC' | 'DESC';
}

export interface InvitationFilters {
  organizationId: number;
  status?: string;
  page?: number;
  pageSize?: number;
}
