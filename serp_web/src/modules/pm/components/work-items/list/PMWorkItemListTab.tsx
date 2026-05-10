/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item list tab
 */

'use client';

import type React from 'react';
import {
  startTransition,
  useCallback,
  useDeferredValue,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import {
  AlertCircle,
  Flag,
  LayoutList,
  ListChecks,
  RefreshCw,
  Search,
  SlidersHorizontal,
  UserRound,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { useAppSelector } from '@/shared/hooks';
import { cn } from '@/shared/utils';
import {
  useGetPmIssueTypesQuery,
  useGetPmPrioritiesQuery,
  useGetPmStatusesQuery,
  useGetPmWorkItemByIdQuery,
  useSearchPmWorkItemsQuery,
} from '../../../api';
import type { PMWorkItemSearchApi } from '../../../types/api';

type WorkItemListViewMode = 'list' | 'detail';

interface PMWorkItemListTabProps {
  projectId: number;
}

function parseViewMode(value: string | null): WorkItemListViewMode {
  return value === 'detail' ? 'detail' : 'list';
}

function parseIssueId(value: string | null): number | undefined {
  if (!value) return undefined;
  const issueId = Number(value);
  return Number.isFinite(issueId) ? issueId : undefined;
}

function parseNumberList(value: string | null): number[] {
  if (!value) return [];
  return value
    .split(',')
    .map((part) => Number(part))
    .filter((item) => Number.isFinite(item));
}

function serializeNumberList(values: number[]): string | undefined {
  return values.length ? values.join(',') : undefined;
}

function formatDate(value?: number | null): string {
  if (!value) return 'No date';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'No date';
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

function getInitials(name?: string | null): string {
  if (!name) return '?';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}

export function PMWorkItemListTab({ projectId }: PMWorkItemListTabProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const view = parseViewMode(searchParams.get('view'));
  const selectedIssueId = parseIssueId(searchParams.get('issueId'));
  const parentId = parseIssueId(searchParams.get('parentId'));
  const assigneeIds = parseNumberList(searchParams.get('assigneeIds'));
  const issueTypeIds = parseNumberList(searchParams.get('issueTypeIds'));
  const statusIds = parseNumberList(searchParams.get('statusIds'));
  const priorityIds = parseNumberList(searchParams.get('priorityIds'));
  const reporterIds = parseNumberList(searchParams.get('reporterIds'));
  const [keyword, setKeyword] = useState(searchParams.get('q') ?? '');
  const deferredKeyword = useDeferredValue(keyword.trim());

  const searchQuery = useSearchPmWorkItemsQuery({
    projectId,
    params: {
      keyword: deferredKeyword || undefined,
      parentId,
      assigneeIds,
      issueTypeIds,
      statusIds,
      priorityIds,
      reporterIds,
      enriched: true,
      page: 0,
      pageSize: 50,
      sortField: 'rank',
      sortDirection: 'ASC',
    },
  });

  const items = searchQuery.data?.data.items ?? [];
  const totalItems = searchQuery.data?.data.totalItems ?? 0;

  const selectedItem = useMemo(
    () => items.find((item) => item.id === selectedIssueId),
    [items, selectedIssueId]
  );

  const updateUrl = useCallback(
    (updates: Record<string, string | undefined>) => {
      const nextParams = new URLSearchParams(searchParams.toString());
      for (const [key, value] of Object.entries(updates)) {
        if (value) {
          nextParams.set(key, value);
        } else {
          nextParams.delete(key);
        }
      }
      const queryString = nextParams.toString();
      startTransition(() => {
        router.replace(queryString ? `${pathname}?${queryString}` : pathname, {
          scroll: false,
        });
      });
    },
    [pathname, router, searchParams]
  );

  useEffect(() => {
    if (deferredKeyword === (searchParams.get('q') ?? '')) return;
    updateUrl({ q: deferredKeyword || undefined });
  }, [deferredKeyword, searchParams]);

  useEffect(() => {
    if (view !== 'detail' || selectedIssueId || !items[0]) return;
    updateUrl({ issueId: String(items[0].id) });
  }, [items, selectedIssueId, view]);

  const selectIssue = (issueId: number) => {
    updateUrl({
      view: view === 'detail' ? 'detail' : undefined,
      issueId: String(issueId),
    });
  };

  const setView = (nextView: WorkItemListViewMode) => {
    updateUrl({
      view: nextView === 'detail' ? 'detail' : undefined,
      issueId:
        nextView === 'detail'
          ? String(selectedIssueId ?? items[0]?.id ?? '') || undefined
          : undefined,
    });
  };

  const updateFilter = (updates: Record<string, string | undefined>) => {
    updateUrl({ ...updates, issueId: undefined });
  };

  const clearFilters = () => {
    updateFilter({
      parentId: undefined,
      assigneeIds: undefined,
      issueTypeIds: undefined,
      statusIds: undefined,
      priorityIds: undefined,
      reporterIds: undefined,
    });
  };

  return (
    <div className='space-y-4'>
      <Card className='shadow-sm'>
        <CardContent className='p-4'>
          <div className='flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between'>
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Project work items
              </p>
              <h1 className='mt-1 text-2xl font-semibold tracking-tight'>
                List
              </h1>
            </div>
            <div className='flex flex-col gap-2 sm:flex-row sm:items-center'>
              <div className='relative sm:w-80'>
                <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                  placeholder='Search key or summary'
                  className='pl-9'
                />
              </div>
              <div className='inline-flex rounded-md border bg-background p-1'>
                <Button
                  type='button'
                  variant={view === 'list' ? 'secondary' : 'ghost'}
                  size='sm'
                  onClick={() => setView('list')}
                >
                  <ListChecks className='mr-2 h-4 w-4' />
                  List view
                </Button>
                <Button
                  type='button'
                  variant={view === 'detail' ? 'secondary' : 'ghost'}
                  size='sm'
                  onClick={() => setView('detail')}
                >
                  <LayoutList className='mr-2 h-4 w-4' />
                  Detail view
                </Button>
              </div>
              <Button
                type='button'
                variant='outline'
                onClick={() => searchQuery.refetch()}
                disabled={searchQuery.isFetching}
              >
                <RefreshCw
                  className={cn(
                    'mr-2 h-4 w-4',
                    searchQuery.isFetching && 'animate-spin'
                  )}
                />
                Refresh
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {searchQuery.error ? (
        <Alert variant='destructive'>
          <AlertCircle className='h-4 w-4' />
          <AlertTitle>Work items unavailable</AlertTitle>
          <AlertDescription>
            {getErrorMessage(searchQuery.error)}
          </AlertDescription>
        </Alert>
      ) : null}

      <PMWorkItemListFilters
        projectId={projectId}
        parentId={parentId}
        assigneeIds={assigneeIds}
        issueTypeIds={issueTypeIds}
        statusIds={statusIds}
        priorityIds={priorityIds}
        reporterIds={reporterIds}
        onUpdate={updateFilter}
        onClear={clearFilters}
      />

      {view === 'detail' ? (
        <div className='grid gap-4 xl:grid-cols-[440px_minmax(0,1fr)]'>
          <PMWorkItemCompactList
            items={items}
            loading={searchQuery.isLoading}
            selectedIssueId={selectedIssueId}
            totalItems={totalItems}
            onSelect={selectIssue}
          />
          <PMWorkItemDetailPanel
            projectId={projectId}
            workItemId={selectedIssueId}
            fallbackItem={selectedItem}
          />
        </div>
      ) : (
        <PMWorkItemListTable
          items={items}
          loading={searchQuery.isLoading}
          selectedIssueId={selectedIssueId}
          totalItems={totalItems}
          onSelect={selectIssue}
        />
      )}
    </div>
  );
}

type FilterCriterion =
  | 'parent'
  | 'assignee'
  | 'workType'
  | 'status'
  | 'priority'
  | 'reporter';

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

function PMWorkItemListFilters({
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

interface WorkItemListProps {
  items: PMWorkItemSearchApi[];
  loading: boolean;
  selectedIssueId?: number;
  totalItems: number;
  onSelect: (issueId: number) => void;
}

function PMWorkItemListTable({
  items,
  loading,
  selectedIssueId,
  totalItems,
  onSelect,
}: WorkItemListProps) {
  return (
    <Card className='overflow-hidden shadow-sm'>
      <CardHeader className='border-b px-4 py-3'>
        <CardTitle className='text-sm font-medium text-muted-foreground'>
          {totalItems} work items
        </CardTitle>
      </CardHeader>
      <CardContent className='p-0'>
        {loading ? <PMWorkItemListSkeleton /> : null}
        {!loading && items.length === 0 ? <PMWorkItemListEmpty /> : null}
        {!loading && items.length > 0 ? (
          <div className='overflow-x-auto'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className='w-28'>Key</TableHead>
                  <TableHead>Summary</TableHead>
                  <TableHead className='w-40'>Status</TableHead>
                  <TableHead className='w-48'>Assignee</TableHead>
                  <TableHead className='w-36'>Priority</TableHead>
                  <TableHead className='w-36'>Due date</TableHead>
                  <TableHead className='w-36'>Updated</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {items.map((item) => (
                  <TableRow
                    key={item.id}
                    onClick={() => onSelect(item.id)}
                    className={cn(
                      'cursor-pointer',
                      selectedIssueId === item.id && 'bg-primary/5'
                    )}
                  >
                    <TableCell className='font-semibold text-primary'>
                      {item.key}
                    </TableCell>
                    <TableCell>
                      <div className='min-w-64'>
                        <p className='line-clamp-1 font-medium'>
                          {item.summary}
                        </p>
                        <p className='mt-1 text-xs text-muted-foreground'>
                          {item.issueTypeName ?? 'Work item'}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell>
                      <PMStatusBadge item={item} />
                    </TableCell>
                    <TableCell>
                      <PMUserCell item={item} />
                    </TableCell>
                    <TableCell>
                      <PMPriorityCell item={item} />
                    </TableCell>
                    <TableCell className='text-sm text-muted-foreground'>
                      {formatDate(item.dueDate)}
                    </TableCell>
                    <TableCell className='text-sm text-muted-foreground'>
                      {formatDate(item.updatedAt)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

function PMWorkItemCompactList({
  items,
  loading,
  selectedIssueId,
  totalItems,
  onSelect,
}: WorkItemListProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b px-4 py-3'>
        <CardTitle className='text-sm font-medium text-muted-foreground'>
          {totalItems} work items
        </CardTitle>
      </CardHeader>
      <CardContent className='max-h-[calc(100vh-260px)] space-y-2 overflow-y-auto p-3'>
        {loading ? <PMWorkItemListSkeleton compact /> : null}
        {!loading && items.length === 0 ? <PMWorkItemListEmpty /> : null}
        {!loading
          ? items.map((item) => (
              <button
                key={item.id}
                type='button'
                onClick={() => onSelect(item.id)}
                className={cn(
                  'w-full rounded-lg border p-3 text-left transition-colors hover:border-primary/40 hover:bg-muted/60',
                  selectedIssueId === item.id && 'border-primary bg-primary/5'
                )}
              >
                <div className='flex items-start justify-between gap-3'>
                  <div className='min-w-0'>
                    <p className='text-xs font-semibold text-primary'>
                      {item.key}
                    </p>
                    <p className='mt-1 line-clamp-2 text-sm font-medium'>
                      {item.summary}
                    </p>
                  </div>
                  <PMStatusBadge item={item} />
                </div>
                <div className='mt-3 flex items-center justify-between gap-3'>
                  <PMUserCell item={item} compact />
                  <PMPriorityCell item={item} />
                </div>
              </button>
            ))
          : null}
      </CardContent>
    </Card>
  );
}

interface PMWorkItemDetailPanelProps {
  projectId: number;
  workItemId?: number;
  fallbackItem?: PMWorkItemSearchApi;
}

function PMWorkItemDetailPanel({
  projectId,
  workItemId,
  fallbackItem,
}: PMWorkItemDetailPanelProps) {
  const { data, error, isFetching, isLoading } = useGetPmWorkItemByIdQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !workItemId }
  );

  if (!workItemId) {
    return (
      <Card className='min-h-96 shadow-sm'>
        <CardContent className='flex h-96 items-center justify-center text-sm text-muted-foreground'>
          Select work item to view details.
        </CardContent>
      </Card>
    );
  }

  if (isLoading) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='space-y-4 p-6'>
          <Skeleton className='h-6 w-32' />
          <Skeleton className='h-8 w-3/4' />
          <Skeleton className='h-28 w-full' />
          <div className='grid gap-3 md:grid-cols-2'>
            <Skeleton className='h-20' />
            <Skeleton className='h-20' />
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Alert variant='destructive'>
        <AlertCircle className='h-4 w-4' />
        <AlertTitle>Detail unavailable</AlertTitle>
        <AlertDescription>{getErrorMessage(error)}</AlertDescription>
      </Alert>
    );
  }

  const title = data?.summary ?? fallbackItem?.summary ?? 'Work item';
  const key = data?.key ?? fallbackItem?.key ?? `#${workItemId}`;
  const description = data?.description || 'No description provided.';

  return (
    <Card className='min-h-96 shadow-sm'>
      <CardContent className='space-y-6 p-6'>
        <div className='flex flex-col gap-3 border-b pb-5 lg:flex-row lg:items-start lg:justify-between'>
          <div>
            <div className='flex items-center gap-2'>
              <span className='text-sm font-semibold text-primary'>{key}</span>
              {isFetching ? (
                <RefreshCw className='h-3.5 w-3.5 animate-spin text-muted-foreground' />
              ) : null}
            </div>
            <h2 className='mt-2 text-2xl font-semibold tracking-tight'>
              {title}
            </h2>
          </div>
          <PMDetailStatus
            statusName={data?.status?.name ?? fallbackItem?.statusName}
          />
        </div>

        <section>
          <h3 className='text-sm font-semibold'>Description</h3>
          <p className='mt-2 whitespace-pre-wrap text-sm leading-6 text-muted-foreground'>
            {description}
          </p>
        </section>

        <div className='grid gap-4 md:grid-cols-2'>
          <PMDetailField
            label='Issue type'
            value={
              data?.issueType?.name ?? fallbackItem?.issueTypeName ?? 'None'
            }
          />
          <PMDetailField
            label='Priority'
            value={data?.priority?.name ?? fallbackItem?.priorityName ?? 'None'}
          />
          <PMDetailField
            label='Assignee'
            value={
              data?.assignee?.displayName ??
              fallbackItem?.assigneeName ??
              'Unassigned'
            }
          />
          <PMDetailField
            label='Reporter'
            value={
              data?.reporter?.displayName ??
              fallbackItem?.reporterName ??
              'None'
            }
          />
          <PMDetailField
            label='Due date'
            value={formatDate(data?.dueDate ?? fallbackItem?.dueDate)}
          />
          <PMDetailField
            label='Updated'
            value={formatDate(data?.updatedAt ?? fallbackItem?.updatedAt)}
          />
        </div>
      </CardContent>
    </Card>
  );
}

function PMStatusBadge({ item }: { item: PMWorkItemSearchApi }) {
  return (
    <Badge variant='secondary' className='max-w-36 truncate'>
      {item.statusName ?? `Status ${item.statusId}`}
    </Badge>
  );
}

function PMUserCell({
  item,
  compact = false,
}: {
  item: PMWorkItemSearchApi;
  compact?: boolean;
}) {
  const name = item.assigneeName ?? 'Unassigned';
  return (
    <div className='flex min-w-0 items-center gap-2'>
      <Avatar className='h-7 w-7'>
        {item.assigneeAvatarUrl ? (
          <AvatarImage src={item.assigneeAvatarUrl} alt={name} />
        ) : null}
        <AvatarFallback className='text-[10px]'>
          {item.assigneeName ? (
            getInitials(item.assigneeName)
          ) : (
            <UserRound className='h-3 w-3' />
          )}
        </AvatarFallback>
      </Avatar>
      <span
        className={cn(
          'truncate text-sm text-muted-foreground',
          compact && 'max-w-32 text-xs'
        )}
      >
        {name}
      </span>
    </div>
  );
}

function PMPriorityCell({ item }: { item: PMWorkItemSearchApi }) {
  return (
    <span className='inline-flex items-center gap-1.5 text-sm text-muted-foreground'>
      <Flag
        className='h-4 w-4'
        style={item.priorityColor ? { color: item.priorityColor } : undefined}
      />
      <span className='truncate'>{item.priorityName ?? 'None'}</span>
    </span>
  );
}

function PMDetailStatus({ statusName }: { statusName?: string }) {
  return (
    <Badge className='w-fit px-3 py-1 text-sm'>
      {statusName ?? 'Unknown status'}
    </Badge>
  );
}

function PMDetailField({ label, value }: { label: string; value: string }) {
  return (
    <div className='rounded-lg border bg-muted/20 p-4'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {label}
      </p>
      <p className='mt-2 text-sm font-medium'>{value}</p>
    </div>
  );
}

function PMWorkItemListEmpty() {
  return (
    <div className='flex min-h-64 flex-col items-center justify-center gap-2 p-8 text-center text-muted-foreground'>
      <ListChecks className='h-8 w-8' />
      <p className='font-medium'>No work items found</p>
      <p className='text-sm'>
        Try a different keyword or create new work item.
      </p>
    </div>
  );
}

function PMWorkItemListSkeleton({ compact = false }: { compact?: boolean }) {
  const count = compact ? 6 : 8;
  return (
    <div className='space-y-2 p-4'>
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className='flex items-center gap-3'>
          <Skeleton className='h-8 w-20' />
          <Skeleton className='h-8 flex-1' />
          {!compact ? <Skeleton className='h-8 w-32' /> : null}
        </div>
      ))}
    </div>
  );
}
