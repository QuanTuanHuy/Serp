/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin global search types
 */

export type AdminGlobalSearchType = 'ORGANIZATION' | 'USER' | 'ROLE' | 'MODULE';

export interface AdminGlobalSearchItem {
  id: string;
  title: string;
  subtitle?: string;
  url: string;
}

export interface AdminGlobalSearchGroup {
  type: AdminGlobalSearchType;
  title: string;
  total: number;
  items: AdminGlobalSearchItem[];
}

export interface AdminGlobalSearchResponse {
  query: string;
  limit: number;
  groups: AdminGlobalSearchGroup[];
}

export interface AdminGlobalSearchParams {
  q: string;
  limit?: number;
}
