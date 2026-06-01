/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM dependency filters
 */

'use client';

import { useMemo, useState } from 'react';
import { Check, ChevronRight, SlidersHorizontal } from 'lucide-react';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  ScrollArea,
  Separator,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetPmProjectComponentsQuery,
  useGetPmProjectPeopleQuery,
} from '../../../api/projectApi';
import {
  useGetPmIssueTypesQuery,
  useGetPmPrioritiesQuery,
  useGetPmStatusesQuery,
} from '../../../api/workItemApi';
import { serializeNumberList } from './pmProjectDependencies.utils';

type DependencyFilterCriterion =
  | 'assignee'
  | 'workType'
  | 'status'
  | 'priority'
  | 'component'
  | 'parent';

interface PMProjectDependenciesFiltersProps {
  projectId: number;
  open: boolean;
  parentId?: number;
  assigneeIds: number[];
  issueTypeIds: number[];
  statusIds: number[];
  priorityIds: number[];
  componentIds: number[];
  onOpenChange: (open: boolean) => void;
  onUpdate: (updates: Record<string, string | undefined>) => void;
  onClear: () => void;
}

export function PMProjectDependenciesFilters({
  projectId,
  open,
  parentId,
  assigneeIds,
  issueTypeIds,
  statusIds,
  priorityIds,
  componentIds,
  onOpenChange,
  onUpdate,
  onClear,
}: PMProjectDependenciesFiltersProps) {
  const [selectedCriterion, setSelectedCriterion] =
    useState<DependencyFilterCriterion>('assignee');
  const [parentInput, setParentInput] = useState(
    parentId ? String(parentId) : ''
  );

  const { data: projectPeople = [] } = useGetPmProjectPeopleQuery(projectId, {
    skip: !open || selectedCriterion !== 'assignee',
  });
  const { data: issueTypes } = useGetPmIssueTypesQuery(
    {
      projectId,
      page: 0,
      pageSize: 50,
      sortBy: 'name',
      sortDirection: 'asc',
    },
    { skip: !open || selectedCriterion !== 'workType' }
  );
  const { data: statuses } = useGetPmStatusesQuery(
    {
      projectId,
      page: 0,
      pageSize: 50,
      sortBy: 'name',
      sortDirection: 'asc',
    },
    { skip: !open || selectedCriterion !== 'status' }
  );
  const { data: priorities } = useGetPmPrioritiesQuery(
    {
      projectId,
      page: 0,
      pageSize: 50,
      sortBy: 'name',
      sortDirection: 'asc',
    },
    { skip: !open || selectedCriterion !== 'priority' }
  );
  const { data: components } = useGetPmProjectComponentsQuery(
    {
      projectId,
      params: {
        page: 0,
        pageSize: 50,
        sortBy: 'name',
        sortDirection: 'asc',
      },
    },
    { skip: !open || selectedCriterion !== 'component' }
  );

  const users = useMemo(
    () =>
      projectPeople
        .map((person) => ({
          id: person.userId,
          label: person.name || person.email || `User #${person.userId}`,
        }))
        .sort((left, right) => left.label.localeCompare(right.label)),
    [projectPeople]
  );

  const activeCount =
    assigneeIds.length +
    issueTypeIds.length +
    statusIds.length +
    priorityIds.length +
    componentIds.length +
    (parentId ? 1 : 0);

  const toggleListValue = (key: string, values: number[], value: number) => {
    const nextValues = values.includes(value)
      ? values.filter((item) => item !== value)
      : [...values, value];
    onUpdate({ [key]: serializeNumberList(nextValues) });
  };

  const applyParent = () => {
    const trimmed = parentInput.trim();
    const nextParentId = Number(trimmed);
    onUpdate({
      parentId: trimmed && Number.isFinite(nextParentId) ? trimmed : undefined,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-4xl gap-0 p-0 sm:max-h-[85vh] sm:max-w-4xl'>
        <DialogHeader className='border-b px-5 py-4'>
          <div className='flex items-center justify-between gap-3'>
            <DialogTitle className='flex items-center gap-2 text-base'>
              <SlidersHorizontal className='h-4 w-4' />
              Filters
              {activeCount > 0 ? (
                <Badge variant='secondary'>{activeCount}</Badge>
              ) : null}
            </DialogTitle>
            <Button
              type='button'
              variant='ghost'
              size='sm'
              onClick={() => onOpenChange(false)}
            >
              Close
            </Button>
          </div>
        </DialogHeader>

        <div className='grid min-h-[440px] grid-cols-[220px_minmax(0,1fr)]'>
          <aside className='border-r bg-muted/20 p-3'>
            <div className='space-y-1'>
              {(
                [
                  'assignee',
                  'workType',
                  'status',
                  'priority',
                  'component',
                  'parent',
                ] as const
              ).map((criterion) => {
                const count = countForCriterion(criterion, {
                  assigneeIds,
                  issueTypeIds,
                  statusIds,
                  priorityIds,
                  componentIds,
                  parentId,
                });
                return (
                  <button
                    key={criterion}
                    type='button'
                    onClick={() => setSelectedCriterion(criterion)}
                    className={cn(
                      'flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm transition-colors hover:bg-background',
                      selectedCriterion === criterion &&
                        'bg-background shadow-sm'
                    )}
                  >
                    <span>{getCriterionLabel(criterion)}</span>
                    <span className='flex items-center gap-2'>
                      {count > 0 ? (
                        <Badge variant='secondary'>{count}</Badge>
                      ) : null}
                      <ChevronRight className='h-4 w-4 text-muted-foreground' />
                    </span>
                  </button>
                );
              })}
            </div>
            <Separator className='my-4' />
            <Button
              type='button'
              variant='ghost'
              className='w-full justify-start'
              onClick={onClear}
              disabled={activeCount === 0}
            >
              Clear all
            </Button>
          </aside>

          <section className='min-h-0 p-4'>
            {selectedCriterion === 'assignee' ? (
              <ValueList
                items={users}
                values={assigneeIds}
                emptyLabel='No people found'
                onToggle={(id) =>
                  toggleListValue('assigneeIds', assigneeIds, id)
                }
              />
            ) : null}
            {selectedCriterion === 'workType' ? (
              <ValueList
                items={(issueTypes?.data.items || []).map((item) => ({
                  id: item.id,
                  label: item.name,
                }))}
                values={issueTypeIds}
                emptyLabel='No work types found'
                onToggle={(id) =>
                  toggleListValue('issueTypeIds', issueTypeIds, id)
                }
              />
            ) : null}
            {selectedCriterion === 'status' ? (
              <ValueList
                items={(statuses?.data.items || []).map((item) => ({
                  id: item.id,
                  label: item.name,
                }))}
                values={statusIds}
                emptyLabel='No statuses found'
                onToggle={(id) => toggleListValue('statusIds', statusIds, id)}
              />
            ) : null}
            {selectedCriterion === 'priority' ? (
              <ValueList
                items={(priorities?.data.items || []).map((item) => ({
                  id: item.id,
                  label: item.name,
                }))}
                values={priorityIds}
                emptyLabel='No priorities found'
                onToggle={(id) =>
                  toggleListValue('priorityIds', priorityIds, id)
                }
              />
            ) : null}
            {selectedCriterion === 'component' ? (
              <ValueList
                items={(components?.data.items || []).map((item) => ({
                  id: item.id,
                  label: item.name,
                }))}
                values={componentIds}
                emptyLabel='No components found'
                onToggle={(id) =>
                  toggleListValue('componentIds', componentIds, id)
                }
              />
            ) : null}
            {selectedCriterion === 'parent' ? (
              <div className='max-w-sm space-y-3'>
                <Label htmlFor='dependency-parent-filter'>Parent id</Label>
                <Input
                  id='dependency-parent-filter'
                  inputMode='numeric'
                  value={parentInput}
                  onChange={(event) => setParentInput(event.target.value)}
                  placeholder='Work item id'
                />
                <div className='flex gap-2'>
                  <Button type='button' size='sm' onClick={applyParent}>
                    Apply
                  </Button>
                  <Button
                    type='button'
                    variant='ghost'
                    size='sm'
                    onClick={() => {
                      setParentInput('');
                      onUpdate({ parentId: undefined });
                    }}
                  >
                    Clear
                  </Button>
                </div>
              </div>
            ) : null}
          </section>
        </div>
      </DialogContent>
    </Dialog>
  );
}

function ValueList({
  items,
  values,
  emptyLabel,
  onToggle,
}: {
  items: Array<{ id: number; label: string }>;
  values: number[];
  emptyLabel: string;
  onToggle: (id: number) => void;
}) {
  return (
    <ScrollArea className='h-[360px] rounded-md border'>
      <div className='space-y-1 p-2'>
        {items.length === 0 ? (
          <p className='px-2 py-1 text-xs text-muted-foreground'>
            {emptyLabel}
          </p>
        ) : null}
        {items.map((item) => (
          <button
            key={item.id}
            type='button'
            onClick={() => onToggle(item.id)}
            className='flex w-full items-center gap-2 rounded px-2 py-1.5 text-left text-sm hover:bg-muted'
          >
            <span
              className={cn(
                'flex h-4 w-4 items-center justify-center rounded border text-[10px]',
                values.includes(item.id) &&
                  'border-primary bg-primary text-primary-foreground'
              )}
            >
              {values.includes(item.id) ? <Check className='h-3 w-3' /> : null}
            </span>
            <span className='line-clamp-1'>{item.label}</span>
          </button>
        ))}
      </div>
    </ScrollArea>
  );
}

function getCriterionLabel(criterion: DependencyFilterCriterion): string {
  switch (criterion) {
    case 'workType':
      return 'Work type';
    case 'assignee':
      return 'Assignee';
    case 'status':
      return 'Status';
    case 'priority':
      return 'Priority';
    case 'component':
      return 'Component';
    case 'parent':
      return 'Parent';
  }
}

function countForCriterion(
  criterion: DependencyFilterCriterion,
  filters: {
    parentId?: number;
    assigneeIds: number[];
    issueTypeIds: number[];
    statusIds: number[];
    priorityIds: number[];
    componentIds: number[];
  }
) {
  switch (criterion) {
    case 'assignee':
      return filters.assigneeIds.length;
    case 'workType':
      return filters.issueTypeIds.length;
    case 'status':
      return filters.statusIds.length;
    case 'priority':
      return filters.priorityIds.length;
    case 'component':
      return filters.componentIds.length;
    case 'parent':
      return filters.parentId ? 1 : 0;
  }
}
