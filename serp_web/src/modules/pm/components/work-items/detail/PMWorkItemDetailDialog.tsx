/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail dialog
 */

'use client';

import {
  Bolt,
  CheckSquare,
  ChevronDown,
  ChevronRight,
  Eye,
  Flag,
  GitBranch,
  Link2,
  Loader2,
  LockKeyhole,
  Maximize2,
  MoreHorizontal,
  Plus,
  Settings,
  Share2,
  SlidersHorizontal,
  UserRound,
  X,
  Zap,
} from 'lucide-react';
import { useState } from 'react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
  Skeleton,
  Tabs,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetPmWorkItemByIdQuery } from '../../../api';
import type {
  PMWorkItemBoardCardApi,
  PMWorkItemDetailApi,
  PMWorkItemSearchApi,
} from '../../../types/api';
import {
  formatDetailDate,
  formatRelativeTime,
  getInitials,
} from './pmWorkItemDetail.utils';

export type PMWorkItemDetailFallback =
  | PMWorkItemSearchApi
  | PMWorkItemBoardCardApi;

interface PMWorkItemDetailDialogProps {
  projectId: number;
  workItemId?: number;
  open: boolean;
  fallbackItem?: PMWorkItemDetailFallback;
  onOpenChange: (open: boolean) => void;
}

type WorkItemDetailModel = {
  key: string;
  summary: string;
  description?: string | null;
  statusName?: string | null;
  issueTypeName?: string | null;
  priorityName?: string | null;
  priorityColor?: string | null;
  assigneeName?: string | null;
  assigneeAvatarUrl?: string | null;
  reporterName?: string | null;
  reporterAvatarUrl?: string | null;
  parentId?: number | null;
  startDate?: number | string | null;
  dueDate?: number | string | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
};

function toDetailModel(
  workItemId: number | undefined,
  data?: PMWorkItemDetailApi,
  fallbackItem?: PMWorkItemDetailFallback
): WorkItemDetailModel {
  const searchItem =
    fallbackItem && 'statusName' in fallbackItem ? fallbackItem : undefined;
  const boardItem =
    fallbackItem && 'issueType' in fallbackItem ? fallbackItem : undefined;

  return {
    key: data?.key ?? fallbackItem?.key ?? (workItemId ? `#${workItemId}` : ''),
    summary: data?.summary ?? fallbackItem?.summary ?? 'Work item',
    description: data?.description ?? fallbackItem?.description,
    statusName: data?.status?.name ?? searchItem?.statusName,
    issueTypeName:
      data?.issueType?.name ??
      searchItem?.issueTypeName ??
      boardItem?.issueType?.name,
    priorityName:
      data?.priority?.name ??
      searchItem?.priorityName ??
      boardItem?.priority?.name,
    priorityColor:
      data?.priority?.color ??
      searchItem?.priorityColor ??
      boardItem?.priority?.color,
    assigneeName:
      data?.assignee?.displayName ??
      searchItem?.assigneeName ??
      boardItem?.assigneeName,
    assigneeAvatarUrl:
      searchItem?.assigneeAvatarUrl ?? boardItem?.assigneeAvatarUrl,
    reporterName: data?.reporter?.displayName ?? searchItem?.reporterName,
    reporterAvatarUrl: searchItem?.reporterAvatarUrl,
    parentId: data?.parentId ?? fallbackItem?.parentId,
    startDate: data?.startDate ?? fallbackItem?.startDate,
    dueDate: data?.dueDate ?? fallbackItem?.dueDate,
    createdAt: data?.createdAt ?? searchItem?.createdAt,
    updatedAt: data?.updatedAt ?? searchItem?.updatedAt,
  };
}

export function PMWorkItemDetailDialog({
  projectId,
  workItemId,
  open,
  fallbackItem,
  onOpenChange,
}: PMWorkItemDetailDialogProps) {
  const { data, error, isFetching, isLoading } = useGetPmWorkItemByIdQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !open || !workItemId }
  );
  const item = toDetailModel(workItemId, data, fallbackItem);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className='h-[min(860px,calc(100vh-6rem))] w-[calc(100vw-1rem)] !max-w-[1280px] gap-0 overflow-hidden p-0 sm:rounded-xl lg:w-[min(1280px,calc(100vw-2rem))]'
        showCloseButton={false}
      >
        <DialogTitle className='sr-only'>{item.summary}</DialogTitle>
        <DialogDescription className='sr-only'>
          Work item detail dialog
        </DialogDescription>

        {isLoading && !fallbackItem ? (
          <PMWorkItemDetailSkeleton />
        ) : error ? (
          <div className='p-6'>
            <Alert variant='destructive'>
              <AlertTitle>Detail unavailable</AlertTitle>
              <AlertDescription>{getErrorMessage(error)}</AlertDescription>
            </Alert>
          </div>
        ) : (
          <div className='flex h-full min-h-0 flex-col bg-background text-foreground'>
            <PMWorkItemDetailHeader
              item={item}
              isFetching={isFetching}
              onClose={() => onOpenChange(false)}
            />
            <div className='grid min-h-0 flex-1 overflow-hidden lg:grid-cols-[minmax(0,1fr)_400px] xl:grid-cols-[minmax(0,1fr)_432px]'>
              <PMWorkItemDetailMain item={item} />
              <PMWorkItemDetailSidebar item={item} />
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

function PMWorkItemDetailHeader({
  item,
  isFetching,
  onClose,
}: {
  item: WorkItemDetailModel;
  isFetching: boolean;
  onClose: () => void;
}) {
  return (
    <header className='flex h-16 shrink-0 items-center justify-between gap-3 border-b px-4 sm:px-6'>
      <div className='flex min-w-0 items-center gap-2 text-sm text-muted-foreground'>
        <Button variant='ghost' size='sm' className='gap-2 px-2'>
          <GitBranch className='h-4 w-4' />
          <span className='hidden sm:inline'>Add epic</span>
        </Button>
        <span>/</span>
        <span className='inline-flex min-w-0 items-center gap-2 font-medium text-foreground'>
          <CheckSquare className='h-4 w-4 text-primary' />
          <span className='truncate'>{item.key}</span>
        </span>
        {isFetching ? (
          <Loader2 className='h-3.5 w-3.5 animate-spin text-muted-foreground' />
        ) : null}
      </div>

      <div className='flex shrink-0 items-center gap-1'>
        <LockKeyhole className='mr-1 hidden h-4 w-4 text-violet-500 sm:block' />
        <Button variant='outline' size='sm' className='hidden gap-1 sm:flex'>
          <Eye className='h-4 w-4' />1
        </Button>
        <Button variant='ghost' size='icon' className='h-8 w-8'>
          <Share2 className='h-4 w-4' />
        </Button>
        <Button variant='ghost' size='icon' className='h-8 w-8'>
          <MoreHorizontal className='h-4 w-4' />
        </Button>
        <Button
          variant='ghost'
          size='icon'
          className='hidden h-8 w-8 sm:inline-flex'
        >
          <Maximize2 className='h-4 w-4' />
        </Button>
        <Button
          variant='ghost'
          size='icon'
          className='h-8 w-8'
          onClick={onClose}
        >
          <X className='h-4 w-4' />
        </Button>
      </div>
    </header>
  );
}

function PMWorkItemDetailMain({ item }: { item: WorkItemDetailModel }) {
  const [activityTab, setActivityTab] = useState('comments');

  return (
    <main className='min-h-0 overflow-y-auto px-5 py-6 sm:px-9'>
      <div className='mx-auto max-w-3xl space-y-8'>
        <section className='space-y-4'>
          <h1 className='text-2xl font-semibold tracking-tight sm:text-3xl'>
            {item.summary}
          </h1>
          <div className='flex items-center gap-2'>
            <Button variant='outline' size='icon' className='h-8 w-8'>
              <Plus className='h-4 w-4' />
            </Button>
            <Button variant='outline' size='icon' className='h-8 w-8'>
              <MoreHorizontal className='h-4 w-4' />
            </Button>
          </div>
        </section>

        <DetailSection title='Description'>
          {item.description ? (
            <p className='whitespace-pre-wrap text-sm leading-6 text-muted-foreground'>
              {item.description}
            </p>
          ) : (
            <button className='text-left text-sm text-muted-foreground hover:text-foreground'>
              Add a description...
            </button>
          )}
        </DetailSection>

        <DetailSection title='Subtasks'>
          <button className='text-left text-sm text-muted-foreground hover:text-foreground'>
            Add subtask
          </button>
        </DetailSection>

        <DetailSection title='Linked work items'>
          <button className='text-left text-sm text-muted-foreground hover:text-foreground'>
            Add linked work item
          </button>
        </DetailSection>

        <DetailSection title='Activity'>
          <Tabs
            value={activityTab}
            onValueChange={setActivityTab}
            className='space-y-4'
          >
            <div className='flex items-center justify-between gap-3'>
              <TabsList>
                <TabsTrigger value='all'>All</TabsTrigger>
                <TabsTrigger value='comments'>Comments</TabsTrigger>
                <TabsTrigger value='history'>History</TabsTrigger>
                <TabsTrigger value='worklog'>Work log</TabsTrigger>
              </TabsList>
              <SlidersHorizontal className='h-4 w-4 text-muted-foreground' />
            </div>
            <div className='flex gap-3'>
              <PMUserAvatar name={item.reporterName ?? 'User'} />
              <div className='flex-1 rounded-md border p-4'>
                <p className='text-sm text-muted-foreground'>
                  Add a comment...
                </p>
                <div className='mt-5 flex flex-wrap items-center gap-4 text-sm font-medium'>
                  <span>Looks good!</span>
                  <span>Need help?</span>
                  <span>This is blocked...</span>
                  <span>Can you clarify...?</span>
                  <ChevronRight className='h-4 w-4 text-muted-foreground' />
                </div>
              </div>
            </div>
            <p className='pl-12 text-xs text-muted-foreground'>
              Pro tip: press <kbd className='rounded border px-1'>M</kbd> to
              comment
            </p>
          </Tabs>
        </DetailSection>
      </div>
    </main>
  );
}

function PMWorkItemDetailSidebar({ item }: { item: WorkItemDetailModel }) {
  return (
    <aside className='min-h-0 overflow-y-auto border-t bg-muted/10 p-4 lg:border-l lg:border-t-0 lg:p-5'>
      <div className='mb-3 flex items-center gap-2'>
        <Button variant='secondary' size='sm' className='gap-1 font-semibold'>
          {item.statusName ?? 'To Do'}
          <ChevronDown className='h-4 w-4' />
        </Button>
        <Button variant='outline' size='icon' className='h-8 w-8'>
          <Zap className='h-4 w-4' />
        </Button>
      </div>

      <section className='rounded-lg border bg-background'>
        <div className='flex items-center justify-between border-b px-4 py-3'>
          <h2 className='flex items-center gap-2 font-semibold'>
            <ChevronDown className='h-4 w-4' /> Details
          </h2>
          <SlidersHorizontal className='h-4 w-4 text-muted-foreground' />
        </div>
        <div className='space-y-5 p-4'>
          <DetailField label='Assignee'>
            <div className='space-y-1'>
              <UserValue
                name={item.assigneeName ?? 'Unassigned'}
                avatarUrl={item.assigneeAvatarUrl}
              />
              {!item.assigneeName ? (
                <button className='text-sm font-medium text-primary'>
                  Assign to me
                </button>
              ) : null}
            </div>
          </DetailField>
          <DetailField label='Priority'>
            <span className='inline-flex items-center gap-2'>
              <Flag
                className='h-4 w-4'
                style={
                  item.priorityColor ? { color: item.priorityColor } : undefined
                }
              />
              {item.priorityName ?? 'None'}
            </span>
          </DetailField>
          <DetailField label='Parent'>
            {item.parentId ? `#${item.parentId}` : 'None'}
          </DetailField>
          <DetailField label='Due date'>
            {formatDetailDate(item.dueDate)}
          </DetailField>
          <DetailField label='Labels'>None</DetailField>
          <DetailField label='Team'>None</DetailField>
          <DetailField label='Start date'>
            {formatDetailDate(item.startDate)}
          </DetailField>
          <DetailField label='Reporter'>
            <UserValue
              name={item.reporterName ?? 'None'}
              avatarUrl={item.reporterAvatarUrl}
            />
          </DetailField>
        </div>
      </section>

      <CollapsedPanel
        title='Development'
        icon={<Link2 className='h-4 w-4' />}
      />
      <CollapsedPanel
        title='Automation'
        subtitle='Rule executions'
        icon={<Bolt className='h-4 w-4' />}
      />

      <div className='mt-3 flex items-start justify-between gap-4 px-3 text-xs text-muted-foreground'>
        <div className='space-y-1'>
          <p>Created {formatRelativeTime(item.createdAt)}</p>
          <p>Updated {formatRelativeTime(item.updatedAt)}</p>
        </div>
        <button className='inline-flex items-center gap-1 font-medium hover:text-foreground'>
          <Settings className='h-4 w-4' /> Configure
        </button>
      </div>
    </aside>
  );
}

function DetailSection({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section className='space-y-2'>
      <h2 className='text-base font-semibold'>{title}</h2>
      {children}
    </section>
  );
}

function DetailField({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div className='grid grid-cols-[132px_minmax(0,1fr)] gap-3 text-sm'>
      <dt className='font-medium text-muted-foreground'>{label}</dt>
      <dd className='min-w-0 text-foreground'>{children}</dd>
    </div>
  );
}

function UserValue({
  name,
  avatarUrl,
}: {
  name: string;
  avatarUrl?: string | null;
}) {
  return (
    <span className='inline-flex min-w-0 items-center gap-2'>
      <PMUserAvatar name={name} avatarUrl={avatarUrl} />
      <span className='truncate'>{name}</span>
    </span>
  );
}

function PMUserAvatar({
  name,
  avatarUrl,
}: {
  name: string;
  avatarUrl?: string | null;
}) {
  const empty = name === 'Unassigned' || name === 'None';
  return (
    <Avatar className='h-7 w-7 border'>
      {avatarUrl ? <AvatarImage src={avatarUrl} alt={name} /> : null}
      <AvatarFallback
        className={cn(
          'text-[10px] font-semibold',
          empty && 'text-muted-foreground'
        )}
      >
        {empty ? <UserRound className='h-3.5 w-3.5' /> : getInitials(name)}
      </AvatarFallback>
    </Avatar>
  );
}

function CollapsedPanel({
  title,
  subtitle,
  icon,
}: {
  title: string;
  subtitle?: string;
  icon: React.ReactNode;
}) {
  return (
    <button className='mt-3 flex w-full items-center justify-between rounded-lg border bg-background px-4 py-3 text-left hover:bg-muted/40'>
      <span className='inline-flex items-center gap-3 font-semibold'>
        <ChevronRight className='h-4 w-4 text-muted-foreground' />
        {title}
        <span className='text-muted-foreground'>{icon}</span>
        {subtitle ? (
          <span className='text-xs font-normal text-muted-foreground'>
            {subtitle}
          </span>
        ) : null}
      </span>
    </button>
  );
}

function PMWorkItemDetailSkeleton() {
  return (
    <div className='space-y-6 p-6'>
      <div className='flex items-center justify-between'>
        <Skeleton className='h-5 w-40' />
        <div className='flex gap-2'>
          <Skeleton className='h-8 w-8' />
          <Skeleton className='h-8 w-8' />
        </div>
      </div>
      <div className='grid gap-8 lg:grid-cols-[minmax(0,1fr)_320px] xl:grid-cols-[minmax(0,1fr)_360px]'>
        <div className='space-y-6'>
          <Skeleton className='h-9 w-3/4' />
          <Skeleton className='h-24 w-full' />
          <Skeleton className='h-20 w-full' />
        </div>
        <Skeleton className='h-96 w-full' />
      </div>
    </div>
  );
}
