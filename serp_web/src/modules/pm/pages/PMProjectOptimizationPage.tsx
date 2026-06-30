/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project optimization launch page
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { ArrowLeft, PlayCircle } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { Badge, Button, Card, CardContent } from '@/shared/components/ui';
import {
  getPmOptimizationAlgorithmKeyForObjective,
  getPmOptimizationAlgorithmLabel,
} from '../constants/optimization';
import {
  useGeneratePmOptimizationRunMutation,
  useSearchPmWorkItemsQuery,
} from '../api';
import { fromLocalDateInputValue, toLocalDateInputValue } from '../utils/date';
import type {
  PMGenerateOptimizationRunRequest,
  PMOptimizationChangeScope,
  PMOptimizationObjective,
  PMWorkItemSearchApi,
} from '../types/api';
import { PMOptimizationRunIntentPanel } from '../components/optimization/PMOptimizationRunIntentPanel';
import { PMOptimizationWorkItemPicker } from '../components/optimization/PMOptimizationWorkItemPicker';

interface PMProjectOptimizationPageProps {
  projectId: string;
}

type DateInputState = {
  planningStart: string;
  planningEnd: string;
};

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
        .filter((value) => Number.isInteger(value) && value > 0),
    [searchParams]
  );

  const [keyword, setKeyword] = useState('');
  const [selectedIds, setSelectedIds] = useState<number[]>(initialSelectedIds);
  const [selectedItemCache, setSelectedItemCache] = useState<
    Record<number, PMWorkItemSearchApi>
  >({});
  const [objective, setObjective] =
    useState<PMOptimizationObjective>('BALANCED_WORKLOAD');
  const [changeScope, setChangeScope] = useState<PMOptimizationChangeScope>(
    'ASSIGNMENT_AND_SCHEDULE'
  );
  const algorithmKey = getPmOptimizationAlgorithmKeyForObjective(objective);
  const [dateState, setDateState] = useState<DateInputState>({
    planningStart: toLocalDateInputValue(Date.now()),
    planningEnd: toLocalDateInputValue(Date.now() + 14 * 24 * 60 * 60 * 1000),
  });

  const searchQuery = useSearchPmWorkItemsQuery(
    {
      projectId: projectNumericId,
      params: {
        keyword: keyword.trim() || undefined,
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
  const selectedWorkItemIds = useMemo(
    () => selectedIds.filter((value) => Number.isInteger(value) && value > 0),
    [selectedIds]
  );

  useEffect(() => {
    if (initialSelectedIds.length > 0) {
      setSelectedIds(initialSelectedIds);
    }
  }, [initialSelectedIds]);

  useEffect(() => {
    const items = searchQuery.data?.data.items || [];
    if (!items.length) return;
    setSelectedItemCache((current) => {
      const next = { ...current };
      for (const item of items) {
        next[item.id] = item;
      }
      return next;
    });
  }, [searchQuery.data?.data.items]);

  const selectedItems = useMemo(
    () =>
      selectedWorkItemIds
        .map((id) => selectedItemCache[id])
        .filter(Boolean) as PMWorkItemSearchApi[],
    [selectedItemCache, selectedWorkItemIds]
  );

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
    const planningStart = fromLocalDateInputValue(dateState.planningStart);
    const planningEnd = fromLocalDateInputValue(dateState.planningEnd, true);

    if (!planningStart || !planningEnd) {
      toast.error('Planning range is required.');
      return;
    }

    if (planningStart >= planningEnd) {
      toast.error('Planning start must be before planning end.');
      return;
    }

    if (selectedWorkItemIds.length === 0) {
      toast.error('Select at least one work item.');
      return;
    }

    const body: PMGenerateOptimizationRunRequest = {
      scope: 'SELECTED_WORK_ITEMS',
      algorithmKey,
      objective,
      changeScope,
      planningStart,
      planningEnd,
      selectedWorkItemIds,
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
          <div className='space-y-2'>
            <div className='flex items-center gap-3'>
              <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
                <PlayCircle className='h-5 w-5' />
              </div>
              <div>
                <h1 className='text-3xl font-bold tracking-tight'>
                  Optimize selected
                </h1>
                <p className='text-sm text-muted-foreground'>
                  Launch a reviewable optimization run from selected work items.
                </p>
              </div>
            </div>
            <div className='flex flex-wrap gap-2'>
              <Badge variant='secondary'>
                {selectedWorkItemIds.length} selected
              </Badge>
              <Badge variant='secondary'>{objective}</Badge>
              <Badge variant='secondary'>{changeScope}</Badge>
            </div>
          </div>
        </div>
        <Button
          type='button'
          size='lg'
          className='gap-2'
          onClick={handleGenerate}
          disabled={generateState.isLoading || selectedWorkItemIds.length === 0}
        >
          <PlayCircle className='h-4 w-4' />
          Generate run
        </Button>
      </div>

      <div className='grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]'>
        <PMOptimizationWorkItemPicker
          keyword={keyword}
          onKeywordChange={setKeyword}
          selectedIds={selectedIds}
          items={searchQuery.data?.data.items || []}
          totalItems={searchQuery.data?.data.totalItems}
          isLoading={searchQuery.isFetching}
          onToggleSelected={toggleSelected}
          onSelectVisible={selectAllVisible}
          onClearSelected={clearSelected}
        />

        <div className='space-y-5'>
          <PMOptimizationRunIntentPanel
            objective={objective}
            changeScope={changeScope}
            algorithmKey={algorithmKey}
            planningStart={dateState.planningStart}
            planningEnd={dateState.planningEnd}
            onObjectiveChange={setObjective}
            onChangeScopeChange={setChangeScope}
            onPlanningStartChange={(value) =>
              setDateState((current) => ({
                ...current,
                planningStart: value,
              }))
            }
            onPlanningEndChange={(value) =>
              setDateState((current) => ({
                ...current,
                planningEnd: value,
              }))
            }
          />

          <Card className='shadow-sm'>
            <CardContent className='space-y-4 p-4'>
              <div className='flex items-start justify-between gap-3'>
                <div>
                  <p className='text-sm font-medium'>Selected summary</p>
                  <p className='text-sm text-muted-foreground'>
                    Confirm the run inputs before generating.
                  </p>
                </div>
                <Badge variant='secondary'>{selectedIds.length}</Badge>
              </div>

              <div className='grid min-w-0 gap-3 md:grid-cols-2'>
                <SummaryField
                  label='Algorithm'
                  value={getPmOptimizationAlgorithmLabel(algorithmKey)}
                />
                <SummaryField label='Objective' value={objective} />
                <SummaryField label='Change scope' value={changeScope} />
                <SummaryField
                  label='Planning range'
                  value={`${dateState.planningStart || '-'} -> ${
                    dateState.planningEnd || '-'
                  }`}
                />
              </div>

              <div className='space-y-2'>
                <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
                  Selected items
                </p>
                {selectedItems.length ? (
                  <div className='space-y-2'>
                    {selectedItems.slice(0, 5).map((item) => (
                      <div
                        key={item.id}
                        className='flex items-start justify-between gap-3 rounded-md border px-3 py-2'
                      >
                        <div className='min-w-0'>
                          <p className='break-words text-sm font-medium'>
                            {item.key}
                          </p>
                          <p className='truncate text-sm text-muted-foreground'>
                            {item.summary}
                          </p>
                        </div>
                        <Badge variant='secondary'>
                          {item.statusName || '-'}
                        </Badge>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className='rounded-md border border-dashed p-4 text-sm text-muted-foreground'>
                    No selection.
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

function SummaryField({ label, value }: { label: string; value: string }) {
  return (
    <div className='min-w-0 rounded-md border px-3 py-2'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {label}
      </p>
      <p className='mt-1 break-words text-sm font-medium'>{value || '-'}</p>
    </div>
  );
}
