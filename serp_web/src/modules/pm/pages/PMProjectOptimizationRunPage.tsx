/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project optimization run review page
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  AlertCircle,
  ArrowLeft,
  Check,
  CircleSlash,
  History,
  ListChecks,
  PenLine,
  PlayCircle,
  ShieldAlert,
  Sparkles,
  X,
} from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  ScrollArea,
  Separator,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { useAppSelector } from '@/shared/hooks';
import {
  useApplyPmOptimizationRunMutation,
  useDiscardPmOptimizationRunMutation,
  useGetPmOptimizationRunQuery,
  useUpdatePmOptimizationRunItemDecisionMutation,
} from '../api';
import type {
  PMOptimizationDecision,
  PMOptimizationRunApi,
  PMOptimizationRunItemApi,
} from '../types/api';

interface PMProjectOptimizationRunPageProps {
  projectId: string;
  runId: string;
}

const DECISION_LABELS: Record<PMOptimizationDecision, string> = {
  ACCEPTED: 'Accept',
  REJECTED: 'Reject',
  OVERRIDDEN: 'Override',
  PENDING: 'Pending',
};

function formatDate(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}

function formatValue(value?: string | number | null) {
  if (value === undefined || value === null || value === '') return '-';
  return String(value);
}

function toDateInputValue(value?: number | null) {
  if (!value) return '';
  return new Date(value).toISOString().slice(0, 10);
}

function dateInputToEpoch(value: string, endOfDay = false) {
  if (!value) return undefined;
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return undefined;
  if (endOfDay) {
    date.setHours(23, 59, 59, 999);
  }
  return date.getTime();
}

function metricValue(value?: number | null) {
  return typeof value === 'number' ? value.toString() : '0';
}

export function PMProjectOptimizationRunPage({
  projectId,
  runId,
}: PMProjectOptimizationRunPageProps) {
  const router = useRouter();
  const organizationId = useAppSelector(selectOrganizationId);
  const numericProjectId = Number(projectId);
  const numericRunId = Number(runId);
  const [selectedApplyIds, setSelectedApplyIds] = useState<number[]>([]);
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
  const [activeTab, setActiveTab] = useState('summary');

  const { data, isLoading, isFetching, error, refetch } =
    useGetPmOptimizationRunQuery(
      { projectId: numericProjectId, runId: numericRunId },
      {
        skip:
          !Number.isFinite(numericProjectId) || !Number.isFinite(numericRunId),
      }
    );
  const { data: usersResponse } = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      page: 0,
      pageSize: 100,
      status: 'ACTIVE',
    },
    { skip: !organizationId }
  );
  const [updateDecision, updateState] =
    useUpdatePmOptimizationRunItemDecisionMutation();
  const [applyRun, applyState] = useApplyPmOptimizationRunMutation();
  const [discardRun, discardState] = useDiscardPmOptimizationRunMutation();

  useEffect(() => {
    if (data?.items?.length) {
      setSelectedApplyIds(data.items.map((item) => item.workItemId));
    }
  }, [data?.items]);

  const users = useMemo(
    () =>
      [...(usersResponse?.data.items || [])].map((user) => ({
        id: Number(user.id),
        label:
          `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
          user.email ||
          `User #${user.id}`,
      })),
    [usersResponse]
  );

  const run = data as PMOptimizationRunApi | undefined;

  const handleToggleApply = (workItemId: number) => {
    setSelectedApplyIds((current) =>
      current.includes(workItemId)
        ? current.filter((value) => value !== workItemId)
        : [...current, workItemId]
    );
  };

  const handleDecision = async (
    item: PMOptimizationRunItemApi,
    body: {
      assignmentDecision?: PMOptimizationDecision | null;
      scheduleDecision?: PMOptimizationDecision | null;
      overrideAssigneeId?: number | null;
      overridePlannedStart?: number | null;
      overridePlannedEnd?: number | null;
    }
  ) => {
    try {
      await updateDecision({
        projectId: numericProjectId,
        runId: numericRunId,
        workItemId: item.workItemId,
        body,
      }).unwrap();
      await refetch();
    } catch (patchError) {
      toast.error('Failed to update decision', {
        description: getErrorMessage(patchError),
      });
    }
  };

  const openOverride = (item: PMOptimizationRunItemApi) => {
    setReviewItem(item);
    setOverrideAssignmentDecision(item.assignmentDecision || 'OVERRIDDEN');
    setOverrideScheduleDecision(item.scheduleDecision || 'OVERRIDDEN');
    setOverrideAssigneeId(item.overrideAssigneeId?.toString() || '');
    setOverridePlannedStart(toDateInputValue(item.overridePlannedStart));
    setOverridePlannedEnd(toDateInputValue(item.overridePlannedEnd));
  };

  const saveOverride = async () => {
    if (!reviewItem) return;

    await handleDecision(reviewItem, {
      assignmentDecision: overrideAssignmentDecision,
      scheduleDecision: overrideScheduleDecision,
      overrideAssigneeId: overrideAssigneeId
        ? Number(overrideAssigneeId)
        : undefined,
      overridePlannedStart: dateInputToEpoch(overridePlannedStart),
      overridePlannedEnd: dateInputToEpoch(overridePlannedEnd, true),
    });
    setReviewItem(null);
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
          applyAssignment: true,
          applySchedule: true,
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

  const warnings = run.warnings || [];
  const items = run.items || [];
  const summary = run.summary;

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
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>
                Optimization run
              </h1>
              <p className='text-sm text-muted-foreground'>
                #{run.id} · {run.status || 'GENERATED'}
              </p>
            </div>
          </div>
        </div>

        <div className='flex flex-wrap gap-2'>
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

      <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
        <Metric title='Scope' value={metricValue(summary?.scopeSize)} />
        <Metric
          title='Suggestions'
          value={metricValue(summary?.assignmentSuggestionCount)}
        />
        <Metric title='Warnings' value={metricValue(summary?.warningsCount)} />
        <Metric
          title='Confidence'
          value={summary?.confidenceLevel || 'UNKNOWN'}
        />
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className='grid w-full grid-cols-5'>
          <TabsTrigger value='summary'>Summary</TabsTrigger>
          <TabsTrigger value='assignment'>Assignment</TabsTrigger>
          <TabsTrigger value='schedule'>Schedule</TabsTrigger>
          <TabsTrigger value='risks'>Risks</TabsTrigger>
          <TabsTrigger value='history'>History</TabsTrigger>
        </TabsList>

        <TabsContent value='summary' className='space-y-4'>
          <div className='grid gap-4 xl:grid-cols-2'>
            <Card className='shadow-sm'>
              <CardHeader>
                <CardTitle className='text-base'>Run metadata</CardTitle>
              </CardHeader>
              <CardContent className='grid gap-3 md:grid-cols-2'>
                <Field label='Mode' value={run.mode || '-'} />
                <Field label='Status' value={run.status || '-'} />
                <Field
                  label='Planning start'
                  value={formatDate(run.planningStart)}
                />
                <Field
                  label='Planning end'
                  value={formatDate(run.planningEnd)}
                />
                <Field
                  label='Allow reassignment'
                  value={run.allowReassignment ? 'Yes' : 'No'}
                />
                <Field
                  label='Allow schedule changes'
                  value={run.allowScheduleChanges ? 'Yes' : 'No'}
                />
              </CardContent>
            </Card>
            <Card className='shadow-sm'>
              <CardHeader>
                <CardTitle className='text-base'>Summary</CardTitle>
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
          </div>
        </TabsContent>

        <TabsContent value='assignment' className='space-y-4'>
          <RunItemTable
            title='Assignment suggestions'
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
            mode='assignment'
          />
        </TabsContent>

        <TabsContent value='schedule' className='space-y-4'>
          <RunItemTable
            title='Schedule suggestions'
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
            mode='schedule'
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

      <Dialog
        open={Boolean(reviewItem)}
        onOpenChange={(open) => !open && setReviewItem(null)}
      >
        <DialogContent className='max-w-2xl'>
          <DialogHeader>
            <DialogTitle>Override suggestion</DialogTitle>
          </DialogHeader>
          {reviewItem ? (
            <div className='space-y-4'>
              <div className='grid gap-3 md:grid-cols-2'>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>
                    Assignment decision
                  </span>
                  <select
                    value={overrideAssignmentDecision}
                    onChange={(event) =>
                      setOverrideAssignmentDecision(
                        event.target.value as PMOptimizationDecision
                      )
                    }
                    className='h-10 w-full rounded-md border bg-background px-3 text-sm'
                  >
                    <option value='OVERRIDDEN'>Override</option>
                    <option value='ACCEPTED'>Accept</option>
                    <option value='REJECTED'>Reject</option>
                    <option value='PENDING'>Pending</option>
                  </select>
                </label>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Schedule decision</span>
                  <select
                    value={overrideScheduleDecision}
                    onChange={(event) =>
                      setOverrideScheduleDecision(
                        event.target.value as PMOptimizationDecision
                      )
                    }
                    className='h-10 w-full rounded-md border bg-background px-3 text-sm'
                  >
                    <option value='OVERRIDDEN'>Override</option>
                    <option value='ACCEPTED'>Accept</option>
                    <option value='REJECTED'>Reject</option>
                    <option value='PENDING'>Pending</option>
                  </select>
                </label>
              </div>

              <label className='space-y-1'>
                <span className='text-sm font-medium'>Assignee</span>
                <select
                  value={overrideAssigneeId}
                  onChange={(event) =>
                    setOverrideAssigneeId(event.target.value)
                  }
                  className='h-10 w-full rounded-md border bg-background px-3 text-sm'
                >
                  <option value=''>No override</option>
                  {users.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.label}
                    </option>
                  ))}
                </select>
              </label>

              <div className='grid gap-3 md:grid-cols-2'>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Planned start</span>
                  <Input
                    type='date'
                    value={overridePlannedStart}
                    onChange={(event) =>
                      setOverridePlannedStart(event.target.value)
                    }
                  />
                </label>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Planned end</span>
                  <Input
                    type='date'
                    value={overridePlannedEnd}
                    onChange={(event) =>
                      setOverridePlannedEnd(event.target.value)
                    }
                  />
                </label>
              </div>

              <Separator />
              <div className='text-sm text-muted-foreground'>
                {reviewItem.workItemId}
              </div>
            </div>
          ) : null}
          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => setReviewItem(null)}
            >
              Cancel
            </Button>
            <Button
              type='button'
              onClick={saveOverride}
              disabled={updateState.isLoading}
            >
              Save override
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function Metric({ title, value }: { title: string; value: string }) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='pb-2'>
        <CardTitle className='text-sm text-muted-foreground'>{title}</CardTitle>
      </CardHeader>
      <CardContent className='text-2xl font-semibold'>{value}</CardContent>
    </Card>
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

function RunItemTable({
  title,
  items,
  selectedIds,
  onToggleApply,
  onAccept,
  onReject,
  onOverride,
  mode,
}: {
  title: string;
  items: PMOptimizationRunItemApi[];
  selectedIds: number[];
  onToggleApply: (workItemId: number) => void;
  onAccept: (item: PMOptimizationRunItemApi) => void;
  onReject: (item: PMOptimizationRunItemApi) => void;
  onOverride: (item: PMOptimizationRunItemApi) => void;
  mode: 'assignment' | 'schedule';
}) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>{title}</CardTitle>
      </CardHeader>
      <CardContent className='p-0'>
        <ScrollArea className='h-[640px]'>
          <div className='divide-y'>
            {items.map((item) => (
              <div
                key={item.id}
                className='grid gap-3 px-4 py-3 xl:grid-cols-[28px_minmax(0,1fr)_240px_220px_180px]'
              >
                <div className='pt-1'>
                  <Checkbox
                    checked={selectedIds.includes(item.workItemId)}
                    onCheckedChange={() => onToggleApply(item.workItemId)}
                  />
                </div>
                <div className='min-w-0'>
                  <div className='flex items-center gap-2'>
                    <span className='text-xs font-semibold text-primary'>
                      #{item.workItemId}
                    </span>
                    <Badge variant='secondary'>
                      {mode === 'assignment'
                        ? DECISION_LABELS[item.assignmentDecision || 'PENDING']
                        : DECISION_LABELS[item.scheduleDecision || 'PENDING']}
                    </Badge>
                  </div>
                  <div className='mt-2 flex flex-wrap gap-2 text-xs text-muted-foreground'>
                    <span>Score {formatValue(item.score)}</span>
                    <span>Cost {formatValue(item.cost)}</span>
                    <span>Confidence {formatValue(item.confidence)}</span>
                  </div>
                </div>
                <div className='text-sm text-muted-foreground'>
                  {mode === 'assignment'
                    ? `Current ${formatValue(item.currentAssigneeId)}`
                    : `Current ${formatDate(item.currentPlannedStart)} → ${formatDate(item.currentPlannedEnd)}`}
                </div>
                <div className='text-sm text-muted-foreground'>
                  {mode === 'assignment'
                    ? `Suggested ${formatValue(item.suggestedAssigneeId)}`
                    : `Suggested ${formatDate(item.suggestedPlannedStart)} → ${formatDate(item.suggestedPlannedEnd)}`}
                </div>
                <div className='flex flex-wrap gap-2'>
                  <Button
                    type='button'
                    size='sm'
                    variant='outline'
                    onClick={() => onAccept(item)}
                  >
                    <Check className='mr-2 h-4 w-4' />
                    Accept
                  </Button>
                  <Button
                    type='button'
                    size='sm'
                    variant='outline'
                    onClick={() => onReject(item)}
                  >
                    <X className='mr-2 h-4 w-4' />
                    Reject
                  </Button>
                  <Button
                    type='button'
                    size='sm'
                    variant='ghost'
                    onClick={() => onOverride(item)}
                  >
                    <PenLine className='mr-2 h-4 w-4' />
                    Override
                  </Button>
                </div>
                <div className='xl:col-span-4'>
                  <div className='grid gap-2 md:grid-cols-2'>
                    <DetailList
                      title='Reasons'
                      items={item.assignmentReasons || []}
                    />
                    <DetailList
                      title='Violations'
                      items={item.violations || []}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </ScrollArea>
      </CardContent>
    </Card>
  );
}

function DetailList({ title, items }: { title: string; items: string[] }) {
  return (
    <div className='rounded-md border bg-muted/20 p-3'>
      <div className='mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
        {title}
        <Badge variant='secondary' className='h-5 px-1.5'>
          {items.length}
        </Badge>
      </div>
      <div className='space-y-1 text-sm'>
        {items.length ? (
          items.slice(0, 4).map((item) => (
            <div key={item} className='truncate text-muted-foreground'>
              {item}
            </div>
          ))
        ) : (
          <div className='text-muted-foreground'>-</div>
        )}
      </div>
    </div>
  );
}
