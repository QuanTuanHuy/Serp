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

export interface PMWorkItemCreateMetaProject {
  id: number;
  key: string;
  name: string;
  projectTypeKey: string;
  archived: boolean;
}

export interface PMWorkItemIssueTypeOption {
  id: number;
  typeKey: string;
  name: string;
  description?: string;
  iconUrl?: string | null;
  hierarchyLevel?: number | null;
}

export interface PMWorkItemStatusOption {
  id: number;
  statusKey: string;
  name: string;
  description?: string;
  iconUrl?: string | null;
  statusCategoryId?: number | null;
}

export interface PMWorkItemPriorityOption {
  id: number;
  priorityKey: string;
  name: string;
  description?: string;
  iconUrl?: string | null;
  color?: string | null;
  sequence?: number | null;
}

export interface PMWorkItemSecurityLevelOption {
  id: number;
  name: string;
  description?: string;
}

export interface PMWorkItemComponentOption {
  id: number;
  name: string;
  description?: string;
  leadUserId?: number | null;
  assigneeType?: string | null;
}

export interface PMWorkItemFieldPolicy {
  required: boolean;
  hidden: boolean;
  onScreen: boolean;
  clientWritable: boolean;
}

export interface PMWorkItemCustomFieldOption {
  id: number;
  optionKey: string;
  value: string;
  sequence?: number | null;
  parentOptionId?: number | null;
  disabled: boolean;
}

export interface PMWorkItemCustomFieldDefaultValue {
  valueType: string;
  value: unknown;
  sortOrder?: number | null;
}

export interface PMWorkItemCustomField {
  id: number;
  fieldKey: string;
  name: string;
  description?: string;
  typeKey: string;
  schemaJson?: string | null;
  contextId: number;
  contextName?: string;
  contextIssueTypeKey?: string | null;
  required: boolean;
  hidden: boolean;
  onScreen: boolean;
  clientWritable: boolean;
  options: PMWorkItemCustomFieldOption[];
  defaultValues: PMWorkItemCustomFieldDefaultValue[];
}

export interface PMWorkItemCreateMetaResponse {
  project: PMWorkItemCreateMetaProject;
  createAllowed: boolean;
  createBlockedReason?: string | null;
  issueTypes: PMWorkItemIssueTypeOption[];
  selectedIssueTypeId: number;
  initialStatus: PMWorkItemStatusOption;
  defaultPriorityId?: number | null;
  priorities: PMWorkItemPriorityOption[];
  defaultSecurityLevelId?: number | null;
  securityLevels: PMWorkItemSecurityLevelOption[];
  components: PMWorkItemComponentOption[];
  systemFields: Record<string, PMWorkItemFieldPolicy>;
  customFields: PMWorkItemCustomField[];
}

export interface PMCreateWorkItemRequest {
  issueTypeId: number;
  summary: string;
  description?: string;
  priorityId?: number;
  assigneeId?: number;
  parentId?: number;
  dueDate?: number;
  timeOriginalEstimate?: number;
  securityLevelId?: number;
  customFields?: Record<string, unknown>;
}

export interface PMCreateWorkItemResponse {
  id: number;
  projectId: number;
  issueTypeId: number;
  issueNo: number;
  key: string;
  summary: string;
  description?: string;
  workflowStepId: number;
  statusId: number;
  priorityId?: number;
  assigneeId?: number;
  reporterId: number;
  parentId?: number;
  securityLevelId?: number;
  startDate?: number;
  dueDate?: number;
  rank: string;
  timeOriginalEstimate?: number;
  timeRemainingEstimate?: number;
  timeSpent?: number;
  createdAt: number;
  createdBy: number;
  updatedAt: number;
  updatedBy: number;
}

export interface PMWorkItemSearchApi {
  id: number;
  projectId: number;
  issueTypeId: number;
  issueNo: number;
  key: string;
  summary: string;
  description?: string;
  workflowStepId: number;
  statusId: number;
  priorityId?: number;
  resolutionId?: number;
  assigneeId?: number;
  reporterId?: number;
  parentId?: number;
  securityLevelId?: number;
  startDate?: number;
  dueDate?: number;
  rank: string;
  timeOriginalEstimate?: number;
  timeRemainingEstimate?: number;
  timeSpent?: number;
  createdAt: number;
  createdBy: number;
  updatedAt: number;
  updatedBy: number;
  issueTypeName?: string;
  issueTypeIconUrl?: string | null;
  issueTypeHierarchyLevel?: number | null;
  priorityName?: string;
  priorityIconUrl?: string | null;
  priorityColor?: string | null;
  prioritySequence?: number | null;
  statusKey?: string;
  statusName?: string;
  statusIconUrl?: string | null;
  statusCategoryKey?: string;
  statusCategoryName?: string;
  assigneeName?: string;
  assigneeAvatarUrl?: string | null;
  reporterName?: string;
  reporterAvatarUrl?: string | null;
}

export interface PMSearchWorkItemsParams {
  keyword?: string;
  statusIds?: number[];
  priorityIds?: number[];
  issueTypeIds?: number[];
  assigneeIds?: number[];
  reporterIds?: number[];
  resolutionIds?: number[];
  parentId?: number;
  excludeStatusIds?: number[];
  excludeIssueTypeIds?: number[];
  unassigned?: boolean;
  unresolved?: boolean;
  dueDateFrom?: number;
  dueDateTo?: number;
  createdFrom?: number;
  createdTo?: number;
  updatedFrom?: number;
  updatedTo?: number;
  sprintIds?: number[];
  componentIds?: number[];
  fixVersionIds?: number[];
  isOverdue?: boolean;
  hasTimeLogged?: boolean;
  enriched?: boolean;
  page?: number;
  pageSize?: number;
  sortField?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface PMWorkItemDetailIssueTypeApi {
  id: number;
  name: string;
}

export interface PMWorkItemDetailUserApi {
  id: number;
  displayName?: string | null;
}

export interface PMWorkItemDetailWorkflowStepApi {
  id: number;
  name: string;
}

export interface PMWorkItemDetailStatusApi {
  id: number;
  name: string;
}

export interface PMWorkItemDetailPriorityApi {
  id: number;
  name: string;
  color?: string | null;
}

export interface PMWorkItemDetailApi {
  id: number;
  projectId: number;
  issueTypeId?: number;
  issueNo: number;
  key: string;
  summary: string;
  description?: string | null;
  workflowStepId?: number;
  statusId?: number;
  priorityId?: number;
  resolutionId?: number;
  assigneeId?: number;
  reporterId?: number;
  parentId?: number;
  securityLevelId?: number;
  startDate?: number;
  dueDate?: number;
  rank?: string;
  timeOriginalEstimate?: number;
  timeRemainingEstimate?: number;
  timeSpent?: number;
  issueType?: PMWorkItemDetailIssueTypeApi | null;
  assignee?: PMWorkItemDetailUserApi | null;
  reporter?: PMWorkItemDetailUserApi | null;
  workflowStep?: PMWorkItemDetailWorkflowStepApi | null;
  status?: PMWorkItemDetailStatusApi | null;
  priority?: PMWorkItemDetailPriorityApi | null;
  createdAt?: number;
  createdBy?: number;
  updatedAt?: number;
  updatedBy?: number;
}

export interface PMWorkItemBoardIssueTypeApi {
  id?: number | null;
  name?: string | null;
  iconUrl?: string | null;
  hierarchyLevel?: number | null;
}

export interface PMWorkItemBoardPriorityApi {
  id?: number | null;
  name?: string | null;
  iconUrl?: string | null;
  color?: string | null;
}

export interface PMWorkItemBoardCardApi {
  id: number;
  projectId: number;
  parentId?: number | null;
  key: string;
  summary: string;
  description?: string | null;
  assigneeId?: number | null;
  reporterId?: number | null;
  startDate?: string | number | null;
  dueDate?: string | number | null;
  rank: string;
  issueType?: PMWorkItemBoardIssueTypeApi | null;
  priority?: PMWorkItemBoardPriorityApi | null;
}

export interface PMWorkItemBoardStatusCategoryApi {
  id?: number | null;
  key?: string | null;
  name?: string | null;
}

export interface PMWorkItemBoardColumnApi {
  statusId: number;
  statusKey: string;
  statusName: string;
  statusDescription?: string | null;
  statusIconUrl?: string | null;
  statusCategory?: PMWorkItemBoardStatusCategoryApi | null;
  items: PMWorkItemBoardCardApi[];
  total: number;
}

export interface PMWorkItemBoardResponse {
  projectId: number;
  columns: PMWorkItemBoardColumnApi[];
}

export interface PMGetWorkItemBoardParams {
  keyword?: string;
  statusIds?: number[];
  assigneeIds?: number[];
  issueTypeIds?: number[];
  priorityIds?: number[];
}
