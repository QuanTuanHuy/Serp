/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item API contract types
 */

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

export interface PMStatusApi {
  id: number;
  tenantId: number;
  statusKey: string;
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  statusCategoryId?: number | null;
  isSystem: boolean;
  readOnly: boolean;
  createdAt?: number;
  createdBy?: number;
  updatedAt?: number;
  updatedBy?: number;
}

export interface PMPriorityApi {
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
  createdAt?: number;
  createdBy?: number;
  updatedAt?: number;
  updatedBy?: number;
}

export interface PMIssueTypeApi {
  id: number;
  tenantId: number;
  typeKey: string;
  name: string;
  description?: string | null;
  iconUrl?: string | null;
  hierarchyLevel?: number | null;
  isSystem: boolean;
  readOnly: boolean;
  createdAt?: number;
  createdBy?: number;
  updatedAt?: number;
  updatedBy?: number;
}

export interface PMProjectScopedListParams {
  projectId?: number;
  isSystem?: boolean;
  search?: string;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
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
  iconUrl?: string | null;
  hierarchyLevel?: number | null;
}

export interface PMWorkItemDetailUserApi {
  id: number;
  displayName?: string | null;
  avatarUrl?: string | null;
}

export interface PMWorkItemDetailWorkflowStepApi {
  id: number;
  name: string;
}

export interface PMWorkItemDetailStatusApi {
  id: number;
  name: string;
  key?: string | null;
}

export interface PMWorkItemDetailPriorityApi {
  id: number;
  name: string;
  color?: string | null;
}

export interface PMWorkItemDetailParentApi {
  id: number;
  key: string;
  summary: string;
}

export interface PMWorkItemComponentApi {
  id: number;
  name: string;
  description?: string | null;
}

export interface PMWorkItemCountStatsApi {
  total: number;
}

export interface PMWorkItemSubtaskStatsApi extends PMWorkItemCountStatsApi {
  done: number;
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
  parent?: PMWorkItemDetailParentApi | null;
  components?: PMWorkItemComponentApi[] | null;
  subtaskStats?: PMWorkItemSubtaskStatsApi | null;
  linkStats?: PMWorkItemCountStatsApi | null;
  commentStats?: PMWorkItemCountStatsApi | null;
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMWorkItemChildApi {
  id: number;
  projectId: number;
  parentId?: number | null;
  key: string;
  summary: string;
  issueType?: PMWorkItemDetailIssueTypeApi | null;
  status?: PMWorkItemDetailStatusApi | null;
  priority?: PMWorkItemDetailPriorityApi | null;
  assignee?: PMWorkItemDetailUserApi | null;
}

export interface PMWorkItemLinkTypeApi {
  id: number;
  name: string;
  description?: string | null;
}

export interface PMWorkItemLinkTargetApi {
  id: number;
  projectId?: number | null;
  key: string;
  summary: string;
  status?: PMWorkItemDetailStatusApi | null;
  priority?: PMWorkItemDetailPriorityApi | null;
}

export interface PMWorkItemLinkApi {
  id: number;
  direction: 'OUTWARD' | 'INWARD' | string;
  linkType?: PMWorkItemLinkTypeApi | null;
  workItem?: PMWorkItemLinkTargetApi | null;
}

export interface PMWorkItemCommentApi {
  id: number;
  body: string;
  author?: PMWorkItemDetailUserApi | null;
  createdAt?: number | string;
  updatedAt?: number | string;
  edited: boolean;
}

export interface PMWorkItemActivityApi {
  id: string;
  type: 'COMMENT' | 'HISTORY' | string;
  actor?: PMWorkItemDetailUserApi | null;
  body?: string | null;
  fieldKey?: string | null;
  fieldName?: string | null;
  fromValue?: string | null;
  toValue?: string | null;
  createdAt?: number | string;
}

export interface PMUpdateWorkItemRequest {
  summary?: string | null;
  description?: string | null;
  priorityId?: number | null;
  assigneeId?: number | null;
  startDate?: number | null;
  dueDate?: number | null;
  timeOriginalEstimate?: number | null;
  timeRemainingEstimate?: number | null;
  securityLevelId?: number | null;
  customFields?: Record<string, unknown>;
}

export interface PMUpdateWorkItemResponse {
  id: number;
  projectId: number;
  key: string;
  summary: string;
  description?: string | null;
  statusId?: number | null;
  priorityId?: number | null;
  assigneeId?: number | null;
  startDate?: number | null;
  dueDate?: number | null;
  timeOriginalEstimate?: number | null;
  timeRemainingEstimate?: number | null;
  changedFields: string[];
  updatedAt?: number | string;
  updatedBy?: number | null;
}

export interface PMWorkItemTransitionStatusApi {
  id: number;
  key?: string | null;
  name: string;
  iconUrl?: string | null;
}

export interface PMWorkItemTransitionStatusCategoryApi {
  id: number;
  key?: string | null;
  name: string;
}

export interface PMWorkItemTransitionApi {
  id: number;
  name: string;
  fromStepId: number;
  toStepId: number;
  screenId?: number | null;
  sequence?: number | null;
  targetStatus: PMWorkItemTransitionStatusApi;
  targetStatusCategory?: PMWorkItemTransitionStatusCategoryApi | null;
}

export interface PMTransitionWorkItemStatusRequest {
  transitionId: number;
  resolutionId?: number | null;
  fields?: Record<string, unknown>;
}

export interface PMTransitionWorkItemStatusResponse {
  id: number;
  projectId: number;
  key: string;
  summary: string;
  description?: string | null;
  workflowStepId?: number | null;
  statusId?: number | null;
  assigneeId?: number | null;
  resolutionId?: number | null;
  securityLevelId?: number | null;
  dueDate?: number | null;
  timeOriginalEstimate?: number | null;
  transitionId: number;
  transitionName: string;
  fromStepId?: number | null;
  toStepId?: number | null;
  changedFields: string[];
  transitionedAt?: number | string;
  transitionedBy?: number | null;
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
  assigneeName?: string | null;
  assigneeAvatarUrl?: string | null;
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

export interface PMWorkItemTimelineIssueTypeApi {
  id?: number | null;
  name?: string | null;
  iconUrl?: string | null;
  hierarchyLevel?: number | null;
}

export interface PMWorkItemTimelineStatusApi {
  id?: number | null;
  name?: string | null;
}

export interface PMWorkItemTimelinePriorityApi {
  id?: number | null;
  name?: string | null;
  color?: string | null;
}

export interface PMWorkItemTimelineItemApi {
  id: number;
  projectId: number;
  parentId?: number | null;
  key: string;
  summary: string;
  assigneeId?: number | null;
  startDate?: number | null;
  dueDate?: number | null;
  isUnscheduled: boolean;
  hasChildren: boolean;
  rank: string;
  issueType?: PMWorkItemTimelineIssueTypeApi | null;
  status?: PMWorkItemTimelineStatusApi | null;
  priority?: PMWorkItemTimelinePriorityApi | null;
}

export interface PMWorkItemTimelineDependencyApi {
  sourceId: number;
  targetId: number;
  linkTypeId?: number | null;
  linkTypeKey?: string | null;
  linkTypeName?: string | null;
}

export interface PMWorkItemTimelineResponse {
  items: PMWorkItemTimelineItemApi[];
  dependencies: PMWorkItemTimelineDependencyApi[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface PMGetWorkItemTimelineParams {
  viewportStart?: number;
  viewportEnd?: number;
  includeUnscheduled?: boolean;
  includeDependencies?: boolean;
  parentId?: number;
  depth?: number;
  statusIds?: number[];
  assigneeIds?: number[];
  issueTypeIds?: number[];
  priorityIds?: number[];
  keyword?: string;
  page?: number;
  pageSize?: number;
}
