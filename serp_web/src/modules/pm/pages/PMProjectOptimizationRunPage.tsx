/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project optimization run review page
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  CheckCircle2,
  CircleSlash,
  Eraser,
  PlayCircle,
  ShieldAlert,
  Sparkles,
  XCircle,
} from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import {
  useApplyPmOptimizationRunMutation,
  useDiscardPmOptimizationRunMutation,
  useGetPmOptimizationRunQuery,
  useUpdatePmOptimizationRunItemDecisionsMutation,
} from '../api';
import { useGetPmProjectPeopleQuery } from '../api/projectApi';
import { fromLocalDateInputValue, toLocalDateInputValue } from '../utils/date';
import { PMOptimizationRunItemTable } from '../components/optimization/PMOptimizationRunItemTable';
import { PMOptimizationRunOverview } from '../components/optimization/PMOptimizationRunOverview';
import { PMOptimizationRunOverrideDialog } from '../components/optimization/PMOptimizationRunOverrideDialog';
import { getPmOptimizationAlgorithmLabel } from '../constants/optimization';
import type {
  PMOptimizationDecision,
  PMOptimizationScheduleAllocationApi,
  PMOptimizationRunDecisionItemRequest,
  PMOptimizationRunApi,
  PMOptimizationRunItemApi,
} from '../types/api';

interface PMProjectOptimizationRunPageProps {
  projectId: string;
  runId: string;
}

function formatDate(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}

function formatValue(value?: string | number | null) {
  if (value === undefined || value === null || value === '') return '-';
  return String(value);
}

function metricValue(value?: number | null) {
  return typeof value === 'number' ? value.toString() : '0';
}

export function PMProjectOptimizationRunPage({
  projectId,
  runId,
}: PMProjectOptimizationRunPageProps) {
  const router = useRouter();
  const numericProjectId = Number(projectId);
  const numericRunId = Number(runId);
  const [selectedApplyIds, setSelectedApplyIds] = useState<number[]>([]);
  const [hasInitializedSelection, setHasInitializedSelection] = useState(false);
  const [reviewItem, setReviewItem] = useState<PMOptimizationRunItemApi | null>(
    null
  );
  const [overrideAssignmentDecision, setOverrideAssignmentDecision] =
    useState<PMOptimizationDecision>('OVERRIDDEN');
  const [overrideScheduleDecision, setOverrideScheduleDecision] =
    useState<PMOptimizationDecision>('OVERRIDDEN');
  const [overrideAssigneeId, setOverrideAssigneeId] = useState('');
  const [overridePlannedStart, setOverridePlannedStart] = useState('');
  const [overridePlannedEnd, setOverridePlannedEnd] = useState('');
  const [overrideAllocationChunks, setOverrideAllocationChunks] = useState<
    PMOptimizationScheduleAllocationApi[]
  >([]);
  const [activeTab, setActiveTab] = useState('summary');

  const { data, isLoading, error, refetch } = useGetPmOptimizationRunQuery(
    { projectId: numericProjectId, runId: numericRunId },
    {
      skip:
        !Number.isFinite(numericProjectId) || !Number.isFinite(numericRunId),
    }
  );
  const { data: projectPeople = [] } = useGetPmProjectPeopleQuery(
    numericProjectId,
    { skip: !Number.isFinite(numericProjectId) }
  );
  const [updateDecisions, updateState] =
    useUpdatePmOptimizationRunItemDecisionsMutation();
  const [applyRun, applyState] = useApplyPmOptimizationRunMutation();
  const [discardRun, discardState] = useDiscardPmOptimizationRunMutation();

  useEffect(() => {
    setHasInitializedSelection(false);
    setSelectedApplyIds([]);
  }, [numericRunId]);

  useEffect(() => {
    if (data?.items?.length && !hasInitializedSelection) {
      setSelectedApplyIds(data.items.map((item) => item.workItemId));
      setHasInitializedSelection(true);
    }
  }, [data?.items, hasInitializedSelection]);

  const users = useMemo(
    () =>
      [...projectPeople]
        .map((person) => ({
          id: Number(person.userId),
          label: person.name || person.email || `User #${person.userId}`,
        }))
        .sort((left, right) => left.label.localeCompare(right.label)),
    [projectPeople]
  );

  const run = data as PMOptimizationRunApi | undefined;
  const warnings = run?.warnings || [];
  const items = run?.items || [];
  const summary = run?.summary;
  const canEditAssignment = run?.changeScope !== 'SCHEDULE_ONLY';
  const canEditSchedule = run?.changeScope !== 'ASSIGNMENT_ONLY';

  const handleToggleApply = (workItemId: number) => {
    setSelectedApplyIds((current) =>
      current.includes(workItemId)
        ? current.filter((value) => value !== workItemId)
        : [...current, workItemId]
    );
  };

  const handleDecisionBatch = async (
    decisionItems: PMOptimizationRunDecisionItemRequest[],
    successMessage = 'Decision updated.'
  ) => {
    if (!decisionItems.length) {
      toast.error('Select at least one work item to review.');
      return false;
    }

    try {
      await updateDecisions({
        projectId: numericProjectId,
        runId: numericRunId,
        body: { items: decisionItems },
      }).unwrap();
      toast.success(successMessage);
      await refetch();
      return true;
    } catch (patchError) {
      toast.error('Failed to update decisions', {
        description: getErrorMessage(patchError),
      });
      return false;
    }
  };

  const handleDecision = (
    item: PMOptimizationRunItemApi,
    body: Omit<PMOptimizationRunDecisionItemRequest, 'workItemId'>
  ) => handleDecisionBatch([{ workItemId: item.workItemId, ...body }]);

  const handleBulkDecision = (decision: 'ACCEPTED' | 'REJECTED') => {
    if (activeTab !== 'assignment' && activeTab !== 'schedule') {
      toast.error('Open Assignment or Schedule to review selected items.');
      return;
    }
    const selectedItems = items.filter((item) =>
      selectedApplyIds.includes(item.workItemId)
    );
    if (!selectedItems.length) {
      toast.error('Select at least one work item to review.');
      return;
    }
    const decisionItems = selectedItems.map((item) =>
      activeTab === 'assignment'
        ? { workItemId: item.workItemId, assignmentDecision: decision }
        : { workItemId: item.workItemId, scheduleDecision: decision }
    );

    void handleDecisionBatch(
      decisionItems,
      decision === 'ACCEPTED'
        ? 'Selected suggestions accepted.'
        : 'Selected suggestions rejected.'
    );
  };

  const openOverride = (item: PMOptimizationRunItemApi) => {
    setReviewItem(item);
    setOverrideAssignmentDecision(item.assignmentDecision || 'OVERRIDDEN');
    setOverrideScheduleDecision(item.scheduleDecision || 'OVERRIDDEN');
    setOverrideAssigneeId(item.overrideAssigneeId?.toString() || '');
    setOverridePlannedStart(toLocalDateInputValue(item.overridePlannedStart));
    setOverridePlannedEnd(toLocalDateInputValue(item.overridePlannedEnd));
    const effectiveChunks =
      item.scheduleDecision === 'OVERRIDDEN' &&
      item.overrideAllocationChunks?.length
        ? item.overrideAllocationChunks
        : item.allocationChunks || [];
    setOverrideAllocationChunks(
      effectiveChunks.length
        ? effectiveChunks.map((chunk) => ({ ...chunk }))
        : [
            {
              assigneeId:
                item.overrideAssigneeId ||
                item.suggestedAssigneeId ||
                item.currentAssigneeId ||
                0,
              start:
                item.overridePlannedStart ||
                item.suggestedPlannedStart ||
                item.currentPlannedStart ||
                Date.now(),
              end:
                item.overridePlannedEnd ||
                item.suggestedPlannedEnd ||
                item.currentPlannedEnd ||
                Date.now() + 60 * 60 * 1000,
              effortMillis: 60 * 60 * 1000,
            },
          ]
    );
  };

  const saveOverride = async () => {
    if (!reviewItem) return;

    const validOverrideAllocationChunks = overrideAllocationChunks.filter(
      (chunk) =>
        chunk.assigneeId > 0 &&
        chunk.start > 0 &&
        chunk.end > chunk.start &&
        chunk.effortMillis > 0
    );

    if (
      overrideScheduleDecision === 'OVERRIDDEN' &&
      validOverrideAllocationChunks.length === 0
    ) {
      toast.error('Add at least one valid schedule allocation.');
      return;
    }

    const saved = await handleDecision(reviewItem, {
      assignmentDecision: overrideAssignmentDecision,
      scheduleDecision: overrideScheduleDecision,
      overrideAssigneeId: overrideAssigneeId
        ? Number(overrideAssigneeId)
        : undefined,
      overridePlannedStart:
        overrideScheduleDecision === 'OVERRIDDEN'
          ? undefined
          : fromLocalDateInputValue(overridePlannedStart),
      overridePlannedEnd:
        overrideScheduleDecision === 'OVERRIDDEN'
          ? undefined
          : fromLocalDateInputValue(overridePlannedEnd, true),
      overrideAllocationChunks:
        overrideScheduleDecision === 'OVERRIDDEN'
          ? validOverrideAllocationChunks
          : undefined,
    });
    if (saved) {
      setReviewItem(null);
    }
  };

  const handleApply = async () => {
    if (!run || selectedApplyIds.length === 0) {
      toast.error('Select at least one work item to apply.');
      return;
    }

    try {
      await applyRun({
        projectId: numericProjectId,
        runId: numericRunId,
        body: {
          applyAssignment: canEditAssignment,
          applySchedule: canEditSchedule,
          workItemIds: selectedApplyIds,
        },
      }).unwrap();
      toast.success('Optimization applied.');
      await refetch();
    } catch (applyError) {
      toast.error('Failed to apply optimization', {
        description: getErrorMessage(applyError),
      });
    }
  };

  const handleDiscard = async () => {
    try {
      await discardRun({
        projectId: numericProjectId,
        runId: numericRunId,
      }).unwrap();
      toast.success('Optimization discarded.');
      await refetch();
    } catch (discardError) {
      toast.error('Failed to discard optimization', {
        description: getErrorMessage(discardError),
      });
    }
  };

  if (!Number.isFinite(numericProjectId) || !Number.isFinite(numericRunId)) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='p-6 text-sm text-muted-foreground'>
          Invalid optimization run.
        </CardContent>
      </Card>
    );
  }

  if (isLoading) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='p-6 text-sm text-muted-foreground'>
          Loading optimization run...
        </CardContent>
      </Card>
    );
  }

  if (error || !run) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='space-y-3 p-6 text-sm text-muted-foreground'>
          <div>Optimization run unavailable.</div>
          <Button
            type='button'
            variant='outline'
            onClick={() =>
              router.push(`/pm/projects/${projectId}/optimization`)
            }
          >
            Back to optimization
          </Button>
        </CardContent>
      </Card>
    );
  }

  const assignmentTabDisabled = !canEditAssignment;
  const scheduleTabDisabled = !canEditSchedule;
  const bulkReviewDisabled =
    (activeTab !== 'assignment' && activeTab !== 'schedule') ||
    updateState.isLoading ||
    selectedApplyIds.length === 0 ||
    (activeTab === 'assignment' && assignmentTabDisabled) ||
    (activeTab === 'schedule' && scheduleTabDisabled);

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-3'>
          <Button
            type='button'
            variant='ghost'
            className='w-fit px-0 text-muted-foreground hover:bg-transparent'
            onClick={() =>
              router.push(`/pm/projects/${projectId}/optimization`)
            }
          >
            <ArrowLeft className='mr-2 h-4 w-4' />
            Back to optimization
          </Button>
          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
              <Sparkles className='h-5 w-5' />
            </div>
            <div className='space-y-1'>
              <h1 className='text-3xl font-bold tracking-tight'>
                Optimization run
              </h1>
              <p className='text-sm text-muted-foreground'>
                #{run.id} · {run.status || 'GENERATED'}
              </p>
              <div className='flex flex-wrap gap-2'>
                <Badge variant='secondary'>{formatValue(run.objective)}</Badge>
                <Badge variant='secondary'>
                  {formatValue(run.changeScope)}
                </Badge>
                <Badge variant='secondary'>
                  {getPmOptimizationAlgorithmLabel(run.algorithmKey)}
                </Badge>
              </div>
            </div>
          </div>
        </div>

        <div className='flex flex-wrap gap-2'>
          <Button
            type='button'
            variant='outline'
            onClick={() => handleBulkDecision('ACCEPTED')}
            disabled={bulkReviewDisabled}
          >
            <CheckCircle2 className='mr-2 h-4 w-4' />
            Accept selected
          </Button>
          <Button
            type='button'
            variant='outline'
            onClick={() => handleBulkDecision('REJECTED')}
            disabled={bulkReviewDisabled}
          >
            <XCircle className='mr-2 h-4 w-4' />
            Reject selected
          </Button>
          <Button
            type='button'
            variant='ghost'
            onClick={() => setSelectedApplyIds([])}
            disabled={selectedApplyIds.length === 0}
          >
            <Eraser className='mr-2 h-4 w-4' />
            Clear selection
          </Button>
          <Button
            type='button'
            onClick={handleApply}
            disabled={applyState.isLoading || selectedApplyIds.length === 0}
          >
            <PlayCircle className='mr-2 h-4 w-4' />
            Apply selected
          </Button>
          <Button
            type='button'
            variant='outline'
            onClick={handleDiscard}
            disabled={discardState.isLoading}
          >
            <CircleSlash className='mr-2 h-4 w-4' />
            Discard
          </Button>
        </div>
      </div>

      <PMOptimizationRunOverview run={run} summary={summary} />

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className='grid w-full grid-cols-5'>
          <TabsTrigger value='summary'>Summary</TabsTrigger>
          <TabsTrigger value='assignment' disabled={assignmentTabDisabled}>
            Assignment
          </TabsTrigger>
          <TabsTrigger value='schedule' disabled={scheduleTabDisabled}>
            Schedule
          </TabsTrigger>
          <TabsTrigger value='risks'>Risks</TabsTrigger>
          <TabsTrigger value='history'>History</TabsTrigger>
        </TabsList>

        <TabsContent value='summary' className='space-y-4'>
          <div className='grid gap-4 xl:grid-cols-2'>
            <Card className='shadow-sm'>
              <CardHeader>
                <CardTitle className='text-base'>Before / after</CardTitle>
              </CardHeader>
              <CardContent className='grid gap-3 md:grid-cols-2'>
                <Field
                  label='Late items before'
                  value={metricValue(summary?.lateItemsBefore)}
                />
                <Field
                  label='Late items after'
                  value={metricValue(summary?.lateItemsAfter)}
                />
                <Field
                  label='Overloaded before'
                  value={metricValue(summary?.overloadedAssigneeCountBefore)}
                />
                <Field
                  label='Overloaded after'
                  value={metricValue(summary?.overloadedAssigneeCountAfter)}
                />
              </CardContent>
            </Card>
            <Card className='shadow-sm'>
              <CardHeader>
                <CardTitle className='text-base'>Key highlights</CardTitle>
              </CardHeader>
              <CardContent className='space-y-2 text-sm text-muted-foreground'>
                <p>Scope size: {metricValue(summary?.scopeSize)}</p>
                <p>
                  Suggestions: {metricValue(summary?.assignmentSuggestionCount)}
                </p>
                <p>Warnings: {metricValue(summary?.warningsCount)}</p>
                <p>Confidence: {formatValue(summary?.confidenceLevel)}</p>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value='assignment' className='space-y-4'>
          <PMOptimizationRunItemTable
            title='Assignment suggestions'
            mode='assignment'
            items={items}
            selectedIds={selectedApplyIds}
            onToggleApply={handleToggleApply}
            onAccept={(item) =>
              handleDecision(item, { assignmentDecision: 'ACCEPTED' })
            }
            onReject={(item) =>
              handleDecision(item, { assignmentDecision: 'REJECTED' })
            }
            onOverride={openOverride}
            disabled={assignmentTabDisabled || updateState.isLoading}
            disabledMessage={
              assignmentTabDisabled
                ? 'This run was generated as schedule-only, so assignment changes are not editable.'
                : undefined
            }
          />
        </TabsContent>

        <TabsContent value='schedule' className='space-y-4'>
          <PMOptimizationRunItemTable
            title='Schedule suggestions'
            mode='schedule'
            items={items}
            selectedIds={selectedApplyIds}
            onToggleApply={handleToggleApply}
            onAccept={(item) =>
              handleDecision(item, { scheduleDecision: 'ACCEPTED' })
            }
            onReject={(item) =>
              handleDecision(item, { scheduleDecision: 'REJECTED' })
            }
            onOverride={openOverride}
            disabled={scheduleTabDisabled || updateState.isLoading}
            disabledMessage={
              scheduleTabDisabled
                ? 'This run was generated as assignment-only, so schedule changes are not editable.'
                : undefined
            }
          />
        </TabsContent>

        <TabsContent value='risks' className='space-y-4'>
          <Card className='shadow-sm'>
            <CardHeader>
              <CardTitle className='text-base'>Warnings</CardTitle>
            </CardHeader>
            <CardContent className='space-y-3'>
              {warnings.length ? (
                warnings.map((warning) => (
                  <div
                    key={warning.id}
                    className='rounded-md border px-3 py-2 text-sm'
                  >
                    <div className='flex items-center gap-2'>
                      <ShieldAlert className='h-4 w-4 text-destructive' />
                      <span className='font-medium'>
                        {warning.code || 'WARNING'}
                      </span>
                      <Badge variant='secondary'>
                        {warning.severity || 'INFO'}
                      </Badge>
                    </div>
                    <p className='mt-1 text-muted-foreground'>
                      {warning.message || '-'}
                    </p>
                    {warning.detailsJson ? (
                      <pre className='mt-2 overflow-x-auto rounded bg-muted p-2 text-xs'>
                        {warning.detailsJson}
                      </pre>
                    ) : null}
                  </div>
                ))
              ) : (
                <div className='rounded-md border border-dashed p-6 text-sm text-muted-foreground'>
                  No warnings.
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='history' className='space-y-4'>
          <Card className='shadow-sm'>
            <CardHeader>
              <CardTitle className='text-base'>History</CardTitle>
            </CardHeader>
            <CardContent className='grid gap-3 md:grid-cols-2'>
              <Field label='Created at' value={formatDate(run.createdAt)} />
              <Field label='Updated at' value={formatDate(run.updatedAt)} />
              <Field label='Created by' value={formatValue(run.createdBy)} />
              <Field label='Updated by' value={formatValue(run.updatedBy)} />
              <Field label='Run items' value={metricValue(items.length)} />
              <Field label='Warnings' value={metricValue(warnings.length)} />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <PMOptimizationRunOverrideDialog
        open={Boolean(reviewItem)}
        item={reviewItem}
        users={users}
        assignmentDecision={overrideAssignmentDecision}
        scheduleDecision={overrideScheduleDecision}
        overrideAssigneeId={overrideAssigneeId}
        overridePlannedStart={overridePlannedStart}
        overridePlannedEnd={overridePlannedEnd}
        overrideAllocationChunks={overrideAllocationChunks}
        projectPeople={projectPeople}
        onAssignmentDecisionChange={setOverrideAssignmentDecision}
        onScheduleDecisionChange={setOverrideScheduleDecision}
        onOverrideAssigneeIdChange={setOverrideAssigneeId}
        onOverridePlannedStartChange={setOverridePlannedStart}
        onOverridePlannedEndChange={setOverridePlannedEnd}
        onOverrideAllocationChunksChange={setOverrideAllocationChunks}
        onSave={saveOverride}
        onClose={() => setReviewItem(null)}
        isSaving={updateState.isLoading}
      />
    </div>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className='rounded-md border px-3 py-2'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {label}
      </p>
      <p className='mt-1 text-sm font-medium'>{value}</p>
    </div>
  );
}
