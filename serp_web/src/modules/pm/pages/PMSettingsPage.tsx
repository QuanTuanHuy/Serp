/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings shell page
 */

'use client';

import { useCallback, useDeferredValue, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { ChevronDown, Layers3, Search, Workflow } from 'lucide-react';
import { toast } from 'sonner';

import { getErrorMessage } from '@/lib/store/api';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { Input } from '@/shared/components/ui/input';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { cn } from '@/shared/utils';

import {
  useCreatePmIssueTypeMutation,
  useCreatePmIssueTypeSchemeMutation,
  useCreatePmPriorityMutation,
  useCreatePmPrioritySchemeMutation,
  useCreatePmWorkflowMutation,
  useCreatePmWorkflowSchemeMutation,
  useDeletePmIssueTypeMutation,
  useDeletePmIssueTypeSchemeMutation,
  useDeletePmPriorityMutation,
  useDeletePmPrioritySchemeMutation,
  useDeletePmWorkflowSchemeMutation,
  useGetPmIssueTypeSettingsOverviewQuery,
  useGetPmPrioritySettingsOverviewQuery,
  useGetPmResourceCalendarSettingsOverviewQuery,
  useGetPmWorkflowSettingsOverviewQuery,
  useManagePmIssueTypeSchemeItemsMutation,
  useManagePmPrioritySchemeItemsMutation,
  useManagePmWorkflowSchemeItemsMutation,
  useUpdatePmIssueTypeMutation,
  useUpdatePmIssueTypeSchemeMutation,
  useUpdatePmPriorityMutation,
  useUpdatePmPrioritySchemeMutation,
  useUpdatePmWorkflowMutation,
  useUpdatePmWorkflowSchemeMutation,
} from '../api';
import {
  PriorityDialog,
  PrioritySchemeDialog,
  SchemeDialog,
  WorkflowDialog,
  WorkflowSchemeDialog,
  WorkTypeDialog,
} from '../components/settings/settings-dialogs';
import {
  MiniStat,
  PrioritiesTable,
  PrioritySchemesTable,
  WorkflowSchemesTable,
  WorkflowsTable,
  WorkTypeSchemesTable,
  WorkTypesTable,
} from '../components/settings/settings-tables';
import { PMSkillCatalogSection } from '../components/skills';
import { PMResourceCalendarSettingsSection } from '../components/settings/resource-calendar';
import {
  SETTINGS_GROUPS,
  SETTINGS_ITEMS,
  includesText,
  orderedSelectedPriorityIds,
  orderedSelectedWorkTypeIds,
  orderedWorkflowSchemeItems,
  type DeleteTarget,
  type PMSettingsSection,
  type PriorityDialogState,
  type PrioritySchemeDialogState,
  type SchemeDialogState,
  type WorkflowDialogState,
  type WorkflowSchemeDialogState,
  type WorkTypeDialogState,
} from '../components/settings/settings-page.types';
import type {
  PMCreateIssueTypeRequest,
  PMCreatePriorityRequest,
  PMCreateWorkflowRequest,
  PMManageIssueTypeSchemeItemsRequest,
  PMManagePrioritySchemeItemsRequest,
  PMManageWorkflowSchemeItemsRequest,
  PMPrioritySchemeSettingsApi,
  PMPrioritySettingsApi,
  PMUpdateIssueTypeRequest,
  PMUpdatePriorityRequest,
  PMUpdateWorkflowRequest,
  PMWorkflowSchemeSettingsApi,
  PMWorkflowSettingsApi,
  PMWorkTypeSchemeSettingsApi,
  PMWorkTypeSettingsApi,
} from '../types/api';

const SECTION_DESCRIPTIONS: Record<PMSettingsSection, string> = {
  'work-types': 'Structure the work items that teams can create and track.',
  'work-type-schemes':
    'Choose which work types are available to each project space.',
  skills:
    'Maintain the skill catalog used by people profiles, work item requirements, and optimization ranking.',
  'resource-calendars':
    'Configure workspace-level working calendars and capacity for optimization.',
  workflows:
    'A workflow is a set of statuses and transitions that a work item moves through during its lifecycle.',
  'workflow-schemes':
    'Define which workflows apply to given work types and project spaces.',
  priorities:
    'Add priorities, edit their order, and tune their visual treatment.',
  'priority-schemes':
    'Associate a priority or group of priorities with project spaces.',
};

const SECTION_ADD_LABELS: Record<PMSettingsSection, string> = {
  'work-types': 'Add work type',
  'work-type-schemes': 'Add work type scheme',
  skills: 'Add skill',
  'resource-calendars': 'Add calendar',
  workflows: 'Add workflow',
  'workflow-schemes': 'Add workflow scheme',
  priorities: 'Add priority',
  'priority-schemes': 'Add priority scheme',
};

const SECTION_SEARCH_PLACEHOLDERS: Record<PMSettingsSection, string> = {
  'work-types': 'Filter work types',
  'work-type-schemes': 'Filter work type schemes',
  skills: 'Filter skills',
  'resource-calendars': 'Filter resource calendars',
  workflows: 'Find workflow',
  'workflow-schemes': 'Filter workflow schemes',
  priorities: 'Filter priorities',
  'priority-schemes': 'Filter priority schemes',
};

export function PMSettingsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const rawSection = searchParams.get('section') as PMSettingsSection | null;
  const section = SETTINGS_ITEMS.some((item) => item.key === rawSection)
    ? rawSection!
    : 'work-types';
  const activeSetting = useMemo(
    () => SETTINGS_ITEMS.find((item) => item.key === section),
    [section]
  );

  const [search, setSearch] = useState('');
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const [workTypeDialog, setWorkTypeDialog] =
    useState<WorkTypeDialogState | null>(null);
  const [schemeDialog, setSchemeDialog] = useState<SchemeDialogState | null>(
    null
  );
  const [priorityDialog, setPriorityDialog] =
    useState<PriorityDialogState | null>(null);
  const [prioritySchemeDialog, setPrioritySchemeDialog] =
    useState<PrioritySchemeDialogState | null>(null);
  const [workflowDialog, setWorkflowDialog] =
    useState<WorkflowDialogState | null>(null);
  const [workflowSchemeDialog, setWorkflowSchemeDialog] =
    useState<WorkflowSchemeDialogState | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<DeleteTarget | null>(null);

  const overviewQuery = useGetPmIssueTypeSettingsOverviewQuery();
  const priorityOverviewQuery = useGetPmPrioritySettingsOverviewQuery();
  const workflowOverviewQuery = useGetPmWorkflowSettingsOverviewQuery();
  const resourceCalendarOverviewQuery =
    useGetPmResourceCalendarSettingsOverviewQuery();
  const [createWorkType, createWorkTypeState] = useCreatePmIssueTypeMutation();
  const [updateWorkType, updateWorkTypeState] = useUpdatePmIssueTypeMutation();
  const [deleteWorkType, deleteWorkTypeState] = useDeletePmIssueTypeMutation();
  const [createScheme, createSchemeState] =
    useCreatePmIssueTypeSchemeMutation();
  const [updateScheme, updateSchemeState] =
    useUpdatePmIssueTypeSchemeMutation();
  const [manageSchemeItems, manageSchemeItemsState] =
    useManagePmIssueTypeSchemeItemsMutation();
  const [deleteScheme, deleteSchemeState] =
    useDeletePmIssueTypeSchemeMutation();
  const [createPriority, createPriorityState] = useCreatePmPriorityMutation();
  const [updatePriority, updatePriorityState] = useUpdatePmPriorityMutation();
  const [deletePriority, deletePriorityState] = useDeletePmPriorityMutation();
  const [createPriorityScheme, createPrioritySchemeState] =
    useCreatePmPrioritySchemeMutation();
  const [updatePriorityScheme, updatePrioritySchemeState] =
    useUpdatePmPrioritySchemeMutation();
  const [managePrioritySchemeItems, managePrioritySchemeItemsState] =
    useManagePmPrioritySchemeItemsMutation();
  const [deletePriorityScheme, deletePrioritySchemeState] =
    useDeletePmPrioritySchemeMutation();
  const [createWorkflow, createWorkflowState] = useCreatePmWorkflowMutation();
  const [updateWorkflow, updateWorkflowState] = useUpdatePmWorkflowMutation();
  const [createWorkflowScheme, createWorkflowSchemeState] =
    useCreatePmWorkflowSchemeMutation();
  const [updateWorkflowScheme, updateWorkflowSchemeState] =
    useUpdatePmWorkflowSchemeMutation();
  const [manageWorkflowSchemeItems, manageWorkflowSchemeItemsState] =
    useManagePmWorkflowSchemeItemsMutation();
  const [deleteWorkflowScheme, deleteWorkflowSchemeState] =
    useDeletePmWorkflowSchemeMutation();

  const workTypes = overviewQuery.data?.workTypes ?? [];
  const schemes = overviewQuery.data?.workTypeSchemes ?? [];
  const priorities = priorityOverviewQuery.data?.priorities ?? [];
  const prioritySchemes = priorityOverviewQuery.data?.prioritySchemes ?? [];
  const workflows = workflowOverviewQuery.data?.workflows ?? [];
  const workflowSchemes = workflowOverviewQuery.data?.workflowSchemes ?? [];

  const filteredWorkTypes = useMemo(() => {
    if (!deferredSearch) {
      return workTypes;
    }
    return workTypes.filter(
      (workType) =>
        includesText(workType.name, deferredSearch) ||
        includesText(workType.typeKey, deferredSearch) ||
        includesText(workType.description, deferredSearch)
    );
  }, [deferredSearch, workTypes]);

  const filteredSchemes = useMemo(() => {
    if (!deferredSearch) {
      return schemes;
    }
    return schemes.filter(
      (scheme) =>
        includesText(scheme.name, deferredSearch) ||
        includesText(scheme.description, deferredSearch) ||
        scheme.workTypes.some((workType) =>
          includesText(workType.name, deferredSearch)
        ) ||
        scheme.spaces.some((space) => includesText(space.name, deferredSearch))
    );
  }, [deferredSearch, schemes]);

  const filteredPriorities = useMemo(() => {
    if (!deferredSearch) {
      return priorities;
    }
    return priorities.filter(
      (priority) =>
        includesText(priority.name, deferredSearch) ||
        includesText(priority.priorityKey, deferredSearch) ||
        includesText(priority.description, deferredSearch)
    );
  }, [deferredSearch, priorities]);

  const filteredPrioritySchemes = useMemo(() => {
    if (!deferredSearch) {
      return prioritySchemes;
    }
    return prioritySchemes.filter(
      (scheme) =>
        includesText(scheme.name, deferredSearch) ||
        includesText(scheme.description, deferredSearch) ||
        scheme.priorities.some((priority) =>
          includesText(priority.name, deferredSearch)
        ) ||
        scheme.spaces.some((space) => includesText(space.name, deferredSearch))
    );
  }, [deferredSearch, prioritySchemes]);

  const filteredWorkflows = useMemo(() => {
    if (!deferredSearch) {
      return workflows;
    }
    return workflows.filter(
      (workflow) =>
        includesText(workflow.name, deferredSearch) ||
        includesText(workflow.workflowKey, deferredSearch) ||
        includesText(workflow.description, deferredSearch) ||
        workflow.relatedSchemes.some((scheme) =>
          includesText(scheme.name, deferredSearch)
        ) ||
        workflow.spaces.some((space) =>
          includesText(space.name, deferredSearch)
        )
    );
  }, [deferredSearch, workflows]);

  const filteredWorkflowSchemes = useMemo(() => {
    if (!deferredSearch) {
      return workflowSchemes;
    }
    return workflowSchemes.filter(
      (scheme) =>
        includesText(scheme.name, deferredSearch) ||
        includesText(scheme.description, deferredSearch) ||
        scheme.items.some(
          (item) =>
            includesText(item.workType?.name, deferredSearch) ||
            includesText(item.workflow?.name, deferredSearch)
        ) ||
        scheme.spaces.some((space) => includesText(space.name, deferredSearch))
    );
  }, [deferredSearch, workflowSchemes]);

  const stats = useMemo(
    () => ({
      workTypes: workTypes.length,
      schemes: schemes.length,
      priorities: priorities.length,
      prioritySchemes: prioritySchemes.length,
      workflows: workflows.length,
      workflowSchemes: workflowSchemes.length,
    }),
    [
      priorities.length,
      prioritySchemes.length,
      schemes.length,
      workflowSchemes.length,
      workflows.length,
      workTypes.length,
    ]
  );

  const updateSection = useCallback(
    (nextSection: PMSettingsSection) => {
      const nextParams = new URLSearchParams(searchParams.toString());
      nextParams.set('section', nextSection);
      router.replace(`/pm/settings?${nextParams.toString()}`, {
        scroll: false,
      });
    },
    [router, searchParams]
  );

  const openCreateDialog = useCallback(() => {
    if (section === 'work-types') {
      setWorkTypeDialog({ mode: 'create' });
    } else if (section === 'work-type-schemes') {
      setSchemeDialog({ mode: 'create' });
    } else if (section === 'workflows') {
      setWorkflowDialog({ mode: 'create' });
    } else if (section === 'workflow-schemes') {
      setWorkflowSchemeDialog({ mode: 'create' });
    } else if (section === 'priorities') {
      setPriorityDialog({ mode: 'create' });
    } else if (section === 'priority-schemes') {
      setPrioritySchemeDialog({ mode: 'create' });
    }
  }, [section]);

  const handleWorkTypeSubmit = useCallback(
    async (
      mode: WorkTypeDialogState['mode'],
      id: number | undefined,
      values: PMCreateIssueTypeRequest
    ) => {
      try {
        if (mode === 'create') {
          await createWorkType(values).unwrap();
          toast.success('Work type created.');
        } else if (id) {
          const updateBody: PMUpdateIssueTypeRequest = {
            name: values.name,
            description: values.description,
            iconUrl: values.iconUrl,
            hierarchyLevel: values.hierarchyLevel,
          };
          await updateWorkType({ id, body: updateBody }).unwrap();
          toast.success('Work type updated.');
        }
        setWorkTypeDialog(null);
      } catch (error) {
        toast.error('Unable to save work type', {
          description: getErrorMessage(error),
        });
      }
    },
    [createWorkType, updateWorkType]
  );

  const handleSchemeSubmit = useCallback(
    async (
      mode: SchemeDialogState['mode'],
      id: number | undefined,
      values: {
        name: string;
        description: string | null;
        defaultIssueTypeId: number;
        issueTypeIds: number[];
      }
    ) => {
      if (!values.issueTypeIds.includes(values.defaultIssueTypeId)) {
        toast.error('Default work type must be included in the scheme.');
        return;
      }

      try {
        const itemBody: PMManageIssueTypeSchemeItemsRequest = {
          issueTypeIds: orderedSelectedWorkTypeIds(
            workTypes,
            values.issueTypeIds
          ),
        };

        if (mode === 'create') {
          const created = await createScheme({
            name: values.name,
            description: values.description,
            defaultIssueTypeId: values.defaultIssueTypeId,
          }).unwrap();
          await manageSchemeItems({ id: created.id, body: itemBody }).unwrap();
          toast.success('Work type scheme created.');
        } else if (id) {
          await updateScheme({
            id,
            body: {
              name: values.name,
              description: values.description,
              defaultIssueTypeId: values.defaultIssueTypeId,
            },
          }).unwrap();
          await manageSchemeItems({ id, body: itemBody }).unwrap();
          toast.success('Work type scheme updated.');
        }
        setSchemeDialog(null);
      } catch (error) {
        toast.error('Unable to save work type scheme', {
          description: getErrorMessage(error),
        });
      }
    },
    [createScheme, manageSchemeItems, updateScheme, workTypes]
  );

  const handlePrioritySubmit = useCallback(
    async (
      mode: PriorityDialogState['mode'],
      id: number | undefined,
      values: PMCreatePriorityRequest
    ) => {
      try {
        if (mode === 'create') {
          await createPriority(values).unwrap();
          toast.success('Priority created.');
        } else if (id) {
          const updateBody: PMUpdatePriorityRequest = {
            name: values.name,
            description: values.description,
            iconUrl: values.iconUrl,
            color: values.color,
            sequence: values.sequence,
          };
          await updatePriority({ id, body: updateBody }).unwrap();
          toast.success('Priority updated.');
        }
        setPriorityDialog(null);
      } catch (error) {
        toast.error('Unable to save priority', {
          description: getErrorMessage(error),
        });
      }
    },
    [createPriority, updatePriority]
  );

  const handlePrioritySchemeSubmit = useCallback(
    async (
      mode: PrioritySchemeDialogState['mode'],
      id: number | undefined,
      values: {
        name: string;
        description: string | null;
        defaultPriorityId: number;
        priorityIds: number[];
      }
    ) => {
      if (!values.priorityIds.includes(values.defaultPriorityId)) {
        toast.error('Default priority must be included in the scheme.');
        return;
      }

      try {
        const itemBody: PMManagePrioritySchemeItemsRequest = {
          priorityIds: orderedSelectedPriorityIds(
            priorities,
            values.priorityIds
          ),
        };

        if (mode === 'create') {
          const created = await createPriorityScheme({
            name: values.name,
            description: values.description,
            defaultPriorityId: values.defaultPriorityId,
          }).unwrap();
          await managePrioritySchemeItems({
            id: created.id,
            body: itemBody,
          }).unwrap();
          toast.success('Priority scheme created.');
        } else if (id) {
          await updatePriorityScheme({
            id,
            body: {
              name: values.name,
              description: values.description,
              defaultPriorityId: values.defaultPriorityId,
            },
          }).unwrap();
          await managePrioritySchemeItems({ id, body: itemBody }).unwrap();
          toast.success('Priority scheme updated.');
        }
        setPrioritySchemeDialog(null);
      } catch (error) {
        toast.error('Unable to save priority scheme', {
          description: getErrorMessage(error),
        });
      }
    },
    [
      createPriorityScheme,
      managePrioritySchemeItems,
      priorities,
      updatePriorityScheme,
    ]
  );

  const handleWorkflowSubmit = useCallback(
    async (
      mode: WorkflowDialogState['mode'],
      id: number | undefined,
      values: PMCreateWorkflowRequest
    ) => {
      try {
        if (mode === 'create') {
          await createWorkflow(values).unwrap();
          toast.success('Workflow created.');
        } else if (id) {
          const updateBody: PMUpdateWorkflowRequest = {
            name: values.name,
            description: values.description,
          };
          await updateWorkflow({ id, body: updateBody }).unwrap();
          toast.success('Workflow updated.');
        }
        setWorkflowDialog(null);
      } catch (error) {
        toast.error('Unable to save workflow', {
          description: getErrorMessage(error),
        });
      }
    },
    [createWorkflow, updateWorkflow]
  );

  const handleWorkflowSchemeSubmit = useCallback(
    async (
      mode: WorkflowSchemeDialogState['mode'],
      id: number | undefined,
      values: {
        name: string;
        description: string | null;
        defaultWorkflowId: number;
        workflowByWorkTypeId: Record<number, number | undefined>;
      }
    ) => {
      const itemBody: PMManageWorkflowSchemeItemsRequest = {
        items: orderedWorkflowSchemeItems(
          workTypes,
          values.workflowByWorkTypeId
        ),
      };
      if (itemBody.items.length === 0) {
        toast.error('At least one work type must be mapped to a workflow.');
        return;
      }

      try {
        if (mode === 'create') {
          const created = await createWorkflowScheme({
            name: values.name,
            description: values.description,
            defaultWorkflowId: values.defaultWorkflowId,
          }).unwrap();
          await manageWorkflowSchemeItems({
            id: created.id,
            body: itemBody,
          }).unwrap();
          toast.success('Workflow scheme created.');
        } else if (id) {
          await updateWorkflowScheme({
            id,
            body: {
              name: values.name,
              description: values.description,
              defaultWorkflowId: values.defaultWorkflowId,
            },
          }).unwrap();
          await manageWorkflowSchemeItems({ id, body: itemBody }).unwrap();
          toast.success('Workflow scheme updated.');
        }
        setWorkflowSchemeDialog(null);
      } catch (error) {
        toast.error('Unable to save workflow scheme', {
          description: getErrorMessage(error),
        });
      }
    },
    [
      createWorkflowScheme,
      manageWorkflowSchemeItems,
      updateWorkflowScheme,
      workTypes,
    ]
  );

  const handleMovePriority = useCallback(
    async (priority: PMPrioritySettingsApi, direction: 'up' | 'down') => {
      const ordered = [...priorities].sort(
        (left, right) =>
          (left.sequence ?? Number.MAX_SAFE_INTEGER) -
            (right.sequence ?? Number.MAX_SAFE_INTEGER) || left.id - right.id
      );
      const currentIndex = ordered.findIndex((item) => item.id === priority.id);
      const nextIndex =
        direction === 'up' ? currentIndex - 1 : currentIndex + 1;
      const swapWith = ordered[nextIndex];
      if (currentIndex < 0 || !swapWith) {
        return;
      }

      try {
        await updatePriority({
          id: priority.id,
          body: { sequence: swapWith.sequence ?? nextIndex + 1 },
        }).unwrap();
        await updatePriority({
          id: swapWith.id,
          body: { sequence: priority.sequence ?? currentIndex + 1 },
        }).unwrap();
        toast.success('Priority order updated.');
      } catch (error) {
        toast.error('Unable to update priority order', {
          description: getErrorMessage(error),
        });
      }
    },
    [priorities, updatePriority]
  );

  const handleConfirmDelete = useCallback(async () => {
    if (!deleteTarget) {
      return;
    }

    try {
      if (deleteTarget.kind === 'work-type') {
        await deleteWorkType(deleteTarget.item.id).unwrap();
        toast.success('Work type deleted.');
      } else if (deleteTarget.kind === 'scheme') {
        await deleteScheme(deleteTarget.item.id).unwrap();
        toast.success('Work type scheme deleted.');
      } else if (deleteTarget.kind === 'priority') {
        await deletePriority(deleteTarget.item.id).unwrap();
        toast.success('Priority deleted.');
      } else if (deleteTarget.kind === 'priority-scheme') {
        await deletePriorityScheme(deleteTarget.item.id).unwrap();
        toast.success('Priority scheme deleted.');
      } else {
        await deleteWorkflowScheme(deleteTarget.item.id).unwrap();
        toast.success('Workflow scheme deleted.');
      }
      setDeleteTarget(null);
    } catch (error) {
      toast.error('Unable to delete record', {
        description: getErrorMessage(error),
      });
    }
  }, [
    deletePriority,
    deletePriorityScheme,
    deleteWorkflowScheme,
    deleteScheme,
    deleteTarget,
    deleteWorkType,
  ]);

  const handleEditWorkType = useCallback(
    (item: PMWorkTypeSettingsApi) => setWorkTypeDialog({ mode: 'edit', item }),
    []
  );
  const handleDeleteWorkType = useCallback(
    (item: PMWorkTypeSettingsApi) =>
      setDeleteTarget({ kind: 'work-type', item }),
    []
  );
  const handleEditScheme = useCallback(
    (item: PMWorkTypeSchemeSettingsApi) =>
      setSchemeDialog({ mode: 'edit', item }),
    []
  );
  const handleDeleteScheme = useCallback(
    (item: PMWorkTypeSchemeSettingsApi) =>
      setDeleteTarget({ kind: 'scheme', item }),
    []
  );
  const handleEditPriority = useCallback(
    (item: PMPrioritySettingsApi) => setPriorityDialog({ mode: 'edit', item }),
    []
  );
  const handleDeletePriority = useCallback(
    (item: PMPrioritySettingsApi) =>
      setDeleteTarget({ kind: 'priority', item }),
    []
  );
  const handleEditPriorityScheme = useCallback(
    (item: PMPrioritySchemeSettingsApi) =>
      setPrioritySchemeDialog({ mode: 'edit', item }),
    []
  );
  const handleDeletePriorityScheme = useCallback(
    (item: PMPrioritySchemeSettingsApi) =>
      setDeleteTarget({ kind: 'priority-scheme', item }),
    []
  );
  const handleEditWorkflow = useCallback(
    (item: PMWorkflowSettingsApi) => setWorkflowDialog({ mode: 'edit', item }),
    []
  );

  const handleOpenWorkflowEditor = useCallback(
    (item: PMWorkflowSettingsApi) => {
      router.push(`/pm/settings/workflows/${item.id}`);
    },
    [router]
  );
  const handleEditWorkflowScheme = useCallback(
    (item: PMWorkflowSchemeSettingsApi) =>
      setWorkflowSchemeDialog({ mode: 'edit', item }),
    []
  );
  const handleDeleteWorkflowScheme = useCallback(
    (item: PMWorkflowSchemeSettingsApi) =>
      setDeleteTarget({ kind: 'workflow-scheme', item }),
    []
  );
  const handleWorkTypeDialogOpenChange = useCallback((open: boolean) => {
    if (!open) {
      setWorkTypeDialog(null);
    }
  }, []);
  const handleSchemeDialogOpenChange = useCallback((open: boolean) => {
    if (!open) {
      setSchemeDialog(null);
    }
  }, []);
  const handlePriorityDialogOpenChange = useCallback((open: boolean) => {
    if (!open) {
      setPriorityDialog(null);
    }
  }, []);
  const handlePrioritySchemeDialogOpenChange = useCallback((open: boolean) => {
    if (!open) {
      setPrioritySchemeDialog(null);
    }
  }, []);
  const handleWorkflowDialogOpenChange = useCallback((open: boolean) => {
    if (!open) {
      setWorkflowDialog(null);
    }
  }, []);
  const handleWorkflowSchemeDialogOpenChange = useCallback((open: boolean) => {
    if (!open) {
      setWorkflowSchemeDialog(null);
    }
  }, []);
  const handleDeleteDialogOpenChange = useCallback((open: boolean) => {
    if (!open) {
      setDeleteTarget(null);
    }
  }, []);

  const isDeleting =
    deleteWorkTypeState.isLoading ||
    deleteSchemeState.isLoading ||
    deletePriorityState.isLoading ||
    deletePrioritySchemeState.isLoading ||
    deleteWorkflowSchemeState.isLoading;
  const isSchemeSaving =
    createSchemeState.isLoading ||
    updateSchemeState.isLoading ||
    manageSchemeItemsState.isLoading;
  const isPrioritySchemeSaving =
    createPrioritySchemeState.isLoading ||
    updatePrioritySchemeState.isLoading ||
    managePrioritySchemeItemsState.isLoading;
  const isWorkflowSchemeSaving =
    createWorkflowSchemeState.isLoading ||
    updateWorkflowSchemeState.isLoading ||
    manageWorkflowSchemeItemsState.isLoading;
  const activeItems =
    section === 'work-types'
      ? filteredWorkTypes
      : section === 'work-type-schemes'
        ? filteredSchemes
        : section === 'workflows'
          ? filteredWorkflows
          : section === 'workflow-schemes'
            ? filteredWorkflowSchemes
            : section === 'priorities'
              ? filteredPriorities
              : filteredPrioritySchemes;
  const activeQuery =
    section === 'work-types' || section === 'work-type-schemes'
      ? overviewQuery
      : section === 'workflows' || section === 'workflow-schemes'
        ? workflowOverviewQuery
        : priorityOverviewQuery;

  return (
    <div className='grid gap-6 lg:grid-cols-[280px_minmax(0,1fr)]'>
      <aside className='space-y-4'>
        <Card className='border-border/60 bg-background/90 shadow-sm'>
          <CardHeader className='border-b py-4'>
            <CardTitle className='text-sm'>Switch settings</CardTitle>
          </CardHeader>
          <CardContent className='p-2'>
            <div className='space-y-3 p-1'>
              <Button
                type='button'
                variant='outline'
                className='w-full justify-between'
              >
                <span className='flex items-center gap-2'>
                  <Layers3 className='h-4 w-4' />
                  Work items
                </span>
                <ChevronDown className='h-4 w-4 text-muted-foreground' />
              </Button>

              {SETTINGS_GROUPS.map((group) => (
                <div key={group} className='space-y-2'>
                  <p className='px-2 text-xs font-semibold uppercase text-muted-foreground'>
                    {group}
                  </p>
                  {SETTINGS_ITEMS.filter((item) => item.group === group).map(
                    (item) => {
                      const active = item.key === section;
                      return (
                        <button
                          key={item.key}
                          type='button'
                          onClick={() => updateSection(item.key)}
                          className={cn(
                            'flex w-full items-start gap-3 rounded-md px-3 py-2 text-left transition-colors hover:bg-muted',
                            active && 'bg-primary/10 text-primary'
                          )}
                        >
                          <Workflow className='mt-0.5 h-4 w-4 shrink-0' />
                          <span className='min-w-0'>
                            <span className='block text-sm font-medium'>
                              {item.title}
                            </span>
                            <span className='block text-xs text-muted-foreground'>
                              {item.description}
                            </span>
                          </span>
                        </button>
                      );
                    }
                  )}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </aside>

      <main className='space-y-4'>
        {section === 'skills' ? (
          <PMSkillCatalogSection />
        ) : section === 'resource-calendars' ? (
          <PMResourceCalendarSettingsSection
            overview={resourceCalendarOverviewQuery.data}
            isLoading={resourceCalendarOverviewQuery.isLoading}
            errorMessage={
              resourceCalendarOverviewQuery.error
                ? getErrorMessage(resourceCalendarOverviewQuery.error)
                : undefined
            }
          />
        ) : (
          <>
            <div className='flex flex-col gap-3 border-b border-border/60 pb-4 md:flex-row md:items-start md:justify-between'>
              <div className='space-y-1'>
                <h1 className='text-2xl font-semibold tracking-tight'>
                  {activeSetting?.title}
                </h1>
                <p className='text-sm text-muted-foreground'>
                  {SECTION_DESCRIPTIONS[section]}
                </p>
              </div>
              <Button type='button' onClick={openCreateDialog}>
                {SECTION_ADD_LABELS[section]}
              </Button>
            </div>

            <div className='grid gap-3 md:grid-cols-3 xl:grid-cols-6'>
              <MiniStat title='Work types' value={stats.workTypes} />
              <MiniStat title='Workflows' value={stats.workflows} />
              <MiniStat title='Priorities' value={stats.priorities} />
              <MiniStat title='Schemes' value={stats.schemes} />
              <MiniStat
                title='Workflow schemes'
                value={stats.workflowSchemes}
              />
              <MiniStat
                title='Priority schemes'
                value={stats.prioritySchemes}
              />
            </div>

            <Card className='border-border/60 bg-background/90 shadow-sm'>
              <CardHeader className='flex flex-col gap-3 border-b py-4 md:flex-row md:items-center md:justify-between'>
                <div className='relative w-full md:max-w-sm'>
                  <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                  <Input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder={SECTION_SEARCH_PLACEHOLDERS[section]}
                    className='pl-9'
                  />
                </div>
                <Badge variant='secondary'>{activeItems.length} shown</Badge>
              </CardHeader>
              <CardContent className='p-0'>
                {activeQuery.isLoading ? (
                  <div className='space-y-3 p-4'>
                    {Array.from({ length: 5 }).map((_, index) => (
                      <Skeleton key={index} className='h-12 rounded-md' />
                    ))}
                  </div>
                ) : activeQuery.error ? (
                  <div className='p-6 text-sm text-muted-foreground'>
                    Unable to load PM settings:{' '}
                    {getErrorMessage(activeQuery.error)}
                  </div>
                ) : section === 'work-types' ? (
                  <WorkTypesTable
                    workTypes={filteredWorkTypes}
                    onEdit={handleEditWorkType}
                    onDelete={handleDeleteWorkType}
                  />
                ) : section === 'work-type-schemes' ? (
                  <WorkTypeSchemesTable
                    schemes={filteredSchemes}
                    onEdit={handleEditScheme}
                    onDelete={handleDeleteScheme}
                  />
                ) : section === 'workflows' ? (
                  <WorkflowsTable
                    workflows={filteredWorkflows}
                    onEdit={handleEditWorkflow}
                    onOpenEditor={handleOpenWorkflowEditor}
                  />
                ) : section === 'workflow-schemes' ? (
                  <WorkflowSchemesTable
                    schemes={filteredWorkflowSchemes}
                    onEdit={handleEditWorkflowScheme}
                    onDelete={handleDeleteWorkflowScheme}
                  />
                ) : section === 'priorities' ? (
                  <PrioritiesTable
                    priorities={filteredPriorities}
                    onMove={handleMovePriority}
                    onEdit={handleEditPriority}
                    onDelete={handleDeletePriority}
                  />
                ) : (
                  <PrioritySchemesTable
                    schemes={filteredPrioritySchemes}
                    onEdit={handleEditPriorityScheme}
                    onDelete={handleDeletePriorityScheme}
                  />
                )}
              </CardContent>
            </Card>
          </>
        )}
      </main>

      <WorkTypeDialog
        state={workTypeDialog}
        isSubmitting={
          createWorkTypeState.isLoading || updateWorkTypeState.isLoading
        }
        onOpenChange={handleWorkTypeDialogOpenChange}
        onSubmit={handleWorkTypeSubmit}
      />
      <SchemeDialog
        state={schemeDialog}
        workTypes={workTypes}
        isSubmitting={isSchemeSaving}
        onOpenChange={handleSchemeDialogOpenChange}
        onSubmit={handleSchemeSubmit}
      />
      <PriorityDialog
        state={priorityDialog}
        priorities={priorities}
        isSubmitting={
          createPriorityState.isLoading || updatePriorityState.isLoading
        }
        onOpenChange={handlePriorityDialogOpenChange}
        onSubmit={handlePrioritySubmit}
      />
      <PrioritySchemeDialog
        state={prioritySchemeDialog}
        priorities={priorities}
        isSubmitting={isPrioritySchemeSaving}
        onOpenChange={handlePrioritySchemeDialogOpenChange}
        onSubmit={handlePrioritySchemeSubmit}
      />
      <WorkflowDialog
        state={workflowDialog}
        isSubmitting={
          createWorkflowState.isLoading || updateWorkflowState.isLoading
        }
        onOpenChange={handleWorkflowDialogOpenChange}
        onSubmit={handleWorkflowSubmit}
      />
      <WorkflowSchemeDialog
        state={workflowSchemeDialog}
        workTypes={workTypes}
        workflows={workflows}
        isSubmitting={isWorkflowSchemeSaving}
        onOpenChange={handleWorkflowSchemeDialogOpenChange}
        onSubmit={handleWorkflowSchemeSubmit}
      />
      <ConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={handleDeleteDialogOpenChange}
        title='Delete PM setting'
        description={
          deleteTarget
            ? `Delete "${deleteTarget.item.name}"? This may fail if it is still used by projects or work items.`
            : undefined
        }
        confirmText='Delete'
        variant='destructive'
        isLoading={isDeleting}
        onConfirm={handleConfirmDelete}
      />
    </div>
  );
}
