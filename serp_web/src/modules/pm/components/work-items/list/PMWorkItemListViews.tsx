/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item list views
 */

'use client';

import {
  AlertCircle,
  CheckSquare,
  Flag,
  MoreHorizontal,
  RefreshCw,
  UserRound,
  Zap,
} from 'lucide-react';
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
  Checkbox,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import type { ComboboxItem } from '@/shared/components/ui/combobox';
import { cn } from '@/shared/utils';
import type {
  PMWorkItemSearchApi,
  PMWorkItemTransitionApi,
} from '../../../types/api';
import {
  InlineEditorShell,
  WorkItemListComboboxEditor,
  WorkItemListDateEditor,
  WorkItemListStatusEditor,
  WorkItemListSummaryEditor,
  type WorkItemListTransitionLoader,
} from './PMWorkItemListInlineEditors';
import {
  formatDate,
  getInitials,
  getWorkItemLabel,
} from './pmWorkItemList.utils';

interface WorkItemListProps {
  items: PMWorkItemSearchApi[];
  loading: boolean;
  selectedIssueId?: number;
  totalItems: number;
  onSelect: (issueId: number) => void;
  selectedIds?: number[];
  onToggleSelect?: (issueId: number) => void;
  onToggleSelectAll?: (checked: boolean) => void;
}

interface WorkItemListTableProps extends WorkItemListProps {
  assigneeOptions: ComboboxItem[];
  priorityOptions: ComboboxItem[];
  resolutionLabels: Map<number, string>;
  isAssigneeLoading: boolean;
  isPriorityLoading: boolean;
  isUpdating: boolean;
  isTransitioning: boolean;
  onUpdateSummary: (
    item: PMWorkItemSearchApi,
    summary: string
  ) => Promise<void>;
  onUpdateAssignee: (
    item: PMWorkItemSearchApi,
    assigneeId: number | null
  ) => Promise<void>;
  onUpdatePriority: (
    item: PMWorkItemSearchApi,
    priorityId: number | null
  ) => Promise<void>;
  onUpdateDueDate: (
    item: PMWorkItemSearchApi,
    dueDate: number | null
  ) => Promise<void>;
  onLoadTransitions: WorkItemListTransitionLoader;
  onUpdateStatus: (
    item: PMWorkItemSearchApi,
    transition: PMWorkItemTransitionApi
  ) => Promise<void>;
}

interface PMWorkItemDetailPanelProps {
  title?: string;
  keyLabel: string;
  description: string;
  statusName?: string;
  issueTypeName?: string;
  priorityName?: string;
  assigneeName?: string;
  reporterName?: string;
  dueDate?: number | string | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
  isFetching?: boolean;
  loading?: boolean;
  errorMessage?: string;
}

export function PMWorkItemListTable({
  items,
  loading,
  selectedIssueId,
  totalItems,
  onSelect,
  selectedIds = [],
  onToggleSelect,
  onToggleSelectAll,
  assigneeOptions,
  priorityOptions,
  resolutionLabels,
  isAssigneeLoading,
  isPriorityLoading,
  isUpdating,
  isTransitioning,
  onUpdateSummary,
  onUpdateAssignee,
  onUpdatePriority,
  onUpdateDueDate,
  onLoadTransitions,
  onUpdateStatus,
}: WorkItemListTableProps) {
  const visibleSelectedCount = items.filter((item) =>
    selectedIds.includes(item.id)
  ).length;
  const allVisibleSelected =
    items.length > 0 && visibleSelectedCount === items.length;
  const someVisibleSelected =
    visibleSelectedCount > 0 && visibleSelectedCount < items.length;

  return (
    <div className='overflow-hidden rounded-md border bg-background'>
      <div className='flex h-9 items-center justify-between border-b bg-muted/30 px-3 text-xs text-muted-foreground'>
        <span className='font-medium'>{totalItems} work items</span>
        <span>Sorted by rank</span>
      </div>
      {loading ? <PMWorkItemListSkeleton /> : null}
      {!loading && items.length === 0 ? <PMWorkItemListEmpty /> : null}
      {!loading && items.length > 0 ? (
        <div className='overflow-x-auto'>
          <Table className='min-w-[1500px] table-fixed'>
            <TableHeader className='sticky top-0 z-10 bg-muted/50'>
              <TableRow className='hover:bg-transparent'>
                <TableHead className='w-10 border-r px-3'>
                  <Checkbox
                    aria-label='Select all work items'
                    checked={
                      allVisibleSelected
                        ? true
                        : someVisibleSelected
                          ? 'indeterminate'
                          : false
                    }
                    onCheckedChange={(checked) =>
                      onToggleSelectAll?.(checked === true)
                    }
                  />
                </TableHead>
                <TableHead className='w-[410px] border-r px-3'>Work</TableHead>
                <TableHead className='w-44 border-r px-3'>Assignee</TableHead>
                <TableHead className='w-44 border-r px-3'>Reporter</TableHead>
                <TableHead className='w-36 border-r px-3'>Priority</TableHead>
                <TableHead className='w-40 border-r px-3'>Status</TableHead>
                <TableHead className='w-32 border-r px-3'>Resolution</TableHead>
                <TableHead className='w-44 border-r px-3'>Created</TableHead>
                <TableHead className='w-44 border-r px-3'>Updated</TableHead>
                <TableHead className='w-36 border-r px-3'>Due date</TableHead>
                <TableHead className='w-12 px-2 text-center'>
                  <MoreHorizontal className='mx-auto h-4 w-4 text-muted-foreground' />
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {items.map((item) => (
                <TableRow
                  key={item.id}
                  onClick={() => onSelect(item.id)}
                  className={cn(
                    'h-10 cursor-pointer hover:bg-muted/40',
                    selectedIssueId === item.id &&
                      'bg-primary/5 hover:bg-primary/10'
                  )}
                >
                  <TableCell className='border-r px-3 py-1.5'>
                    <InlineEditorShell>
                      <Checkbox
                        aria-label={`Select ${item.key}`}
                        checked={selectedIds.includes(item.id)}
                        onClick={(event) => event.stopPropagation()}
                        onCheckedChange={() => onToggleSelect?.(item.id)}
                      />
                    </InlineEditorShell>
                  </TableCell>
                  <TableCell className='overflow-hidden border-r px-3 py-1.5'>
                    <div className='flex min-w-0 items-center gap-2'>
                      <PMIssueTypeIcon item={item} />
                      <button
                        type='button'
                        className='shrink-0 text-sm font-medium text-primary hover:underline'
                        onClick={(event) => event.stopPropagation()}
                      >
                        {item.key}
                      </button>
                      <InlineEditorShell className='min-w-0 flex-1'>
                        <WorkItemListSummaryEditor
                          item={item}
                          disabled={isUpdating}
                          onSave={onUpdateSummary}
                        />
                      </InlineEditorShell>
                    </div>
                  </TableCell>
                  <TableCell className='overflow-hidden border-r px-3 py-1.5'>
                    <InlineEditorShell className='min-w-0'>
                      <WorkItemListComboboxEditor
                        value={item.assigneeId}
                        currentLabel={item.assigneeName}
                        display={<PMUserValue item={item} />}
                        options={assigneeOptions}
                        placeholder='Unassigned'
                        loading={isAssigneeLoading}
                        disabled={isUpdating}
                        onSave={(assigneeId) =>
                          onUpdateAssignee(item, assigneeId)
                        }
                      />
                    </InlineEditorShell>
                  </TableCell>
                  <TableCell className='overflow-hidden border-r px-3 py-1.5'>
                    <PMUserCell item={item} kind='reporter' compact />
                  </TableCell>
                  <TableCell className='overflow-hidden border-r px-3 py-1.5'>
                    <InlineEditorShell className='min-w-0'>
                      <WorkItemListComboboxEditor
                        value={item.priorityId}
                        currentLabel={item.priorityName}
                        display={<PMPriorityCell item={item} />}
                        options={priorityOptions}
                        placeholder='None'
                        loading={isPriorityLoading}
                        disabled={isUpdating}
                        onSave={(priorityId) =>
                          onUpdatePriority(item, priorityId)
                        }
                      />
                    </InlineEditorShell>
                  </TableCell>
                  <TableCell className='overflow-hidden border-r px-3 py-1.5'>
                    <InlineEditorShell className='min-w-0'>
                      <WorkItemListStatusEditor
                        item={item}
                        loading={isTransitioning}
                        disabled={isUpdating}
                        onLoadTransitions={onLoadTransitions}
                        onSave={onUpdateStatus}
                      />
                    </InlineEditorShell>
                  </TableCell>
                  <TableCell className='border-r px-3 py-1.5 text-sm text-muted-foreground'>
                    {item.resolutionId
                      ? (resolutionLabels.get(item.resolutionId) ??
                        `Resolution ${item.resolutionId}`)
                      : 'Unresolved'}
                  </TableCell>
                  <TableCell className='border-r px-3 py-1.5 text-sm text-muted-foreground'>
                    {formatDate(item.createdAt)}
                  </TableCell>
                  <TableCell className='border-r px-3 py-1.5 text-sm text-muted-foreground'>
                    {formatDate(item.updatedAt)}
                  </TableCell>
                  <TableCell className='overflow-hidden border-r px-3 py-1.5'>
                    <InlineEditorShell className='min-w-0'>
                      <WorkItemListDateEditor
                        value={item.dueDate}
                        disabled={isUpdating}
                        onSave={(dueDate) => onUpdateDueDate(item, dueDate)}
                      />
                    </InlineEditorShell>
                  </TableCell>
                  <TableCell className='px-2 py-1.5'>
                    <InlineEditorShell>
                      <WorkItemRowActions onOpen={() => onSelect(item.id)} />
                    </InlineEditorShell>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      ) : null}
    </div>
  );
}

export function PMWorkItemCompactList({
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
                  selectedIssueId === item.id &&
                    'border-primary bg-primary/5 shadow-sm'
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

export function PMWorkItemDetailPanel({
  title,
  keyLabel,
  description,
  statusName,
  issueTypeName,
  priorityName,
  assigneeName,
  reporterName,
  dueDate,
  createdAt,
  updatedAt,
  isFetching,
  loading,
  errorMessage,
}: PMWorkItemDetailPanelProps) {
  if (loading) {
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

  if (errorMessage) {
    return (
      <Alert variant='destructive'>
        <AlertCircle className='h-4 w-4' />
        <AlertTitle>Detail unavailable</AlertTitle>
        <AlertDescription>{errorMessage}</AlertDescription>
      </Alert>
    );
  }

  return (
    <Card className='min-h-96 shadow-sm'>
      <CardContent className='p-0'>
        <div className='grid gap-0 lg:grid-cols-[minmax(0,1fr)_320px]'>
          <div className='space-y-6 p-6'>
            <div className='flex flex-col gap-3 border-b pb-5'>
              <div className='flex items-center gap-2'>
                <span className='text-sm font-semibold text-primary'>
                  {keyLabel}
                </span>
                {isFetching ? (
                  <RefreshCw className='h-3.5 w-3.5 animate-spin text-muted-foreground' />
                ) : null}
              </div>
              <div className='flex flex-col gap-3 xl:flex-row xl:items-start xl:justify-between'>
                <h2 className='text-2xl font-semibold tracking-tight'>
                  {title ?? 'Work item'}
                </h2>
                <PMDetailStatus statusName={statusName} />
              </div>
            </div>

            <section>
              <h3 className='text-sm font-semibold'>Description</h3>
              <p className='mt-2 whitespace-pre-wrap text-sm leading-6 text-muted-foreground'>
                {description}
              </p>
            </section>

            <section className='space-y-3'>
              <h3 className='text-sm font-semibold'>Related work</h3>
              <div className='rounded-lg border border-dashed bg-muted/20 p-4 text-sm text-muted-foreground'>
                Child items and linked work will appear here when available.
              </div>
            </section>
          </div>

          <aside className='border-t bg-muted/20 lg:sticky lg:top-0 lg:h-fit lg:border-l lg:border-t-0'>
            <div className='space-y-3 p-4'>
              <PMDetailField
                label='Status'
                value={statusName ?? 'Unknown status'}
              />
              <PMDetailField
                label='Issue type'
                value={issueTypeName ?? 'None'}
              />
              <PMDetailField label='Priority' value={priorityName ?? 'None'} />
              <PMDetailField
                label='Assignee'
                value={assigneeName ?? 'Unassigned'}
              />
              <PMDetailField label='Reporter' value={reporterName ?? 'None'} />
              <PMDetailField label='Created' value={formatDate(createdAt)} />
              <PMDetailField label='Due date' value={formatDate(dueDate)} />
              <PMDetailField label='Updated' value={formatDate(updatedAt)} />
            </div>
          </aside>
        </div>
      </CardContent>
    </Card>
  );
}

function PMStatusBadge({ item }: { item: PMWorkItemSearchApi }) {
  return (
    <Badge variant='secondary' className='max-w-28 truncate'>
      {item.statusName ?? `Status ${item.statusId}`}
    </Badge>
  );
}

function PMIssueTypeIcon({ item }: { item: PMWorkItemSearchApi }) {
  return (
    <CheckSquare
      className='h-4 w-4 shrink-0 text-blue-500'
      aria-label={item.issueTypeName ?? 'Work item'}
    />
  );
}

function PMUserValue({ item }: { item: PMWorkItemSearchApi }) {
  return (
    <span className='inline-flex max-w-full min-w-0 items-center gap-2 text-sm'>
      <Avatar className='h-5 w-5'>
        {item.assigneeAvatarUrl ? (
          <AvatarImage src={item.assigneeAvatarUrl} alt='' />
        ) : null}
        <AvatarFallback className='text-[10px]'>
          {item.assigneeName ? (
            getInitials(item.assigneeName)
          ) : (
            <UserRound className='h-3 w-3' />
          )}
        </AvatarFallback>
      </Avatar>
      <span className='min-w-0 flex-1 truncate'>
        {item.assigneeName ?? 'Unassigned'}
      </span>
    </span>
  );
}

function WorkItemRowActions({ onOpen }: { onOpen: () => void }) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          type='button'
          variant='ghost'
          size='icon'
          className='h-7 w-7'
          aria-label='Work item actions'
        >
          <MoreHorizontal className='h-4 w-4' />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end'>
        <DropdownMenuItem onSelect={() => onOpen()}>
          <Zap className='h-4 w-4' />
          Open details
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

function PMUserCell({
  item,
  compact = false,
  kind = 'assignee',
}: {
  item: PMWorkItemSearchApi;
  compact?: boolean;
  kind?: 'assignee' | 'reporter';
}) {
  const isAssignee = kind === 'assignee';
  const name = isAssignee
    ? (item.assigneeName ?? 'Unassigned')
    : (item.reporterName ?? 'None');
  const avatarUrl = isAssignee
    ? item.assigneeAvatarUrl
    : item.reporterAvatarUrl;
  return (
    <div className='flex min-w-0 items-center gap-2'>
      <Avatar className='h-6 w-6'>
        {avatarUrl ? <AvatarImage src={avatarUrl} alt={name} /> : null}
        <AvatarFallback className='text-[10px]'>
          {name !== 'Unassigned' && name !== 'None' ? (
            getInitials(name)
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
    <span className='inline-flex max-w-full min-w-0 items-center gap-1.5 text-sm text-muted-foreground'>
      <Flag
        className='h-3.5 w-3.5 shrink-0'
        style={item.priorityColor ? { color: item.priorityColor } : undefined}
      />
      <span className='min-w-0 truncate'>{item.priorityName ?? 'None'}</span>
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
    <div className='rounded-lg border bg-background p-3'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {label}
      </p>
      <p className='mt-1.5 text-sm font-medium'>{value}</p>
    </div>
  );
}

function PMWorkItemListEmpty() {
  return (
    <div className='flex min-h-64 flex-col items-center justify-center gap-2 p-8 text-center text-muted-foreground'>
      <AlertCircle className='h-8 w-8' />
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
