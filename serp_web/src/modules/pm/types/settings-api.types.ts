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

export interface PMPrioritySchemeRefApi {
  id: number;
  name: string;
  isSystem: boolean;
}

export interface PMPrioritySettingsApi {
  id: number;
  tenantId: number;
  priorityKey: string;
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  color?: string | null;
  sequence?: number | null;
  isSystem: boolean;
  readOnly: boolean;
  relatedSchemes: PMPrioritySchemeRefApi[];
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMPriorityOptionApi {
  id: number;
  priorityKey: string;
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  color?: string | null;
  sequence?: number | null;
  isDefault: boolean;
}

export interface PMPriorityProjectRefApi {
  id: number;
  key: string;
  name: string;
}

export interface PMPrioritySchemeSettingsApi {
  id: number;
  tenantId: number;
  name: string;
  description?: string | null;
  defaultPriorityId: number;
  isSystem: boolean;
  readOnly: boolean;
  priorities: PMPriorityOptionApi[];
  spaces: PMPriorityProjectRefApi[];
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMPrioritySettingsOverviewApi {
  priorities: PMPrioritySettingsApi[];
  prioritySchemes: PMPrioritySchemeSettingsApi[];
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

export interface PMCreatePriorityRequest {
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  color?: string | null;
  sequence: number;
}

export interface PMUpdatePriorityRequest {
  name?: string;
  description?: string | null;
  iconUrl?: string | null;
  color?: string | null;
  sequence?: number;
}

export interface PMDeletePriorityResponse {
  priorityId: number;
  deleted: boolean;
  deletedAt?: number | string | null;
  deletedBy?: number | null;
}

export interface PMCreatePrioritySchemeRequest {
  name: string;
  description?: string | null;
  defaultPriorityId: number;
}

export interface PMUpdatePrioritySchemeRequest {
  name?: string;
  description?: string | null;
  defaultPriorityId?: number;
}

export interface PMManagePrioritySchemeItemsRequest {
  priorityIds: number[];
}

export interface PMDeletePrioritySchemeResponse {
  prioritySchemeId: number;
  deleted: boolean;
  deletedAt?: number | string | null;
  deletedBy?: number | null;
}
