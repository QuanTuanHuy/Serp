/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM API contract types
 */

export interface PMProjectBlueprintApi {
  id: number;
  tenantId: number;
  name: string;
  description: string;
  projectTypeKey: string;
  avatarUrl?: string | null;
  isSystem: boolean;
  readOnly: boolean;
  createdAt?: string;
  createdBy?: number;
  updatedAt?: string;
  updatedBy?: number;
}

export interface PMProjectCategoryApi {
  id: number;
  tenantId: number;
  name: string;
  description: string;
  isSystem: boolean;
  createdAt?: string;
  createdBy?: number;
  updatedAt?: string;
  updatedBy?: number;
}

export interface PMCreateProjectRequest {
  name: string;
  key: string;
  description?: string;
  projectTypeKey: 'software';
  leadUserId: number;
  categoryId?: number;
  blueprintId: number;
  provisioningMode: 'TEMPLATE_DEFAULT';
}

export interface PMCreateProjectResponse {
  id: number;
  key: string;
  name: string;
  description?: string;
  url?: string;
  leadUserId: number;
  avatarId?: number;
  categoryId?: number;
  projectTypeKey: string;
  isArchived: boolean;
  archivedAt?: string;
  issueTypeSchemeId?: number;
  workflowSchemeId?: number;
  fieldConfigSchemeId?: number;
  issueTypeScreenSchemeId?: number;
  permissionSchemeId?: number;
  notificationSchemeId?: number;
  prioritySchemeId?: number;
  issueSecuritySchemeId?: number;
  createdAt?: string;
  createdBy?: number;
  updatedAt?: string;
  updatedBy?: number;
}

export interface PMProjectSummaryApi {
  id: number;
  key: string;
  name: string;
  description?: string;
  projectTypeKey: string;
  leadUserId?: number;
  leadUserName?: string;
  avatarId?: number;
  categoryId?: number;
  categoryName?: string;
  isArchived: boolean;
  archivedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PMListProjectsParams {
  search?: string;
  categoryId?: number;
  projectTypeKey?: string;
  archived?: boolean;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}
