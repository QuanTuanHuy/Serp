/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module access types
 */

/**
 * User Module Access Response
 * Maps to UserModuleAccessResponse.java in backend
 */
export interface UserModuleAccess {
  userId: number;
  organizationId: number;
  moduleId: number;
  moduleName: string;
  moduleCode: string;
  moduleDescription: string;
  isActive: boolean;
  grantedAt: number;
}

/**
 * Module Display Item (for UI)
 */
export interface ModuleDisplayItem {
  code: string;
  name: string;
  description: string;
  href: string;
  isActive: boolean;
  isAdmin?: boolean;
}
