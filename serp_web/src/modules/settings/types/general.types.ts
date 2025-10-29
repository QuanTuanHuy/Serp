/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - General settings types
 */

export interface OrganizationProfile {
  id: string;
  name: string;
  slug: string;
  email: string;
  phone?: string;
  website?: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  zipCode?: string;
  taxId?: string;
  industry?: string;
  companySize?: CompanySize;
  description?: string;
  logoUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export type CompanySize =
  | '1-10'
  | '11-50'
  | '51-200'
  | '201-500'
  | '501-1000'
  | '1001+';

export interface OrganizationBranding {
  primaryColor?: string;
  secondaryColor?: string;
  logoUrl?: string;
  faviconUrl?: string;
  customCss?: string;
}

export interface OrganizationPreferences {
  timezone: string;
  dateFormat: DateFormat;
  timeFormat: TimeFormat;
  currency: string;
  language: string;
  weekStartsOn: WeekDay;
}

export type DateFormat = 'MM/DD/YYYY' | 'DD/MM/YYYY' | 'YYYY-MM-DD';
export type TimeFormat = '12h' | '24h';
export type WeekDay = 'sunday' | 'monday';

export interface UpdateOrganizationProfileRequest {
  name?: string;
  email?: string;
  phone?: string;
  website?: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  zipCode?: string;
  taxId?: string;
  industry?: string;
  companySize?: CompanySize;
  description?: string;
}

export interface UpdateOrganizationBrandingRequest {
  primaryColor?: string;
  secondaryColor?: string;
  logoUrl?: string;
  faviconUrl?: string;
  customCss?: string;
}

export interface UpdateOrganizationPreferencesRequest {
  timezone?: string;
  dateFormat?: DateFormat;
  timeFormat?: TimeFormat;
  currency?: string;
  language?: string;
  weekStartsOn?: WeekDay;
}
