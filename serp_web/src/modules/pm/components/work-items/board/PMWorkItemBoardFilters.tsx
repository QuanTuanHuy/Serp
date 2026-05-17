/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board filters
 */

'use client';

import type React from 'react';
import { useMemo, useState } from 'react';
import { ChevronRight, Search, SlidersHorizontal } from 'lucide-react';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  Input,
  ScrollArea,
  Separator,
} from '@/shared/components/ui';
import { useAppSelector } from '@/shared/hooks';
import { cn } from '@/shared/utils';
import { useGetPmIssueTypesQuery, useGetPmPrioritiesQuery } from '../../../api';
import type { BoardFilterCriterion } from './pmWorkItemBoard.utils';
import { serializeNumberList } from './pmWorkItemBoard.utils';

interface PMWorkItemBoardFiltersProps {
  projectId: number;
  open: boolean;
  assigneeIds: number[];
  issueTypeIds: number[];
  priorityIds: number[];
  onOpenChange: (open: boolean) => void;
  onUpdate: (updates: Record<string, string | undefined>) => void;
  onClear: () => void;
}

export function PMWorkItemBoardFilters({
  projectId,
  open,
  assigneeIds,
  issueTypeIds,
  priorityIds,
  onOpenChange,
  onUpdate,
  onClear,
}: PMWorkItemBoardFiltersProps) {
  const organizationId = useAppSelector(selectOrganizationId);
  const [selectedCriterion, setSelectedCriterion] =
    useState<BoardFilterCriterion>('assignee');
  const [optionSearch, setOptionSearch] = useState('');

  const { data: priorities } = useGetPmPrioritiesQuery(
    {
      projectId,
      page: 0,
      pageSize: 50,
      sortBy: 'sequence',
      sortDirection: 'asc',
    },
    { skip: !open || selectedCriterion !== 'priority' }
  );

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

  const { data: usersResponse } = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      page: 0,
      pageSize: 100,
      status: 'ACTIVE',
    },
    {
      skip: !open || !organizationId || selectedCriterion !== 'assignee',
    }
  );

  const users = useMemo(
    () =>
      [...(usersResponse?.data.items || [])]
        .map((user) => ({
          id: Number(user.id),
          label:
            `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
            user.email ||
            `User #${user.id}`,
        }))
        .sort((left, right) => left.label.localeCompare(right.label)),
    [usersResponse]
  );

  const issueTypeOptions = useMemo(
    () =>
      (issueTypes?.data.items || []).map((item) => ({
        id: item.id,
        label: item.name,
      })),
    [issueTypes]
  );

  const priorityOptions = useMemo(
    () =>
      (priorities?.data.items || []).map((item) => ({
        id: item.id,
        label: item.name,
      })),
    [priorities]
  );

  const activeCount =
    assigneeIds.length + issueTypeIds.length + priorityIds.length;
  const normalizedSearch = optionSearch.trim().toLowerCase();

  const filterOptions = <T extends { label: string }>(items: T[]) => {
    if (!normalizedSearch) return items;
    return items.filter((item) =>
      item.label.toLowerCase().includes(normalizedSearch)
    );
  };

  const visibleUsers = filterOptions(users);
  const visibleIssueTypes = filterOptions(issueTypeOptions);
  const visiblePriorities = filterOptions(priorityOptions);

  const toggleListValue = (key: string, values: number[], value: number) => {
    const nextValues = values.includes(value)
      ? values.filter((item) => item !== value)
      : [...values, value];
    onUpdate({ [key]: serializeNumberList(nextValues) });
  };

  const close = () => onOpenChange(false);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-4xl gap-0 p-0 sm:max-h-[85vh] sm:max-w-4xl'>
        <DialogHeader className='border-b px-5 py-4'>
          <div className='flex items-center justify-between gap-3'>
            <div>
              <DialogTitle className='flex items-center gap-2 text-base'>
                <SlidersHorizontal className='h-4 w-4' />
                Filters
                {activeCount > 0 ? (
                  <Badge variant='secondary'>{activeCount}</Badge>
                ) : null}
              </DialogTitle>
              <p className='mt-1 text-sm text-muted-foreground'>
                Narrow the board without filtering by status columns.
              </p>
            </div>
            <Button type='button' variant='ghost' size='sm' onClick={close}>
              Close
            </Button>
          </div>
        </DialogHeader>

        <div className='grid min-h-[480px] grid-cols-[220px_minmax(0,1fr)]'>
          <aside className='border-r bg-muted/20 p-3'>
            <div className='space-y-1'>
              {(['assignee', 'workType', 'priority'] as const).map(
                (criterion) => {
                  const currentCount =
                    criterion === 'assignee'
                      ? assigneeIds.length
                      : criterion === 'workType'
                        ? issueTypeIds.length
                        : priorityIds.length;

                  return (
                    <button
                      key={criterion}
                      type='button'
                      onClick={() => {
                        setSelectedCriterion(criterion);
                        setOptionSearch('');
                      }}
                      className={cn(
                        'flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm transition-colors hover:bg-background',
                        selectedCriterion === criterion &&
                          'bg-background shadow-sm'
                      )}
                    >
                      <span>
                        {criterion === 'workType'
                          ? 'Work type'
                          : criterion === 'assignee'
                            ? 'Assignee'
                            : 'Priority'}
                      </span>
                      <span className='flex items-center gap-2'>
                        {currentCount > 0 ? (
                          <Badge variant='secondary'>{currentCount}</Badge>
                        ) : null}
                        <ChevronRight className='h-4 w-4 text-muted-foreground' />
                      </span>
                    </button>
                  );
                }
              )}
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

          <section className='flex min-h-0 flex-col p-4'>
            <div className='mb-3'>
              <h3 className='text-sm font-semibold'>
                {selectedCriterion === 'assignee'
                  ? 'Assignee'
                  : selectedCriterion === 'workType'
                    ? 'Work type'
                    : 'Priority'}
              </h3>
              <p className='text-sm text-muted-foreground'>
                Pick one or more values.
              </p>
            </div>

            <div className='relative mb-3'>
              <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                value={optionSearch}
                onChange={(event) => setOptionSearch(event.target.value)}
                placeholder='Search options'
                className='pl-9'
              />
            </div>

            {selectedCriterion === 'assignee' ? (
              <ValueList
                items={visibleUsers}
                values={assigneeIds}
                emptyLabel='No people found'
                onToggle={(id) =>
                  toggleListValue('assigneeIds', assigneeIds, id)
                }
              />
            ) : null}

            {selectedCriterion === 'workType' ? (
              <ValueList
                items={visibleIssueTypes}
                values={issueTypeIds}
                emptyLabel='No work types found'
                onToggle={(id) =>
                  toggleListValue('issueTypeIds', issueTypeIds, id)
                }
              />
            ) : null}

            {selectedCriterion === 'priority' ? (
              <ValueList
                items={visiblePriorities}
                values={priorityIds}
                emptyLabel='No priorities found'
                onToggle={(id) =>
                  toggleListValue('priorityIds', priorityIds, id)
                }
              />
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
    <ScrollArea className='h-[340px] rounded-md border'>
      <div className='space-y-1 p-2'>
        {items.length === 0 ? (
          <p className='px-2 py-1 text-xs text-muted-foreground'>
            {emptyLabel}
          </p>
        ) : null}
        {items.map((item) => (
          <FilterCheckbox
            key={item.id}
            label={item.label}
            checked={values.includes(item.id)}
            onCheckedChange={() => onToggle(item.id)}
          />
        ))}
      </div>
    </ScrollArea>
  );
}

function FilterCheckbox({
  label,
  checked,
  onCheckedChange,
}: {
  label: string;
  checked: boolean;
  onCheckedChange: () => void;
}) {
  return (
    <button
      type='button'
      onClick={onCheckedChange}
      className='flex w-full items-center gap-2 rounded px-2 py-1.5 text-left text-sm hover:bg-muted'
    >
      <span
        className={cn(
          'flex h-4 w-4 items-center justify-center rounded border text-[10px]',
          checked && 'border-primary bg-primary text-primary-foreground'
        )}
      >
        {checked ? '✓' : ''}
      </span>
      <span className='line-clamp-1'>{label}</span>
    </button>
  );
}
