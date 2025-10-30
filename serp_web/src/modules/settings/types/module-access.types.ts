/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module access types
 */

export interface OrganizationModule {
  moduleCode: string;
  moduleName: string;
  moduleDescription?: string;
  isActive: boolean;
  enabledAt: string;
  activeUsersCount: number;
  totalUsersCount: number;
}

export interface ModuleAccessSettings {
  moduleCode: string;
  autoGrantToNewUsers: boolean;
  requiredRoles: string[];
  customSettings?: Record<string, any>;
}

export interface UpdateModuleAccessSettingsRequest {
  autoGrantToNewUsers?: boolean;
  requiredRoles?: string[];
  customSettings?: Record<string, any>;
}

// Backend-aligned response for accessible modules
export interface AccessibleModule {
  organizationId: number;
  moduleId?: number;
  moduleName: string;
  moduleCode: string;
  moduleDescription?: string;
  isActive: boolean;
  grantedAt?: string;
  activeUserCount?: number;
  totalUsersCount?: number;
  isAutoGrantToNewUsers?: boolean;
  requiredRoles: string[];
}

// Minimal Role type for roles-in-module endpoint
export interface ModuleRole {
  id: number;
  name: string;
  description?: string;
  scope?: string;
  moduleId?: number;
  organizationId?: number;
  isDefault?: boolean;
}
