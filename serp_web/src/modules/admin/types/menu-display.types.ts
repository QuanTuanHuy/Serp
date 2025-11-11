/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Display types
 */

import type {
  SearchParams,
  ApiResponse,
  PaginatedResponse,
} from '@/lib/store/api/types';
import type { MenuDisplay as BaseMenuDisplay } from './role.types';

export type MenuType = 'SIDEBAR' | 'TOPBAR' | 'DROPDOWN' | 'ACTION';

export interface RoleInfo {
  roleId: number;
  roleName: string;
}

export interface MenuDisplayDetail extends BaseMenuDisplay {
  order: number;
  menuType: MenuType;
  isVisible: boolean;
  moduleId: number;
  description?: string;
  moduleName?: string;
  moduleCode?: string;
  assignedRoles?: RoleInfo[];
}

export interface MenuDisplayTreeNode extends MenuDisplayDetail {
  children?: MenuDisplayTreeNode[];
  level?: number;
}

export interface CreateMenuDisplayRequest {
  name: string;
  path: string;
  icon?: string;
  order: number;
  parentId?: number;
  moduleId: number;
}

export interface UpdateMenuDisplayRequest {
  name: string;
  path: string;
  icon?: string;
  order: number;
}

export interface AssignMenuDisplayToRoleRequest {
  roleId: number;
  menuDisplayIds: number[];
}

export interface GetMenuDisplayParams extends SearchParams {
  moduleId?: number;
}

// Response types
export type MenuDisplaysResponse = PaginatedResponse<MenuDisplayDetail>;
export type MenuDisplayResponse = ApiResponse<MenuDisplayDetail>;
export type AssignRoleResponse = ApiResponse<void>;

// Filters for client-side
export interface MenuDisplayFilters {
  search?: string;
  moduleId?: number;
  menuType?: MenuType;
}

export interface MenuDisplayStats {
  total: number;
  byModule: Record<string, number>;
  byType: Record<MenuType, number>;
  visible: number;
  hidden: number;
}
