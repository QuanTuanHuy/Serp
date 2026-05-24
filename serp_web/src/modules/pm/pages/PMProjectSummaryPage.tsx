/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project summary page
 */

'use client';

import { useMemo, useState, type ComponentType, type ReactNode } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import {
  CalendarClock,
  CheckCircle2,
  History,
  ListFilter,
  MessageSquare,
  Pencil,
  PlusCircle,
  Search,
  X,
} from 'lucide-react';

import { getErrorMessage } from '@/lib/store/api';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Input } from '@/shared/components/ui/input';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { cn } from '@/shared/utils';
import { useGetPmProjectSummaryQuery } from '../api/projectApi';
import type {
  PMProjectSummaryBreakdownItemApi,
  PMProjectSummaryDashboardApi,
  PMProjectSummaryFilterParams,
  PMProjectSummaryParentOptionApi,
} from '../types/api';

const STATUS_COLORS = ['#579DFF', '#94C748', '#F5CD47', '#C084FC', '#F87168'];
const WORK_TYPE_COLORS = [
  '#579DFF',
  '#C084FC',
  '#F87168',
  '#94C748',
  '#F5CD47',
];
const FILTER_NAMES = [
  'Assignee',
  'Created',
  'Due date',
  'Parent',
  'Priority',
  'Status',
  'Updated',
  'Work type',
];

interface PMProjectSummaryPageProps {
  projectId: string;
}

type FilterState = Omit<
  PMProjectSummaryFilterParams,
  'activityPage' | 'activitySize'
>;

type MultiFilterKey =
  | 'assigneeIds'
  | 'priorityIds'
  | 'statusIds'
  | 'issueTypeIds';

function toNumber(value: string) {
  const parsed = Number(value);
  return Number.isNaN(parsed) ? undefined : parsed;
}

function toDateInputValue(value?: number) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toISOString().slice(0, 10);
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

function formatRelativeDate(value?: number | string | null) {
  if (!value) return 'Unknown time';
  const date = typeof value === 'number' ? new Date(value) : new Date(value);
  if (Number.isNaN(date.getTime())) return 'Unknown time';
  const diffMs = Date.now() - date.getTime();
  const diffDays = Math.floor(diffMs / (24 * 60 * 60 * 1000));
  if (diffDays <= 0) return 'today';
  if (diffDays === 1) return '1 day ago';
  if (diffDays < 7) return `${diffDays} days ago`;
  return date.toLocaleDateString();
}

function getTotal(items: PMProjectSummaryBreakdownItemApi[]) {
  return items.reduce((total, item) => total + item.count, 0);
}

function getPercent(count: number, total: number) {
  if (!total) return 0;
  return Math.round((count / total) * 100);
}

function getActiveFilterCount(filters: FilterState) {
  return [
    filters.assigneeIds?.length || 0,
    filters.priorityIds?.length || 0,
    filters.statusIds?.length || 0,
    filters.issueTypeIds?.length || 0,
    filters.parentId ? 1 : 0,
    filters.createdFrom || filters.createdTo ? 1 : 0,
    filters.updatedFrom || filters.updatedTo ? 1 : 0,
    filters.dueDateFrom || filters.dueDateTo ? 1 : 0,
  ].reduce((total, count) => total + count, 0);
}

function MetricCard({
  icon: Icon,
  value,
  title,
  caption,
}: {
  icon: ComponentType<{ className?: string }>;
  value: number;
  title: string;
  caption: string;
}) {
  return (
    <div className='flex min-h-16 items-center gap-3 rounded-lg border bg-card px-4 py-3 shadow-sm'>
      <div className='flex h-10 w-10 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground'>
        <Icon className='h-5 w-5' />
      </div>
      <div className='min-w-0'>
        <div className='truncate text-lg font-semibold'>
          {value} {title}
        </div>
        <div className='truncate text-xs text-muted-foreground'>{caption}</div>
      </div>
    </div>
  );
}

function OptionCheckbox({
  checked,
  label,
  onCheckedChange,
}: {
  checked: boolean;
  label: string;
  onCheckedChange: (checked: boolean) => void;
}) {
  return (
    <label className='flex min-h-8 cursor-pointer items-center gap-2 rounded px-2 text-sm hover:bg-muted'>
      <Checkbox
        checked={checked}
        onCheckedChange={(value) => onCheckedChange(value === true)}
      />
      <span className='truncate'>{label}</span>
    </label>
  );
}

function FilterPanel({
  filters,
  data,
  onToggleId,
  onParentChange,
  onDateChange,
  onClear,
}: {
  filters: FilterState;
  data?: PMProjectSummaryDashboardApi;
  onToggleId: (key: MultiFilterKey, id: number, checked: boolean) => void;
  onParentChange: (id?: number) => void;
  onDateChange: (
    key:
      | 'createdFrom'
      | 'createdTo'
      | 'updatedFrom'
      | 'updatedTo'
      | 'dueDateFrom'
      | 'dueDateTo',
    value?: number
  ) => void;
  onClear: () => void;
}) {
  const [search, setSearch] = useState('');
  const visibleFilters = FILTER_NAMES.filter((name) =>
    name.toLowerCase().includes(search.trim().toLowerCase())
  );
  const options = data?.filterOptions;

  return (
    <PopoverContent align='start' className='w-[360px] p-0'>
      <div className='border-b p-3'>
        <div className='relative'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder='Search more filters'
            className='pl-9'
          />
        </div>
      </div>
      <ScrollArea className='h-[460px]'>
        <div className='space-y-4 p-3'>
          {visibleFilters.includes('Assignee') ? (
            <FilterSection title='Assignee'>
              {(options?.assignees || []).map((user) => (
                <OptionCheckbox
                  key={user.id}
                  checked={Boolean(filters.assigneeIds?.includes(user.id))}
                  label={user.displayName || `User ${user.id}`}
                  onCheckedChange={(checked) =>
                    onToggleId('assigneeIds', user.id, checked)
                  }
                />
              ))}
            </FilterSection>
          ) : null}

          {visibleFilters.includes('Created') ? (
            <DateRangeFilter
              title='Created'
              from={filters.createdFrom}
              to={filters.createdTo}
              onFromChange={(value) => onDateChange('createdFrom', value)}
              onToChange={(value) => onDateChange('createdTo', value)}
            />
          ) : null}

          {visibleFilters.includes('Due date') ? (
            <DateRangeFilter
              title='Due date'
              from={filters.dueDateFrom}
              to={filters.dueDateTo}
              onFromChange={(value) => onDateChange('dueDateFrom', value)}
              onToChange={(value) => onDateChange('dueDateTo', value)}
            />
          ) : null}

          {visibleFilters.includes('Parent') ? (
            <ParentFilter
              parents={options?.parents || []}
              selectedId={filters.parentId}
              onChange={onParentChange}
            />
          ) : null}

          {visibleFilters.includes('Priority') ? (
            <BreakdownFilter
              title='Priority'
              items={options?.priorities || []}
              selectedIds={filters.priorityIds}
              onToggle={(id, checked) => onToggleId('priorityIds', id, checked)}
            />
          ) : null}

          {visibleFilters.includes('Status') ? (
            <BreakdownFilter
              title='Status'
              items={options?.statuses || []}
              selectedIds={filters.statusIds}
              onToggle={(id, checked) => onToggleId('statusIds', id, checked)}
            />
          ) : null}

          {visibleFilters.includes('Updated') ? (
            <DateRangeFilter
              title='Updated'
              from={filters.updatedFrom}
              to={filters.updatedTo}
              onFromChange={(value) => onDateChange('updatedFrom', value)}
              onToChange={(value) => onDateChange('updatedTo', value)}
            />
          ) : null}

          {visibleFilters.includes('Work type') ? (
            <BreakdownFilter
              title='Work type'
              items={options?.issueTypes || []}
              selectedIds={filters.issueTypeIds}
              onToggle={(id, checked) =>
                onToggleId('issueTypeIds', id, checked)
              }
            />
          ) : null}
        </div>
      </ScrollArea>
      <div className='flex items-center justify-between border-t px-3 py-2 text-xs text-muted-foreground'>
        <span>
          {visibleFilters.length} of {FILTER_NAMES.length}
        </span>
        <Button variant='ghost' size='sm' onClick={onClear}>
          Clear all
        </Button>
      </div>
    </PopoverContent>
  );
}

function FilterSection({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <div className='space-y-2'>
      <div className='px-2 text-xs font-semibold text-muted-foreground'>
        {title}
      </div>
      <div className='space-y-1'>{children}</div>
    </div>
  );
}

function BreakdownFilter({
  title,
  items,
  selectedIds,
  onToggle,
}: {
  title: string;
  items: PMProjectSummaryBreakdownItemApi[];
  selectedIds?: number[];
  onToggle: (id: number, checked: boolean) => void;
}) {
  return (
    <FilterSection title={title}>
      {items.map((item) => (
        <OptionCheckbox
          key={item.id}
          checked={Boolean(selectedIds?.includes(item.id))}
          label={item.name}
          onCheckedChange={(checked) => onToggle(item.id, checked)}
        />
      ))}
    </FilterSection>
  );
}

function ParentFilter({
  parents,
  selectedId,
  onChange,
}: {
  parents: PMProjectSummaryParentOptionApi[];
  selectedId?: number;
  onChange: (id?: number) => void;
}) {
  return (
    <FilterSection title='Parent'>
      {parents.map((parent) => (
        <OptionCheckbox
          key={parent.id}
          checked={selectedId === parent.id}
          label={`${parent.key} ${parent.summary}`}
          onCheckedChange={(checked) =>
            onChange(checked ? parent.id : undefined)
          }
        />
      ))}
    </FilterSection>
  );
}

function DateRangeFilter({
  title,
  from,
  to,
  onFromChange,
  onToChange,
}: {
  title: string;
  from?: number;
  to?: number;
  onFromChange: (value?: number) => void;
  onToChange: (value?: number) => void;
}) {
  return (
    <FilterSection title={title}>
      <div className='grid grid-cols-2 gap-2 px-2'>
        <Input
          type='date'
          value={toDateInputValue(from)}
          onChange={(event) =>
            onFromChange(dateInputToEpoch(event.target.value))
          }
          aria-label={`${title} from`}
        />
        <Input
          type='date'
          value={toDateInputValue(to)}
          onChange={(event) =>
            onToChange(dateInputToEpoch(event.target.value, true))
          }
          aria-label={`${title} to`}
        />
      </div>
    </FilterSection>
  );
}

function StatusOverview({ data }: { data: PMProjectSummaryDashboardApi }) {
  const chartData = data.statusOverview.items.filter((item) => item.count > 0);
  const total = data.statusOverview.total;

  return (
    <Card className='min-h-[320px]'>
      <CardHeader className='pb-2'>
        <CardTitle className='text-base'>Status overview</CardTitle>
        <p className='text-sm text-muted-foreground'>
          Get a snapshot of the status of your work items.
        </p>
      </CardHeader>
      <CardContent>
        <div className='grid min-h-[230px] gap-4 md:grid-cols-[minmax(0,1fr)_190px]'>
          <div className='relative h-[230px]'>
            <ResponsiveContainer width='100%' height='100%'>
              <PieChart>
                <Pie
                  data={chartData as unknown as Record<string, unknown>[]}
                  dataKey='count'
                  nameKey='name'
                  innerRadius='62%'
                  outerRadius='86%'
                  strokeWidth={0}
                >
                  {chartData.map((entry, index) => (
                    <Cell
                      key={entry.id}
                      fill={STATUS_COLORS[index % STATUS_COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className='pointer-events-none absolute inset-0 flex flex-col items-center justify-center'>
              <div className='text-3xl font-semibold'>{total}</div>
              <div className='max-w-28 truncate text-sm text-muted-foreground'>
                Total work items
              </div>
            </div>
          </div>
          <div className='flex flex-col justify-center gap-3'>
            {data.statusOverview.items.map((item, index) => (
              <div key={item.id} className='flex items-center gap-2 text-sm'>
                <span
                  className='h-3 w-3 shrink-0 rounded-sm'
                  style={{
                    backgroundColor:
                      STATUS_COLORS[index % STATUS_COLORS.length],
                  }}
                />
                <span className='min-w-0 flex-1 truncate'>{item.name}</span>
                <span className='text-muted-foreground'>{item.count}</span>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function PriorityBreakdown({ data }: { data: PMProjectSummaryDashboardApi }) {
  return (
    <Card className='min-h-[320px]'>
      <CardHeader className='pb-2'>
        <CardTitle className='text-base'>Priority breakdown</CardTitle>
        <p className='text-sm text-muted-foreground'>
          Get a holistic view of how work is being prioritized.
        </p>
      </CardHeader>
      <CardContent>
        <div className='h-[230px]'>
          <ResponsiveContainer width='100%' height='100%'>
            <BarChart
              data={
                data.priorityBreakdown as unknown as Record<string, unknown>[]
              }
            >
              <CartesianGrid strokeDasharray='3 3' vertical={false} />
              <XAxis dataKey='name' tickLine={false} axisLine={false} />
              <YAxis allowDecimals={false} tickLine={false} axisLine={false} />
              <Tooltip />
              <Bar dataKey='count' radius={[4, 4, 0, 0]}>
                {data.priorityBreakdown.map((entry) => (
                  <Cell key={entry.id} fill={entry.color || '#8993A4'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}

function WorkTypeBreakdown({ data }: { data: PMProjectSummaryDashboardApi }) {
  const total = getTotal(data.workTypeBreakdown);

  return (
    <Card className='min-h-[320px]'>
      <CardHeader className='pb-2'>
        <CardTitle className='text-base'>Types of work</CardTitle>
        <p className='text-sm text-muted-foreground'>
          Get a breakdown of work items by their types.
        </p>
      </CardHeader>
      <CardContent>
        <div className='grid grid-cols-[160px_minmax(0,1fr)] gap-3 text-xs font-semibold text-muted-foreground'>
          <div>Type</div>
          <div>Distribution</div>
        </div>
        <div className='mt-3 space-y-4'>
          {data.workTypeBreakdown.map((item, index) => {
            const percent = getPercent(item.count, total);
            return (
              <div
                key={item.id}
                className='grid grid-cols-[160px_minmax(0,1fr)] items-center gap-3'
              >
                <div className='flex min-w-0 items-center gap-2 text-sm'>
                  <span
                    className='h-3 w-3 shrink-0 rounded-sm'
                    style={{
                      backgroundColor:
                        WORK_TYPE_COLORS[index % WORK_TYPE_COLORS.length],
                    }}
                  />
                  <span className='truncate'>{item.name}</span>
                </div>
                <div className='h-6 overflow-hidden rounded-sm bg-muted'>
                  <div
                    className='flex h-full min-w-0 items-center px-2 text-xs font-medium text-primary-foreground'
                    style={{
                      width: `${percent}%`,
                      backgroundColor:
                        WORK_TYPE_COLORS[index % WORK_TYPE_COLORS.length],
                    }}
                  >
                    {percent > 12 ? `${percent}%` : null}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}

function RecentActivity({ data }: { data: PMProjectSummaryDashboardApi }) {
  return (
    <Card className='min-h-[320px]'>
      <CardHeader className='pb-2'>
        <CardTitle className='text-base'>Recent activity</CardTitle>
        <p className='text-sm text-muted-foreground'>
          Stay up to date with what is happening across the space.
        </p>
      </CardHeader>
      <CardContent>
        <ScrollArea className='h-[230px] pr-3'>
          <div className='space-y-4'>
            {data.recentActivity.items.length ? (
              data.recentActivity.items.map((activity) => (
                <div key={activity.id} className='flex gap-3'>
                  <div className='flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary'>
                    {activity.actor?.displayName?.slice(0, 2).toUpperCase() ||
                      'PM'}
                  </div>
                  <div className='min-w-0 flex-1 space-y-1'>
                    <div className='flex flex-wrap items-center gap-1 text-sm'>
                      <span className='font-medium'>
                        {activity.actor?.displayName || 'Unknown user'}
                      </span>
                      <span className='text-muted-foreground'>
                        {activity.type === 'COMMENT'
                          ? 'commented on'
                          : `updated ${activity.fieldName || 'a field'} on`}
                      </span>
                      <span className='font-medium text-primary'>
                        {activity.workItemKey}: {activity.workItemSummary}
                      </span>
                    </div>
                    <div className='flex flex-wrap items-center gap-2 text-xs text-muted-foreground'>
                      {activity.type === 'COMMENT' ? (
                        <MessageSquare className='h-3.5 w-3.5' />
                      ) : (
                        <History className='h-3.5 w-3.5' />
                      )}
                      <span>{formatRelativeDate(activity.createdAt)}</span>
                      {activity.statusName ? (
                        <Badge variant='secondary' className='h-5 px-1.5'>
                          {activity.statusName}
                        </Badge>
                      ) : null}
                    </div>
                    {activity.body ? (
                      <div className='rounded-md border bg-muted/30 px-3 py-2 text-sm'>
                        {activity.body}
                      </div>
                    ) : activity.toValue || activity.fromValue ? (
                      <div className='text-sm text-muted-foreground'>
                        {activity.fromValue || 'Empty'} -&gt;{' '}
                        {activity.toValue || 'Empty'}
                      </div>
                    ) : null}
                  </div>
                </div>
              ))
            ) : (
              <div className='rounded-lg border border-dashed p-6 text-sm text-muted-foreground'>
                No recent activity for the current filters.
              </div>
            )}
          </div>
        </ScrollArea>
      </CardContent>
    </Card>
  );
}

function SummarySkeleton() {
  return (
    <div className='space-y-5'>
      <Skeleton className='h-9 w-28' />
      <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} className='h-16 rounded-lg' />
        ))}
      </div>
      <div className='grid gap-5 xl:grid-cols-2'>
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} className='h-80 rounded-lg' />
        ))}
      </div>
    </div>
  );
}

export function PMProjectSummaryPage({ projectId }: PMProjectSummaryPageProps) {
  const [filters, setFilters] = useState<FilterState>({});
  const numericProjectId = toNumber(projectId);
  const queryParams = useMemo(
    () => ({
      ...filters,
      activityPage: 0,
      activitySize: 20,
    }),
    [filters]
  );
  const { data, isLoading, isFetching, error } = useGetPmProjectSummaryQuery(
    {
      projectId: numericProjectId || 0,
      params: queryParams,
    },
    {
      skip: !numericProjectId,
    }
  );
  const activeFilterCount = getActiveFilterCount(filters);

  const toggleId = (key: MultiFilterKey, id: number, checked: boolean) => {
    setFilters((current) => {
      const existing = current[key] || [];
      const next = checked
        ? Array.from(new Set([...existing, id]))
        : existing.filter((value) => value !== id);
      return {
        ...current,
        [key]: next.length ? next : undefined,
      };
    });
  };

  const setDateFilter = (
    key:
      | 'createdFrom'
      | 'createdTo'
      | 'updatedFrom'
      | 'updatedTo'
      | 'dueDateFrom'
      | 'dueDateTo',
    value?: number
  ) => {
    setFilters((current) => ({
      ...current,
      [key]: value,
    }));
  };

  if (!numericProjectId) {
    return (
      <div className='rounded-lg border border-dashed p-6 text-sm text-muted-foreground'>
        Invalid project id.
      </div>
    );
  }

  if (isLoading) {
    return <SummarySkeleton />;
  }

  if (error || !data) {
    return (
      <div className='rounded-lg border border-dashed p-6 text-sm text-muted-foreground'>
        {error ? getErrorMessage(error) : 'Project summary is unavailable.'}
      </div>
    );
  }

  return (
    <div className='space-y-5'>
      <div className='flex items-center justify-between gap-3'>
        <Popover>
          <PopoverTrigger asChild>
            <Button
              variant={activeFilterCount ? 'default' : 'outline'}
              size='sm'
              className='gap-2'
            >
              <ListFilter className='h-4 w-4' />
              Filter
              {activeFilterCount ? (
                <Badge variant='secondary' className='h-5 px-1.5'>
                  {activeFilterCount}
                </Badge>
              ) : null}
            </Button>
          </PopoverTrigger>
          <FilterPanel
            filters={filters}
            data={data}
            onToggleId={toggleId}
            onParentChange={(id) =>
              setFilters((current) => ({ ...current, parentId: id }))
            }
            onDateChange={setDateFilter}
            onClear={() => setFilters({})}
          />
        </Popover>

        {isFetching ? (
          <div className='text-xs text-muted-foreground'>Refreshing...</div>
        ) : null}
      </div>

      {activeFilterCount ? (
        <div className='flex flex-wrap gap-2'>
          <Button variant='ghost' size='sm' onClick={() => setFilters({})}>
            <X className='mr-2 h-4 w-4' />
            Clear filters
          </Button>
        </div>
      ) : null}

      <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
        <MetricCard
          icon={CheckCircle2}
          value={data.metrics.completedLast7Days}
          title='completed'
          caption='in the last 7 days'
        />
        <MetricCard
          icon={Pencil}
          value={data.metrics.updatedLast7Days}
          title='updated'
          caption='in the last 7 days'
        />
        <MetricCard
          icon={PlusCircle}
          value={data.metrics.createdLast7Days}
          title='created'
          caption='in the last 7 days'
        />
        <MetricCard
          icon={CalendarClock}
          value={data.metrics.dueSoonNext7Days}
          title='due soon'
          caption='in the next 7 days'
        />
      </div>

      <div
        className={cn(
          'grid gap-5 xl:grid-cols-2',
          isFetching && 'opacity-80 transition-opacity'
        )}
      >
        <StatusOverview data={data} />
        <RecentActivity data={data} />
        <PriorityBreakdown data={data} />
        <WorkTypeBreakdown data={data} />
      </div>
    </div>
  );
}
