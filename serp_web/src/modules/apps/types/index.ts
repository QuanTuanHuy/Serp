/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Apps module types
 */

export type PricingModel = 'FREE' | 'FIXED' | 'PER_USER' | 'TIERED';

export type ModuleStatus =
  | 'ACTIVE'
  | 'BETA'
  | 'DEPRECATED'
  | 'MAINTENANCE'
  | 'DISABLED';

export type ModuleType = 'SYSTEM' | 'CUSTOM';

export interface Module {
  id: number;
  moduleName: string;
  code: string;
  description?: string;
  category?: string;
  icon?: string;
  displayOrder?: number;
  moduleType?: ModuleType;
  isGlobal?: boolean;
  organizationId?: number;
  isFree: boolean;
  pricingModel: PricingModel;
  status: ModuleStatus;
  version?: string;
  dependsOnModuleIds?: number[];
  createdAt: string;
  updatedAt: string;
}

export interface GetModulesParams {
  category?: string;
  pricingModel?: PricingModel;
  status?: ModuleStatus;
  search?: string;
  isGlobal?: boolean;
  page?: number;
  pageSize?: number;
}

export interface ModuleCategory {
  code: string;
  name: string;
  description?: string;
  count?: number;
}

export interface ModuleFilterState {
  search: string;
  category?: string;
  pricingModel?: PricingModel;
  status?: ModuleStatus;
}

export type ViewMode = 'grid' | 'list';
