/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM create work item form helpers
 */

import { z } from 'zod';
import type { ComboboxItem } from '@/shared/components/ui/combobox';
import type {
  PMCreateWorkItemRequest,
  PMProjectSummaryApi,
  PMWorkItemCustomField,
  PMWorkItemSearchApi,
} from '../../types/api';

export const createWorkItemSchema = z.object({
  projectId: z.string().min(1, 'Project is required'),
  issueTypeId: z.string().min(1, 'Issue type is required'),
  summary: z
    .string()
    .trim()
    .min(1, 'Summary is required')
    .max(512, 'Summary must be 512 characters or fewer'),
  description: z.string().optional().or(z.literal('')),
  priorityId: z.string().optional().or(z.literal('')),
  assigneeId: z.string().optional().or(z.literal('')),
  parentId: z.string().optional().or(z.literal('')),
  dueDate: z.string().optional().or(z.literal('')),
  timeOriginalEstimate: z.string().optional().or(z.literal('')),
  securityLevelId: z.string().optional().or(z.literal('')),
});

export type CreateWorkItemFormValues = z.infer<typeof createWorkItemSchema>;

export function getCreateWorkItemDefaultValues(
  initialProjectId?: number,
  initialParentId?: number
): CreateWorkItemFormValues {
  return {
    projectId: initialProjectId ? String(initialProjectId) : '',
    issueTypeId: '',
    summary: '',
    description: '',
    priorityId: '',
    assigneeId: '',
    parentId: initialParentId ? String(initialParentId) : '',
    dueDate: '',
    timeOriginalEstimate: '',
    securityLevelId: '',
  };
}

export function mapProjectToComboboxItem(
  project: PMProjectSummaryApi
): ComboboxItem & { archived: boolean } {
  return {
    value: String(project.id),
    label: `${project.name} (${project.key})`,
    archived: project.isArchived,
  };
}

export function mapWorkItemToComboboxItem(
  item: PMWorkItemSearchApi
): ComboboxItem {
  return {
    value: String(item.id),
    label: `${item.key} - ${item.summary}`,
  };
}

export function buildCreateWorkItemRequest(
  values: CreateWorkItemFormValues,
  customFields: PMWorkItemCustomField[]
): PMCreateWorkItemRequest {
  const customFieldPayload: Record<string, unknown> = {};

  customFields.forEach((field) => {
    if (field.defaultValues.length === 1) {
      customFieldPayload[field.fieldKey] = field.defaultValues[0].value;
      return;
    }

    if (field.defaultValues.length > 1) {
      customFieldPayload[field.fieldKey] = field.defaultValues.map(
        (defaultValue) => defaultValue.value
      );
    }
  });

  return {
    issueTypeId: Number(values.issueTypeId),
    summary: values.summary.trim(),
    description: values.description?.trim() || undefined,
    priorityId: values.priorityId ? Number(values.priorityId) : undefined,
    assigneeId: values.assigneeId ? Number(values.assigneeId) : undefined,
    parentId: values.parentId ? Number(values.parentId) : undefined,
    dueDate: values.dueDate ? new Date(values.dueDate).getTime() : undefined,
    timeOriginalEstimate: values.timeOriginalEstimate
      ? Number(values.timeOriginalEstimate)
      : undefined,
    securityLevelId: values.securityLevelId
      ? Number(values.securityLevelId)
      : undefined,
    customFields:
      Object.keys(customFieldPayload).length > 0
        ? customFieldPayload
        : undefined,
  };
}
