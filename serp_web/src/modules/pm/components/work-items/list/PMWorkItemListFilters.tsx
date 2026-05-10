/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item list filters
 */

'use client';

import { useDeferredValue, useMemo, useState } from 'react';
import { SlidersHorizontal } from 'lucide-react';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import {
  Badge,
  Button,
  Card,
  CardContent,
  Input,
} from '@/shared/components/ui';
import { useAppSelector } from '@/shared/hooks';
import { cn } from '@/shared/utils';
import {
  useGetPmIssueTypesQuery,
  useGetPmPrioritiesQuery,
  useGetPmStatusesQuery,
  useSearchPmWorkItemsQuery,
} from '../../../api';
import {
  type FilterCriterion,
  parseIssueId,
  parseNumberList,
  serializeNumberList,
} from './pmWorkItemList.utils';

interface PMWorkItemListFiltersProps {
  projectId: number;
  parentId?: number;
  assigneeIds: number[];
  issueTypeIds: number[];
  statusIds: number[];
  priorityIds: number[];
  reporterIds: number[];
  onUpdate: (updates: Record<string, string | undefined>) => void;
  onClear: () => void;
}

export function PMWorkItemListFilters({
  projectId,
  parentId,
  assigneeIds,
  issueTypeIds,
  statusIds,
  priorityIds,
  reporterIds,
  onUpdate,
  onClear,
}: PMWorkItemListFiltersProps) {
  const organizationId = useAppSelector(selectOrganizationId);
  const [openCriterion, setOpenCriterion] = useState<FilterCriterion | null>(
    null
  );
  const [parentSearch, setParentSearch] = useState('');
  const deferredParentSearch = useDeferredValue(parentSearch.trim());

  const { data: statuses } = useGetPmStatusesQuery(
    {
      projectId,
      page: 0,
      pageSize: 50,
      sortBy: 'name',
      sortDirection: 'asc',
    },
    { skip: openCriterion !== 'status' }
  );

  const { data: priorities } = useGetPmPrioritiesQuery(
    {
      projectId,
      page: 0,
      pageSize: 50,
      sortBy: 'sequence',
      sortDirection: 'asc',
    },
    { skip: openCriterion !== 'priority' }
  );

  const { data: issueTypes } = useGetPmIssueTypesQuery(
    {
      projectId,
      page: 0,
      pageSize: 50,
      sortBy: 'name',
      sortDirection: 'asc',
    },
    { skip: openCriterion !== 'workType' }
  );

  const { data: usersResponse } = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      page: 0,
      pageSize: 100,
      status: 'ACTIVE',
    },
    {
      skip:
        !organizationId ||
        (openCriterion !== 'assignee' && openCriterion !== 'reporter'),
    }
  );

  const { data: parentResponse, isFetching: isParentFetching } =
    useSearchPmWorkItemsQuery(
      {
        projectId,
        params: {
          keyword: deferredParentSearch || undefined,
          enriched: true,
          page: 0,
          pageSize: 10,
          sortField: 'updatedAt',
          sortDirection: 'DESC',
        },
      },
      { skip: openCriterion !== 'parent' }
    );

  const users = useMemo(
    () =>
      [...(usersResponse?.data.items || [])].sort((left, right) => {
        const leftName =
          `${left.firstName || ''} ${left.lastName || ''}`.trim();
        const rightName =
          `${right.firstName || ''} ${right.lastName || ''}`.trim();
        return leftName.localeCompare(rightName);
      }),
    [usersResponse]
  );

  const activeCount = [
    parentId ? 1 : 0,
    assigneeIds.length,
    issueTypeIds.length,
    statusIds.length,
    priorityIds.length,
    reporterIds.length,
  ].reduce((total, item) => total + item, 0);

  const toggleListValue = (key: string, values: number[], value: number) => {
    const nextValues = values.includes(value)
      ? values.filter((item) => item !== value)
      : [...values, value];
    onUpdate({ [key]: serializeNumberList(nextValues) });
  };

  const toggleCriterion = (criterion: FilterCriterion) => {
    setOpenCriterion((prev) => (prev === criterion ? null : criterion));
  };

  return (
    <Card className='shadow-sm'>
      <CardContent className='space-y-4 p-4'>
        <div className='flex items-center justify-between gap-3'>
          <div className='flex items-center gap-2 text-sm font-medium'>
            <SlidersHorizontal className='h-4 w-4 text-muted-foreground' />
            Filters
            {activeCount > 0 ? (
              <Badge variant='secondary'>{activeCount}</Badge>
            ) : null}
          </div>
          <Button
            type='button'
            variant='ghost'
            size='sm'
            onClick={onClear}
            disabled={activeCount === 0}
          >
            Clear all
          </Button>
        </div>

        <div className='grid gap-4 lg:grid-cols-2 xl:grid-cols-3'>
          <FilterCriterionDropdown
            title='Parent'
            isOpen={openCriterion === 'parent'}
            activeCount={parentId ? 1 : 0}
            onToggle={() => toggleCriterion('parent')}
          >
            <div className='space-y-2 p-2'>
              <Input
                value={parentSearch}
                onChange={(event) => setParentSearch(event.target.value)}
                placeholder='Search parent'
                className='h-8'
              />
              <div className='max-h-48 space-y-1 overflow-y-auto'>
                <FilterCheckbox
                  label='No parent filter'
                  checked={!parentId}
                  onCheckedChange={() => onUpdate({ parentId: undefined })}
                />
                {(parentResponse?.data.items || []).map((item) => (
                  <FilterCheckbox
                    key={item.id}
                    label={`${item.key} ${item.summary}`}
                    checked={parentId === item.id}
                    onCheckedChange={() =>
                      onUpdate({ parentId: String(item.id) })
                    }
                  />
                ))}
                {isParentFetching ? (
                  <p className='px-2 py-1 text-xs text-muted-foreground'>
                    Loading...
                  </p>
                ) : null}
              </div>
            </div>
          </FilterCriterionDropdown>

          <FilterCriterionDropdown
            title='Assignee'
            isOpen={openCriterion === 'assignee'}
            activeCount={assigneeIds.length}
            onToggle={() => toggleCriterion('assignee')}
          >
            <div className='max-h-64 space-y-1 overflow-y-auto p-2'>
              {users.length === 0 ? (
                <p className='px-2 py-1 text-xs text-muted-foreground'>
                  No users
                </p>
              ) : null}
              {users.map((user) => (
                <FilterCheckbox
                  key={user.id}
                  label={
                    `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
                    user.email ||
                    `User #${user.id}`
                  }
                  checked={assigneeIds.includes(Number(user.id))}
                  onCheckedChange={() =>
                    toggleListValue('assigneeIds', assigneeIds, Number(user.id))
                  }
                />
              ))}
            </div>
          </FilterCriterionDropdown>

          <FilterCriterionDropdown
            title='Work type'
            isOpen={openCriterion === 'workType'}
            activeCount={issueTypeIds.length}
            onToggle={() => toggleCriterion('workType')}
          >
            <div className='max-h-64 space-y-1 overflow-y-auto p-2'>
              {(issueTypes?.data.items || []).length === 0 ? (
                <p className='px-2 py-1 text-xs text-muted-foreground'>
                  No types
                </p>
              ) : null}
              {(issueTypes?.data.items || []).map((item) => (
                <FilterCheckbox
                  key={item.id}
                  label={item.name}
                  checked={issueTypeIds.includes(item.id)}
                  onCheckedChange={() =>
                    toggleListValue('issueTypeIds', issueTypeIds, item.id)
                  }
                />
              ))}
            </div>
          </FilterCriterionDropdown>

          <FilterCriterionDropdown
            title='Status'
            isOpen={openCriterion === 'status'}
            activeCount={statusIds.length}
            onToggle={() => toggleCriterion('status')}
          >
            <div className='max-h-64 space-y-1 overflow-y-auto p-2'>
              {(statuses?.data.items || []).length === 0 ? (
                <p className='px-2 py-1 text-xs text-muted-foreground'>
                  No statuses
                </p>
              ) : null}
              {(statuses?.data.items || []).map((item) => (
                <FilterCheckbox
                  key={item.id}
                  label={item.name}
                  checked={statusIds.includes(item.id)}
                  onCheckedChange={() =>
                    toggleListValue('statusIds', statusIds, item.id)
                  }
                />
              ))}
            </div>
          </FilterCriterionDropdown>

          <FilterCriterionDropdown
            title='Priority'
            isOpen={openCriterion === 'priority'}
            activeCount={priorityIds.length}
            onToggle={() => toggleCriterion('priority')}
          >
            <div className='max-h-64 space-y-1 overflow-y-auto p-2'>
              {(priorities?.data.items || []).length === 0 ? (
                <p className='px-2 py-1 text-xs text-muted-foreground'>
                  No priorities
                </p>
              ) : null}
              {(priorities?.data.items || []).map((item) => (
                <FilterCheckbox
                  key={item.id}
                  label={item.name}
                  checked={priorityIds.includes(item.id)}
                  onCheckedChange={() =>
                    toggleListValue('priorityIds', priorityIds, item.id)
                  }
                />
              ))}
            </div>
          </FilterCriterionDropdown>

          <FilterCriterionDropdown
            title='Reporter'
            isOpen={openCriterion === 'reporter'}
            activeCount={reporterIds.length}
            onToggle={() => toggleCriterion('reporter')}
          >
            <div className='max-h-64 space-y-1 overflow-y-auto p-2'>
              {users.length === 0 ? (
                <p className='px-2 py-1 text-xs text-muted-foreground'>
                  No users
                </p>
              ) : null}
              {users.map((user) => (
                <FilterCheckbox
                  key={user.id}
                  label={
                    `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
                    user.email ||
                    `User #${user.id}`
                  }
                  checked={reporterIds.includes(Number(user.id))}
                  onCheckedChange={() =>
                    toggleListValue('reporterIds', reporterIds, Number(user.id))
                  }
                />
              ))}
            </div>
          </FilterCriterionDropdown>
        </div>
      </CardContent>
    </Card>
  );
}

interface FilterCriterionDropdownProps {
  title: string;
  isOpen: boolean;
  activeCount: number;
  onToggle: () => void;
  children: React.ReactNode;
}

function FilterCriterionDropdown({
  title,
  isOpen,
  activeCount,
  onToggle,
  children,
}: FilterCriterionDropdownProps) {
  return (
    <div className='space-y-2'>
      <button
        type='button'
        onClick={onToggle}
        className='flex w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-left text-sm font-medium transition-colors hover:bg-muted'
      >
        <span className='flex items-center gap-2'>
          {title}
          {activeCount > 0 ? (
            <Badge variant='secondary' className='h-5 px-1.5 text-xs'>
              {activeCount}
            </Badge>
          ) : null}
        </span>
        <span
          className={cn(
            'text-muted-foreground transition-transform',
            isOpen && 'rotate-180'
          )}
        >
          ▼
        </span>
      </button>
      {isOpen ? (
        <div className='rounded-md border bg-background shadow-sm'>
          {children}
        </div>
      ) : null}
    </div>
  );
}

interface FilterCheckboxProps {
  label: string;
  checked: boolean;
  onCheckedChange: () => void;
}

function FilterCheckbox({
  label,
  checked,
  onCheckedChange,
}: FilterCheckboxProps) {
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
