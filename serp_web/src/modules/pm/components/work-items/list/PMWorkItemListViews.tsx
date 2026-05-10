/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item list views
 */

'use client';

import { AlertCircle, Flag, RefreshCw, UserRound } from 'lucide-react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMWorkItemSearchApi } from '../../../types/api';
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
  dueDate?: number | null;
  updatedAt?: number | null;
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
                          {getWorkItemLabel(item)}
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
      <CardContent className='space-y-6 p-6'>
        <div className='flex flex-col gap-3 border-b pb-5 lg:flex-row lg:items-start lg:justify-between'>
          <div>
            <div className='flex items-center gap-2'>
              <span className='text-sm font-semibold text-primary'>
                {keyLabel}
              </span>
              {isFetching ? (
                <RefreshCw className='h-3.5 w-3.5 animate-spin text-muted-foreground' />
              ) : null}
            </div>
            <h2 className='mt-2 text-2xl font-semibold tracking-tight'>
              {title ?? 'Work item'}
            </h2>
          </div>
          <PMDetailStatus statusName={statusName} />
        </div>

        <section>
          <h3 className='text-sm font-semibold'>Description</h3>
          <p className='mt-2 whitespace-pre-wrap text-sm leading-6 text-muted-foreground'>
            {description}
          </p>
        </section>

        <div className='grid gap-4 md:grid-cols-2'>
          <PMDetailField label='Issue type' value={issueTypeName ?? 'None'} />
          <PMDetailField label='Priority' value={priorityName ?? 'None'} />
          <PMDetailField
            label='Assignee'
            value={assigneeName ?? 'Unassigned'}
          />
          <PMDetailField label='Reporter' value={reporterName ?? 'None'} />
          <PMDetailField label='Due date' value={formatDate(dueDate)} />
          <PMDetailField label='Updated' value={formatDate(updatedAt)} />
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
