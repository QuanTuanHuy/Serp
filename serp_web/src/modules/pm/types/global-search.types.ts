/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search types
 */

export type PMGlobalSearchType =
  | 'CURRENT_PROJECT_WORK_ITEM'
  | 'WORK_ITEM'
  | 'PROJECT';

export interface PMGlobalSearchItem {
  id: string;
  title: string;
  subtitle?: string;
  url: string;
  meta?: Record<string, string | number | boolean | null>;
}

export interface PMGlobalSearchGroup {
  type: PMGlobalSearchType;
  title: string;
  total: number;
  items: PMGlobalSearchItem[];
}

export interface PMGlobalSearchResponse {
  query: string;
  limit: number;
  groups: PMGlobalSearchGroup[];
}

export interface PMGlobalSearchParams {
  q: string;
  limit?: number;
  currentProjectId?: number;
}
