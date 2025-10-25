/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Role types
 */

import type {
  SearchParams,
  ApiResponse,
  PaginatedResponse as BasePaginatedResponse,
} from '@/lib/store/api/types';

export type RoleScope = 'SYSTEM' | 'ORGANIZATION' | 'MODULE' | 'DEPARTMENT';

export type RoleType =
  | 'OWNER'
  | 'ADMIN'
  | 'MANAGER'
  | 'USER'
  | 'VIEWER'
  | 'CUSTOM';

// Permission entity
export interface Permission {
  id: number;
  name: string;
  description?: string;
  resource: string;
  action: string;
  moduleId?: number;
  createdAt: string;
  updatedAt: string;
}

// Menu Display entity
export interface MenuDisplay {
  id: number;
  name: string;
  displayName?: string;
  path?: string;
  icon?: string;
  parentId?: number;
  moduleId?: number;
  createdAt: string;
  updatedAt: string;
}

// Role entity
export interface Role {
  id: number;
  name: string;
  description?: string;
  isRealmRole: boolean;
  keycloakClientId?: string;
  priority: number;
  scope: RoleScope;
  scopeId?: number;
  moduleId?: number;
  organizationId?: number;
  departmentId?: number;
  parentRoleId?: number;
  roleType: RoleType;
  isDefault: boolean;
  permissions?: Permission[];
  menuDisplays?: MenuDisplay[];
  createdAt: string;
  updatedAt: string;
}

// Request DTOs
export interface CreateRoleRequest {
  name: string;
  description?: string;
  isRealmRole?: boolean;
  keycloakClientId?: string;
  priority?: number;
  scope: string;
  scopeId?: number;
  moduleId?: number;
  organizationId?: number;
  departmentId?: number;
  parentRoleId?: number;
  roleType: string;
  isDefault?: boolean;
  permissionIds?: number[];
}

export interface UpdateRoleRequest {
  name?: string;
  description?: string;
  isRealmRole?: boolean;
  keycloakClientId?: string;
  priority?: number;
  scope?: string;
  scopeId?: number;
  moduleId?: number;
  organizationId?: number;
  departmentId?: number;
  parentRoleId?: number;
  roleType?: string;
  isDefault?: boolean;
  permissionIds?: number[];
}

export interface AddPermissionToRoleRequest {
  permissionIds: number[];
}

// Filters
export interface RoleFilters extends SearchParams {
  search?: string;
  scope?: RoleScope;
  roleType?: RoleType;
  organizationId?: number;
  moduleId?: number;
  isDefault?: boolean;
}

// Response types
export type RolesResponse = BasePaginatedResponse<Role>;
export type RoleResponse = ApiResponse<Role>;
