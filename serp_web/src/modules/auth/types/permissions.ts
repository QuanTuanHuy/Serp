/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Permissions and access control types
 */

// Core permissions structure
export interface UserPermissions {
  roles: string[];
  permissions: string[];
  menus: MenuAccess[];
  features: FeatureAccess[];
  modules: ModuleAccess[];
  organizationPermissions?: OrganizationPermission[];
}

// Menu access control
export interface MenuAccess {
  menuKey: string;
  menuName: string;
  path?: string;
  isVisible: boolean;
  icon?: string;
  order?: number;
  children?: MenuAccess[];
}

// Module access control
export interface ModuleAccess {
  moduleKey: string;
  moduleName: string;
  isEnabled: boolean;
}

// Feature toggles
export interface FeatureAccess {
  featureKey: string;
  featureName: string;
  isEnabled: boolean;
  description?: string;
  permissions?: string[];
}

// Multi-tenant organization permissions
export interface OrganizationPermission {
  organizationId: number;
  organizationName?: string;
  roles: string[];
  permissions: string[];
}

// Permission check configuration
export interface AccessConfig {
  roles?: string[];
  permissions?: string[];
  requireAllRoles?: boolean;
  requireAllPermissions?: boolean;
  menuKey?: string;
  moduleKey?: string;
  featureKey?: string;
  organizationId?: number;
  organizationRole?: string;
  organizationPermission?: string;
}
