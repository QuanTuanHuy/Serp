/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module types
 */

import type { SearchParams, ApiResponse } from '@/lib/store/api/types';

export type ModuleStatus =
  | 'ACTIVE'
  | 'BETA'
  | 'DEPRECATED'
  | 'MAINTENANCE'
  | 'DISABLED';

export type ModuleType = 'SYSTEM' | 'CUSTOM';

export type PricingModel = 'FREE' | 'FIXED' | 'PER_USER' | 'TIERED';

// Module
export interface Module {
  id: number;
  moduleName: string;
  code: string;
  description?: string;
  keycloakClientId?: string;
  category?: string;
  icon?: string;
  displayOrder?: number;
  moduleType: ModuleType;
  isGlobal: boolean;
  organizationId?: number;
  isFree: boolean;
  pricingModel: PricingModel;
  dependsOnModuleIds?: number[];
  status: ModuleStatus;
  version?: string;
  createdAt: string;
  updatedAt: string;
}

// Filters
export interface ModuleFilters extends SearchParams {
  status?: ModuleStatus;
  category?: string;
}

// Response types
export type ModulesResponse = ApiResponse<Module[]>;
export type ModuleResponse = ApiResponse<Module>;
