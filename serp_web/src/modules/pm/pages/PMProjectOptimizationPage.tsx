/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project optimization launch page
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import {
  ArrowLeft,
  CheckSquare,
  Filter,
  PlayCircle,
  Search,
  SlidersHorizontal,
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
  Checkbox,
  Input,
  ScrollArea,
  Separator,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGeneratePmOptimizationRunMutation,
  useSearchPmWorkItemsQuery,
} from '../api';
import type {
  PMOptimizationMode,
  PMGenerateOptimizationRunRequest,
  PMWorkItemSearchApi,
} from '../types/api';

interface PMProjectOptimizationPageProps {
  projectId: string;
}

type DateInputState = {
  planningStart: string;
  planningEnd: string;
};

const MODE_LABELS: Record<PMOptimizationMode, string> = {
  BALANCED_WORKLOAD: 'Balanced workload',
  MINIMAL_REASSIGNMENT: 'Minimal reassignment',
  ASSIGNMENT_ONLY: 'Assignment only',
  SCHEDULE_ONLY: 'Schedule only',
};

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

export function PMProjectOptimizationPage({
  projectId,
}: PMProjectOptimizationPageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const projectNumericId = Number(projectId);
  const initialSelectedIds = useMemo(
    () =>
      (searchParams.get('selected') || '')
        .split(',')
        .map((value) => Number(value))
        .filter((value) => Number.isFinite(value)),
    [searchParams]
  );
  const [keyword, setKeyword] = useState('');
  const [selectedIds, setSelectedIds] = useState<number[]>(initialSelectedIds);
  const [mode, setMode] = useState<PMOptimizationMode>('BALANCED_WORKLOAD');
  const [allowReassignment, setAllowReassignment] = useState(true);
  const [allowScheduleChanges, setAllowScheduleChanges] = useState(true);
  const [dateState, setDateState] = useState<DateInputState>({
    planningStart: toDateInputValue(Date.now()),
    planningEnd: toDateInputValue(Date.now() + 14 * 24 * 60 * 60 * 1000),
  });

  const deferredKeyword = keyword.trim();
  const searchQuery = useSearchPmWorkItemsQuery(
    {
      projectId: projectNumericId,
      params: {
        keyword: deferredKeyword || undefined,
        page: 0,
        pageSize: 50,
        enriched: true,
        sortField: 'rank',
        sortDirection: 'ASC',
      },
    },
    { skip: !Number.isFinite(projectNumericId) }
  );

  const [generateRun, generateState] = useGeneratePmOptimizationRunMutation();

  useEffect(() => {
    if (initialSelectedIds.length > 0) {
      setSelectedIds(initialSelectedIds);
    }
  }, [initialSelectedIds]);

  const selectedItems = useMemo(() => {
    const map = new Map<number, PMWorkItemSearchApi>();
    for (const item of searchQuery.data?.data.items || []) {
      map.set(item.id, item);
    }
    return selectedIds
      .map((id) => map.get(id))
      .filter(Boolean) as PMWorkItemSearchApi[];
  }, [searchQuery.data?.data.items, selectedIds]);

  const toggleSelected = (itemId: number) => {
    setSelectedIds((current) =>
      current.includes(itemId)
        ? current.filter((value) => value !== itemId)
        : [...current, itemId]
    );
  };

  const selectAllVisible = () => {
    const visibleIds = (searchQuery.data?.data.items || []).map(
      (item) => item.id
    );
    setSelectedIds((current) =>
      Array.from(new Set([...current, ...visibleIds]))
    );
  };

  const clearSelected = () => {
    setSelectedIds([]);
  };

  const handleGenerate = async () => {
    const planningStart = dateInputToEpoch(dateState.planningStart);
    const planningEnd = dateInputToEpoch(dateState.planningEnd, true);

    if (!planningStart || !planningEnd) {
      toast.error('Planning range is required.');
      return;
    }

    if (selectedIds.length === 0) {
      toast.error('Select at least one work item.');
      return;
    }

    const body: PMGenerateOptimizationRunRequest = {
      scope: 'SELECTED_WORK_ITEMS',
      mode,
      planningStart,
      planningEnd,
      allowReassignment,
      allowScheduleChanges,
      selectedWorkItemIds: selectedIds,
    };

    try {
      const run = await generateRun({
        projectId: projectNumericId,
        body,
      }).unwrap();

      toast.success('Optimization run generated.');
      router.push(`/pm/projects/${projectId}/optimization-runs/${run.id}`);
    } catch (error) {
      toast.error('Failed to generate optimization run', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!Number.isFinite(projectNumericId)) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='p-6 text-sm text-muted-foreground'>
          Invalid project id.
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-3'>
          <Button
            type='button'
            variant='ghost'
            className='w-fit px-0 text-muted-foreground hover:bg-transparent'
            onClick={() => router.push(`/pm/projects/${projectId}/board`)}
          >
            <ArrowLeft className='mr-2 h-4 w-4' />
            Back to board
          </Button>
          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
              <PlayCircle className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>
                Optimize selected
              </h1>
              <p className='text-sm text-muted-foreground'>
                {selectedIds.length} selected work items
              </p>
            </div>
          </div>
        </div>
        <Button
          type='button'
          size='lg'
          className='gap-2'
          onClick={handleGenerate}
          disabled={generateState.isLoading || selectedIds.length === 0}
        >
          <PlayCircle className='h-4 w-4' />
          Generate run
        </Button>
      </div>

      <div className='grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]'>
        <Card className='shadow-sm'>
          <CardHeader className='border-b'>
            <div className='flex items-center justify-between gap-3'>
              <div>
                <CardTitle className='text-base'>Work items</CardTitle>
                <p className='text-sm text-muted-foreground'>
                  Search and select the items to include in the run.
                </p>
              </div>
              <div className='flex items-center gap-2'>
                <Input
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                  placeholder='Search work items'
                  className='w-72'
                />
                <Button
                  type='button'
                  variant='outline'
                  onClick={selectAllVisible}
                >
                  <CheckSquare className='mr-2 h-4 w-4' />
                  Select visible
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent className='p-0'>
            <div className='flex items-center justify-between border-b px-4 py-3 text-sm text-muted-foreground'>
              <div className='flex items-center gap-2'>
                <Filter className='h-4 w-4' />
                {searchQuery.data?.data.totalItems ?? 0} items
              </div>
              <Button
                type='button'
                variant='ghost'
                size='sm'
                onClick={clearSelected}
              >
                Clear selected
              </Button>
            </div>
            <ScrollArea className='h-[560px]'>
              <div className='divide-y'>
                {(searchQuery.data?.data.items || []).map((item) => (
                  <button
                    key={item.id}
                    type='button'
                    className='flex w-full items-start gap-3 px-4 py-3 text-left hover:bg-muted/40'
                    onClick={() => toggleSelected(item.id)}
                  >
                    <Checkbox
                      checked={selectedIds.includes(item.id)}
                      className='mt-1'
                      onCheckedChange={() => toggleSelected(item.id)}
                    />
                    <div className='min-w-0 flex-1'>
                      <div className='flex items-center gap-2'>
                        <span className='text-xs font-semibold text-primary'>
                          {item.key}
                        </span>
                        <Badge variant='secondary' className='h-5 px-1.5'>
                          {item.issueTypeName || 'Work item'}
                        </Badge>
                      </div>
                      <p className='mt-1 line-clamp-2 text-sm font-medium'>
                        {item.summary}
                      </p>
                      <div className='mt-2 flex flex-wrap gap-3 text-xs text-muted-foreground'>
                        <span>{item.assigneeName || 'Unassigned'}</span>
                        <span>{item.priorityName || 'No priority'}</span>
                        <span>{item.statusName || 'No status'}</span>
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>

        <div className='space-y-5'>
          <Card className='shadow-sm'>
            <CardHeader className='border-b'>
              <CardTitle className='text-base'>Run settings</CardTitle>
            </CardHeader>
            <CardContent className='space-y-4 p-4'>
              <div className='space-y-2'>
                <p className='text-sm font-medium'>Mode</p>
                <div className='grid gap-2'>
                  {(Object.keys(MODE_LABELS) as PMOptimizationMode[]).map(
                    (item) => (
                      <button
                        key={item}
                        type='button'
                        onClick={() => setMode(item)}
                        className={cn(
                          'flex items-center justify-between rounded-md border px-3 py-2 text-left',
                          mode === item &&
                            'border-primary bg-primary/10 text-primary'
                        )}
                      >
                        <span className='text-sm font-medium'>
                          {MODE_LABELS[item]}
                        </span>
                        <Badge variant='secondary'>{item}</Badge>
                      </button>
                    )
                  )}
                </div>
              </div>

              <div className='grid gap-3'>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Planning start</span>
                  <Input
                    type='date'
                    value={dateState.planningStart}
                    onChange={(event) =>
                      setDateState((current) => ({
                        ...current,
                        planningStart: event.target.value,
                      }))
                    }
                  />
                </label>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Planning end</span>
                  <Input
                    type='date'
                    value={dateState.planningEnd}
                    onChange={(event) =>
                      setDateState((current) => ({
                        ...current,
                        planningEnd: event.target.value,
                      }))
                    }
                  />
                </label>
              </div>

              <Separator />

              <div className='space-y-3'>
                <label className='flex items-center justify-between gap-3 rounded-md border px-3 py-2'>
                  <span className='text-sm font-medium'>
                    Allow reassignment
                  </span>
                  <Checkbox
                    checked={allowReassignment}
                    onCheckedChange={(checked) =>
                      setAllowReassignment(checked === true)
                    }
                  />
                </label>
                <label className='flex items-center justify-between gap-3 rounded-md border px-3 py-2'>
                  <span className='text-sm font-medium'>
                    Allow schedule changes
                  </span>
                  <Checkbox
                    checked={allowScheduleChanges}
                    onCheckedChange={(checked) =>
                      setAllowScheduleChanges(checked === true)
                    }
                  />
                </label>
              </div>
            </CardContent>
          </Card>

          <Card className='shadow-sm'>
            <CardHeader className='border-b'>
              <CardTitle className='text-base'>Selected work items</CardTitle>
            </CardHeader>
            <CardContent className='space-y-2 p-4'>
              {selectedItems.length > 0 ? (
                selectedItems.map((item) => (
                  <div
                    key={item.id}
                    className='flex items-start justify-between gap-3 rounded-md border px-3 py-2'
                  >
                    <div className='min-w-0'>
                      <p className='text-sm font-medium'>{item.key}</p>
                      <p className='truncate text-sm text-muted-foreground'>
                        {item.summary}
                      </p>
                    </div>
                    <Badge variant='secondary'>{item.statusName || '-'}</Badge>
                  </div>
                ))
              ) : (
                <div className='rounded-md border border-dashed p-4 text-sm text-muted-foreground'>
                  No selection.
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
