/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings global search types
 */

export type SettingsGlobalSearchType = 'USER' | 'DEPARTMENT' | 'MODULE';

export interface SettingsGlobalSearchItem {
  id: string;
  title: string;
  subtitle?: string;
  url: string;
}

export interface SettingsGlobalSearchGroup {
  type: SettingsGlobalSearchType;
  title: string;
  total: number;
  items: SettingsGlobalSearchItem[];
}

export interface SettingsGlobalSearchResponse {
  query: string;
  limit: number;
  groups: SettingsGlobalSearchGroup[];
}

export interface SettingsGlobalSearchParams {
  organizationId: number;
  q: string;
  limit?: number;
}
