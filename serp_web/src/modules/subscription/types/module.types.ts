/*
Author: QuanTuanHuy
Description: Part of Serp Project - Module Types
*/

export interface Module {
  id: number;
  moduleName: string;
  code: string;
  description?: string;
  category?: string;
  icon?: string;
  displayOrder?: number;
  isFree: boolean;
  pricingModel: 'FREE' | 'FIXED' | 'PER_USER' | 'TIERED';
  status: string;
  createdAt: string;
  updatedAt: string;
}
