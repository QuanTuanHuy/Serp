/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User management types for settings
 */

export interface OrganizationUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  avatarUrl?: string;
  status: UserStatus;
  roles: string[];
  departmentId?: string;
  departmentName?: string;
  position?: string;
  phone?: string;
  joinedAt: string;
  lastActiveAt?: string;
  moduleAccess: UserModuleAccess[];
}

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'PENDING';

export interface UserModuleAccess {
  moduleCode: string;
  moduleName: string;
  isActive: boolean;
  grantedAt: string;
  grantedBy: string;
}

export interface InviteUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  roleIds: string[];
  departmentId?: string;
  position?: string;
  message?: string;
}

export interface UpdateUserRequest {
  firstName?: string;
  lastName?: string;
  roleIds?: string[];
  departmentId?: string;
  position?: string;
  phone?: string;
  status?: UserStatus;
}

export interface GrantModuleAccessRequest {
  userId: string;
  moduleCode: string;
}

export interface RevokeModuleAccessRequest {
  userId: string;
  moduleCode: string;
}

export interface UserFilters {
  search?: string;
  status?: UserStatus;
  roleId?: string;
  departmentId?: string;
  hasModuleAccess?: string;
}

export interface UserInvitation {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  status: InvitationStatus;
  invitedBy: string;
  invitedByName: string;
  invitedAt: string;
  expiresAt: string;
  acceptedAt?: string;
}

export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'CANCELLED';
