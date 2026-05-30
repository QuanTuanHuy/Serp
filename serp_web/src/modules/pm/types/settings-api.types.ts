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

export type PMWorkflowLifecycleStateApi = 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
export type PMWorkflowVersionStateApi = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface PMWorkflowSchemeRefApi {
  id: number;
  name: string;
  isSystem: boolean;
}

export interface PMWorkflowProjectRefApi {
  id: number;
  key: string;
  name: string;
}

export interface PMWorkflowSettingsApi {
  id: number;
  tenantId: number;
  workflowKey: string;
  name: string;
  description?: string | null;
  currentPublishedVersionId?: number | null;
  draftVersionId?: number | null;
  lifecycleState: PMWorkflowLifecycleStateApi;
  isSystem: boolean;
  readOnly: boolean;
  relatedSchemes: PMWorkflowSchemeRefApi[];
  spaces: PMWorkflowProjectRefApi[];
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMWorkflowOptionApi {
  id: number;
  workflowKey: string;
  name: string;
  description?: string | null;
  currentPublishedVersionId?: number | null;
  draftVersionId?: number | null;
  lifecycleState: PMWorkflowLifecycleStateApi;
  isSystem: boolean;
  readOnly: boolean;
}

export interface PMWorkflowSchemeWorkTypeOptionApi {
  id: number;
  typeKey: string;
  name: string;
  hierarchyLevel?: number | null;
  isSystem: boolean;
  readOnly: boolean;
}

export interface PMWorkflowSchemeItemApi {
  id?: number | null;
  issueTypeId: number;
  workflowId: number;
  workType?: PMWorkflowSchemeWorkTypeOptionApi | null;
  workflow?: PMWorkflowOptionApi | null;
}

export interface PMWorkflowSchemeSettingsApi {
  id: number;
  tenantId: number;
  name: string;
  description?: string | null;
  defaultWorkflowId: number;
  isSystem: boolean;
  readOnly: boolean;
  items: PMWorkflowSchemeItemApi[];
  spaces: PMWorkflowProjectRefApi[];
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMWorkflowSettingsOverviewApi {
  workflows: PMWorkflowSettingsApi[];
  workflowSchemes: PMWorkflowSchemeSettingsApi[];
}

export interface PMWorkflowStepApi {
  id: number;
  tenantId: number;
  workflowVersionId: number;
  stepKey: string;
  name: string;
  statusId: number;
  stepOrder?: number | null;
  isInitial: boolean;
  isTerminal: boolean;
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMWorkflowTransitionApi {
  id: number;
  tenantId: number;
  workflowVersionId: number;
  name: string;
  fromStepId?: number | null;
  toStepId: number;
  screenId?: number | null;
  sequence?: number | null;
  createdAt?: number | string;
  createdBy?: number;
  updatedAt?: number | string;
  updatedBy?: number;
}

export interface PMWorkflowEditorApi {
  workflow: PMWorkflowOptionApi;
  versionId?: number | null;
  versionState: PMWorkflowVersionStateApi;
  editable: boolean;
  steps: PMWorkflowStepApi[];
  transitions: PMWorkflowTransitionApi[];
}

export interface PMWorkflowValidationFindingApi {
  ruleKey: string;
  severity: string;
  message: string;
}

export interface PMWorkflowValidationApi {
  valid: boolean;
  errors: PMWorkflowValidationFindingApi[];
  warnings: PMWorkflowValidationFindingApi[];
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

export interface PMCreateWorkflowRequest {
  name: string;
  description?: string | null;
}

export interface PMUpdateWorkflowRequest {
  name: string;
  description?: string | null;
}

export interface PMCreateWorkflowSchemeRequest {
  name: string;
  description?: string | null;
  defaultWorkflowId: number;
}

export interface PMUpdateWorkflowSchemeRequest {
  name?: string;
  description?: string | null;
  defaultWorkflowId?: number;
}

export interface PMManageWorkflowSchemeItemsRequest {
  items: Array<{
    issueTypeId: number;
    workflowId: number;
  }>;
}

export interface PMDeleteWorkflowSchemeResponse {
  id: number;
  deleted: boolean;
  deletedAt?: number | string | null;
  updatedBy?: number | null;
}

export interface PMAddWorkflowStepRequest {
  statusId: number;
  isInitial?: boolean;
  isTerminal?: boolean;
}

export interface PMReorderWorkflowStepsRequest {
  stepIds: number[];
}

export interface PMAddWorkflowTransitionRequest {
  name: string;
  fromStepId?: number | null;
  toStepId: number;
  screenId?: number | null;
  sequence?: number | null;
}

export interface PMUpdateWorkflowTransitionRequest {
  name: string;
  screenId?: number | null;
  sequence?: number | null;
}

export interface PMDeleteWorkflowStepResponse {
  id: number;
  deleted: boolean;
  deletedAt?: number | string | null;
  updatedBy?: number | null;
}

export interface PMDeleteWorkflowTransitionResponse {
  id: number;
  deleted: boolean;
  deletedAt?: number | string | null;
  updatedBy?: number | null;
}
