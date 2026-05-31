/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - General settings types
 */

export type DateFormat = 'MM/DD/YYYY' | 'DD/MM/YYYY' | 'YYYY-MM-DD';
export type TimeFormat = '12h' | '24h';
export type WeekDay = 'sunday' | 'monday';

export interface OrganizationSettingsSummary {
  totalUsers?: number | null;
  totalDepartments?: number | null;
  subscriptionPlan?: string | null;
}

export interface OrganizationSettings {
  id: number;
  name: string;
  code: string;
  email?: string | null;
  phoneNumber?: string | null;
  website?: string | null;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  zipCode?: string | null;
  taxId?: string | null;
  industry?: string | null;
  employeeCount?: number | null;
  description?: string | null;
  logoUrl?: string | null;
  faviconUrl?: string | null;
  primaryColor?: string | null;
  secondaryColor?: string | null;
  timezone?: string | null;
  dateFormat?: DateFormat | null;
  timeFormat?: TimeFormat | null;
  weekStartsOn?: WeekDay | null;
  currency?: string | null;
  language?: string | null;
  createdAt?: number | null;
  updatedAt?: number | null;
  summary?: OrganizationSettingsSummary | null;
}

export type UpdateOrganizationSettingsRequest = Partial<
  Omit<
    OrganizationSettings,
    'id' | 'code' | 'createdAt' | 'updatedAt' | 'summary'
  >
>;
