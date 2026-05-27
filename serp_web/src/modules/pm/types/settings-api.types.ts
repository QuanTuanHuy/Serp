/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings API contract types
 */

export interface PMWorkTypeSchemeRefApi {
  id: number;
  name: string;
  isSystem: boolean;
}

export interface PMWorkTypeSettingsApi {
  id: number;
  tenantId: number;
  typeKey: string;
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  hierarchyLevel?: number | null;
  isSystem: boolean;
  readOnly: boolean;
  relatedSchemes: PMWorkTypeSchemeRefApi[];
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMWorkTypeOptionApi {
  id: number;
  typeKey: string;
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  hierarchyLevel?: number | null;
  sequence?: number | null;
  isDefault: boolean;
}

export interface PMWorkTypeProjectRefApi {
  id: number;
  key: string;
  name: string;
}

export interface PMWorkTypeSchemeSettingsApi {
  id: number;
  tenantId: number;
  name: string;
  description?: string | null;
  defaultIssueTypeId: number;
  isSystem: boolean;
  readOnly: boolean;
  workTypes: PMWorkTypeOptionApi[];
  spaces: PMWorkTypeProjectRefApi[];
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMIssueTypeSettingsOverviewApi {
  workTypes: PMWorkTypeSettingsApi[];
  workTypeSchemes: PMWorkTypeSchemeSettingsApi[];
}

export interface PMCreateIssueTypeRequest {
  typeKey: string;
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  hierarchyLevel: number;
}

export interface PMUpdateIssueTypeRequest {
  name?: string;
  description?: string | null;
  iconUrl?: string | null;
  hierarchyLevel?: number;
}

export interface PMDeleteIssueTypeResponse {
  issueTypeId: number;
  deleted: boolean;
  deletedAt?: number | string | null;
  deletedBy?: number | null;
}

export interface PMCreateIssueTypeSchemeRequest {
  name: string;
  description?: string | null;
  defaultIssueTypeId: number;
}

export interface PMUpdateIssueTypeSchemeRequest {
  name?: string;
  description?: string | null;
  defaultIssueTypeId?: number;
}

export interface PMManageIssueTypeSchemeItemsRequest {
  issueTypeIds: number[];
}

export interface PMDeleteIssueTypeSchemeResponse {
  issueTypeSchemeId: number;
  deleted: boolean;
  deletedAt?: number | string | null;
  deletedBy?: number | null;
}
