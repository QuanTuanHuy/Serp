/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project API contract types
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

export type PMProjectComponentAssigneeType =
  | 'PROJECT_DEFAULT'
  | 'COMPONENT_LEAD'
  | 'PROJECT_LEAD'
  | 'UNASSIGNED';

export interface PMProjectComponentApi {
  id: number;
  tenantId: number;
  projectId: number;
  name: string;
  description?: string | null;
  leadUserId?: number | null;
  assigneeType: PMProjectComponentAssigneeType | string;
  issueCount: number;
  createdAt?: number | string | null;
  createdBy?: number | null;
  updatedAt?: number | string | null;
  updatedBy?: number | null;
}

export interface PMProjectRoleApi {
  id: number;
  name: string;
  description?: string | null;
  isSystem: boolean;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
}

export interface PMProjectPersonRoleApi {
  id: number;
  name: string;
  system: boolean;
}

export interface PMProjectPersonApi {
  userId: number;
  name?: string | null;
  email?: string | null;
  avatarUrl?: string | null;
  projectLead: boolean;
  roles: PMProjectPersonRoleApi[];
  addedAt?: number | null;
}

export interface PMReplaceProjectPersonRolesRequest {
  roleIds: number[];
}

export type PMProjectPermissionGranteeType =
  | 'PROJECT_ROLE'
  | 'GROUP'
  | 'USER'
  | 'PROJECT_LEAD'
  | 'REPORTER'
  | 'ASSIGNEE'
  | 'ANY_LOGGED_IN_USER'
  | 'AUTHENTICATED'
  | 'APPLICATION_ACCESS'
  | 'ANYONE_ON_WEB'
  | 'USER_CUSTOM_FIELD_VALUE'
  | 'GROUP_CUSTOM_FIELD_VALUE'
  | string;

export interface PMProjectPermissionDefinitionApi {
  permissionKey: string;
  name: string;
  description?: string | null;
  category: string;
}

export interface PMProjectPermissionGrantApi {
  id?: number | null;
  permissionKey: string;
  granteeType: PMProjectPermissionGranteeType;
  granteeRef?: string | null;
  customFieldId?: number | null;
}

export interface PMProjectPermissionSettingsApi {
  scheme: {
    id: number;
    name: string;
    description?: string | null;
    tenantOwned: boolean;
  };
  permissions: PMProjectPermissionDefinitionApi[];
  grants: PMProjectPermissionGrantApi[];
}

export interface PMReplaceProjectPermissionGrantsRequest {
  grants: Array<{
    permissionKey: string;
    granteeType: PMProjectPermissionGranteeType;
    granteeRef?: string | null;
    customFieldId?: number | null;
  }>;
}

export interface PMListProjectComponentsParams {
  search?: string;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface PMCreateProjectComponentRequest {
  name: string;
  description?: string;
  leadUserId?: number | null;
  assigneeType?: PMProjectComponentAssigneeType;
}

export interface PMUpdateProjectComponentRequest {
  name?: string;
  description?: string | null;
  leadUserId?: number | null;
  assigneeType?: PMProjectComponentAssigneeType;
}

export interface PMDeleteProjectComponentResponse {
  componentId: number;
  deleted: boolean;
  deletedAt?: number | null;
  deletedBy?: number | null;
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

export type PMProjectSettingsSchemeType =
  | 'ISSUE_TYPE'
  | 'WORKFLOW'
  | 'FIELD_CONFIG'
  | 'SCREEN'
  | 'PERMISSION'
  | 'NOTIFICATION'
  | 'PRIORITY'
  | 'ISSUE_SECURITY';

export interface PMProjectSettingsOverviewApi {
  project: {
    id: number;
    key: string;
    name: string;
    description?: string | null;
    url?: string | null;
    projectTypeKey: string;
    isArchived: boolean;
    archivedAt?: number | string | null;
    leadUserId?: number | null;
    leadUserName?: string | null;
    category?: {
      id: number;
      name: string;
    } | null;
  };
  people: {
    leadUserId?: number | null;
    leadUserName?: string | null;
    memberCount: number;
    roleCount: number;
  };
  components: {
    totalCount: number;
    preview: Array<{
      id: number;
      name: string;
      description?: string | null;
      issueCount: number;
      assigneeType: string;
    }>;
  };
  schemes: Array<{
    type: PMProjectSettingsSchemeType;
    label: string;
    schemeId?: number | null;
    schemeName?: string | null;
    globalSection?: string | null;
    available: boolean;
  }>;
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

export interface PMProjectDetailApi {
  id: number;
  key: string;
  name: string;
  description?: string;
  url?: string;
  leadUserId: number;
  leadUserName?: string;
  avatarId?: number;
  projectTypeKey: string;
  isArchived: boolean;
  archivedAt?: string;
  category?: {
    id: number;
    name: string;
    description?: string;
  } | null;
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

export interface PMUpdateProjectRequest {
  name?: string;
  key?: string;
  description?: string;
  leadUserId?: number;
  categoryId?: number;
  url?: string;
  avatarId?: number;
}

export interface PMUpdateProjectResponse {
  id: number;
  key: string;
  name: string;
  isArchived: boolean;
}

export interface PMProjectSummaryFilterParams {
  assigneeIds?: number[];
  priorityIds?: number[];
  statusIds?: number[];
  issueTypeIds?: number[];
  parentId?: number;
  createdFrom?: number;
  createdTo?: number;
  updatedFrom?: number;
  updatedTo?: number;
  dueDateFrom?: number;
  dueDateTo?: number;
  activityPage?: number;
  activitySize?: number;
}

export interface PMProjectSummaryMetricsApi {
  completedLast7Days: number;
  updatedLast7Days: number;
  createdLast7Days: number;
  dueSoonNext7Days: number;
}

export interface PMProjectSummaryBreakdownItemApi {
  id: number;
  key?: string | null;
  name: string;
  iconUrl?: string | null;
  color?: string | null;
  sequence?: number | null;
  categoryKey?: string | null;
  categoryName?: string | null;
  count: number;
}

export interface PMProjectSummaryStatusOverviewApi {
  total: number;
  items: PMProjectSummaryBreakdownItemApi[];
}

export interface PMProjectSummaryUserApi {
  id: number;
  displayName?: string | null;
  avatarUrl?: string | null;
}

export interface PMProjectSummaryParentOptionApi {
  id: number;
  key: string;
  summary: string;
}

export interface PMProjectSummaryActivityApi {
  id: string;
  type: 'COMMENT' | 'HISTORY' | string;
  actor?: PMProjectSummaryUserApi | null;
  workItemId: number;
  workItemKey: string;
  workItemSummary: string;
  statusId?: number | null;
  statusKey?: string | null;
  statusName?: string | null;
  body?: string | null;
  fieldKey?: string | null;
  fieldName?: string | null;
  fromValue?: string | null;
  toValue?: string | null;
  createdAt?: number | string | null;
}

export interface PMProjectSummaryActivityPageApi {
  items: PMProjectSummaryActivityApi[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface PMProjectSummaryFilterOptionsApi {
  assignees: PMProjectSummaryUserApi[];
  parents: PMProjectSummaryParentOptionApi[];
  priorities: PMProjectSummaryBreakdownItemApi[];
  statuses: PMProjectSummaryBreakdownItemApi[];
  issueTypes: PMProjectSummaryBreakdownItemApi[];
}

export interface PMProjectSummaryDashboardApi {
  projectId: number;
  metrics: PMProjectSummaryMetricsApi;
  statusOverview: PMProjectSummaryStatusOverviewApi;
  priorityBreakdown: PMProjectSummaryBreakdownItemApi[];
  workTypeBreakdown: PMProjectSummaryBreakdownItemApi[];
  recentActivity: PMProjectSummaryActivityPageApi;
  filterOptions: PMProjectSummaryFilterOptionsApi;
}
