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
