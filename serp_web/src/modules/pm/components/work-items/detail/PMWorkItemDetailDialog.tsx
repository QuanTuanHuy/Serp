/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail dialog
 */

'use client';

import { useMemo, useState, type ReactNode } from 'react';
import {
  Bolt,
  CalendarDays,
  Check,
  CheckSquare,
  ChevronDown,
  ChevronRight,
  Clock3,
  Eye,
  Flag,
  GitBranch,
  Link2,
  Loader2,
  LockKeyhole,
  Maximize2,
  MessageSquare,
  MoreHorizontal,
  Pencil,
  Plus,
  Save,
  Settings,
  Share2,
  SlidersHorizontal,
  Trash2,
  UserRound,
  X,
  Zap,
} from 'lucide-react';
import { toast } from 'sonner';
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
  Input,
  Skeleton,
  Tabs,
  TabsList,
  TabsTrigger,
  Textarea,
} from '@/shared/components/ui';
import { Combobox, type ComboboxItem } from '@/shared/components/ui/combobox';
import { useAppSelector } from '@/shared/hooks';
import { cn } from '@/shared/utils';
import {
  useCreatePmWorkItemCommentMutation,
  useDeletePmWorkItemCommentMutation,
  useGetPmWorkItemActivitiesQuery,
  useGetPmWorkItemByIdQuery,
  useGetPmWorkItemChildrenQuery,
  useGetPmWorkItemCommentsQuery,
  useGetPmWorkItemCreateMetaQuery,
  useGetPmWorkItemLinksQuery,
  useUpdatePmWorkItemCommentMutation,
  useUpdatePmWorkItemMutation,
} from '../../../api';
import type {
  PMUpdateWorkItemRequest,
  PMWorkItemActivityApi,
  PMWorkItemBoardCardApi,
  PMWorkItemChildApi,
  PMWorkItemCommentApi,
  PMWorkItemDetailApi,
  PMWorkItemLinkApi,
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

type ActivityTab = 'all' | 'comments' | 'history';

type WorkItemDetailModel = {
  id?: number;
  key: string;
  summary: string;
  description?: string | null;
  statusName?: string | null;
  issueTypeId?: number | null;
  issueTypeName?: string | null;
  issueTypeIconUrl?: string | null;
  priorityId?: number | null;
  priorityName?: string | null;
  priorityColor?: string | null;
  assigneeId?: number | null;
  assigneeName?: string | null;
  assigneeAvatarUrl?: string | null;
  reporterName?: string | null;
  reporterAvatarUrl?: string | null;
  parentId?: number | null;
  parentKey?: string | null;
  parentSummary?: string | null;
  startDate?: number | string | null;
  dueDate?: number | string | null;
  timeOriginalEstimate?: number | null;
  timeRemainingEstimate?: number | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
  subtaskTotal?: number;
  subtaskDone?: number;
  linkTotal?: number;
  commentTotal?: number;
};

type DetailQueryState<T> = {
  data?: T;
  error?: unknown;
  isFetching: boolean;
  isLoading: boolean;
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
    id: data?.id ?? fallbackItem?.id,
    key: data?.key ?? fallbackItem?.key ?? (workItemId ? `#${workItemId}` : ''),
    summary: data?.summary ?? fallbackItem?.summary ?? 'Work item',
    description: data?.description ?? fallbackItem?.description,
    statusName: data?.status?.name ?? searchItem?.statusName,
    issueTypeId:
      data?.issueType?.id ??
      data?.issueTypeId ??
      searchItem?.issueTypeId ??
      boardItem?.issueType?.id,
    issueTypeName:
      data?.issueType?.name ??
      searchItem?.issueTypeName ??
      boardItem?.issueType?.name,
    issueTypeIconUrl:
      data?.issueType?.iconUrl ??
      searchItem?.issueTypeIconUrl ??
      boardItem?.issueType?.iconUrl,
    priorityId:
      data?.priority?.id ??
      data?.priorityId ??
      searchItem?.priorityId ??
      boardItem?.priority?.id,
    priorityName:
      data?.priority?.name ??
      searchItem?.priorityName ??
      boardItem?.priority?.name,
    priorityColor:
      data?.priority?.color ??
      searchItem?.priorityColor ??
      boardItem?.priority?.color,
    assigneeId:
      data?.assignee?.id ?? data?.assigneeId ?? fallbackItem?.assigneeId,
    assigneeName:
      data?.assignee?.displayName ??
      searchItem?.assigneeName ??
      boardItem?.assigneeName,
    assigneeAvatarUrl:
      data?.assignee?.avatarUrl ??
      searchItem?.assigneeAvatarUrl ??
      boardItem?.assigneeAvatarUrl,
    reporterName: data?.reporter?.displayName ?? searchItem?.reporterName,
    reporterAvatarUrl:
      data?.reporter?.avatarUrl ?? searchItem?.reporterAvatarUrl,
    parentId: data?.parent?.id ?? data?.parentId ?? fallbackItem?.parentId,
    parentKey: data?.parent?.key,
    parentSummary: data?.parent?.summary,
    startDate: data?.startDate ?? fallbackItem?.startDate,
    dueDate: data?.dueDate ?? fallbackItem?.dueDate,
    timeOriginalEstimate: data?.timeOriginalEstimate ?? null,
    timeRemainingEstimate: data?.timeRemainingEstimate ?? null,
    createdAt: data?.createdAt ?? searchItem?.createdAt,
    updatedAt: data?.updatedAt ?? searchItem?.updatedAt,
    subtaskTotal: data?.subtaskStats?.total,
    subtaskDone: data?.subtaskStats?.done,
    linkTotal: data?.linkStats?.total,
    commentTotal: data?.commentStats?.total,
  };
}

function getActivityType(tab: ActivityTab): 'ALL' | 'COMMENT' | 'HISTORY' {
  if (tab === 'comments') return 'COMMENT';
  if (tab === 'history') return 'HISTORY';
  return 'ALL';
}

export function PMWorkItemDetailDialog({
  projectId,
  workItemId,
  open,
  fallbackItem,
  onOpenChange,
}: PMWorkItemDetailDialogProps) {
  const [activityTab, setActivityTab] = useState<ActivityTab>('comments');
  const shouldFetch = open && Boolean(workItemId);

  const detailQuery = useGetPmWorkItemByIdQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !shouldFetch }
  );
  const childrenQuery = useGetPmWorkItemChildrenQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !shouldFetch }
  );
  const linksQuery = useGetPmWorkItemLinksQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !shouldFetch }
  );
  const commentsQuery = useGetPmWorkItemCommentsQuery(
    { projectId, workItemId: workItemId ?? 0, page: 0, size: 20 },
    { skip: !shouldFetch }
  );
  const activitiesQuery = useGetPmWorkItemActivitiesQuery(
    {
      projectId,
      workItemId: workItemId ?? 0,
      page: 0,
      size: 20,
      type: getActivityType(activityTab),
    },
    { skip: !shouldFetch }
  );

  const item = toDetailModel(workItemId, detailQuery.data, fallbackItem);

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

        {detailQuery.isLoading && !fallbackItem ? (
          <PMWorkItemDetailSkeleton />
        ) : detailQuery.error ? (
          <div className='p-6'>
            <Alert variant='destructive'>
              <AlertTitle>Detail unavailable</AlertTitle>
              <AlertDescription>
                {getErrorMessage(detailQuery.error)}
              </AlertDescription>
            </Alert>
          </div>
        ) : (
          <div className='flex h-full min-h-0 flex-col bg-background text-foreground'>
            <PMWorkItemDetailHeader
              item={item}
              isFetching={detailQuery.isFetching}
              onClose={() => onOpenChange(false)}
            />
            <div className='grid min-h-0 flex-1 overflow-hidden lg:grid-cols-[minmax(0,1fr)_400px] xl:grid-cols-[minmax(0,1fr)_432px]'>
              <PMWorkItemDetailMain
                projectId={projectId}
                workItemId={workItemId}
                item={item}
                activityTab={activityTab}
                activitiesQuery={activitiesQuery}
                childrenQuery={childrenQuery}
                commentsQuery={commentsQuery}
                linksQuery={linksQuery}
                onActivityTabChange={setActivityTab}
              />
              <PMWorkItemDetailSidebar
                projectId={projectId}
                workItemId={workItemId}
                item={item}
              />
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
        <Button variant='ghost' size='sm' className='gap-2 px-2' disabled>
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

function PMWorkItemDetailMain({
  projectId,
  workItemId,
  item,
  activityTab,
  activitiesQuery,
  childrenQuery,
  commentsQuery,
  linksQuery,
  onActivityTabChange,
}: {
  projectId: number;
  workItemId?: number;
  item: WorkItemDetailModel;
  activityTab: ActivityTab;
  activitiesQuery: DetailQueryState<{
    data: { items: PMWorkItemActivityApi[] };
  }>;
  childrenQuery: DetailQueryState<PMWorkItemChildApi[]>;
  commentsQuery: DetailQueryState<{ data: { items: PMWorkItemCommentApi[] } }>;
  linksQuery: DetailQueryState<PMWorkItemLinkApi[]>;
  onActivityTabChange: (value: ActivityTab) => void;
}) {
  const [updateWorkItem, updateState] = useUpdatePmWorkItemMutation();

  const handleUpdate = async (body: PMUpdateWorkItemRequest) => {
    if (!workItemId) return;

    try {
      await updateWorkItem({ projectId, workItemId, body }).unwrap();
      toast.success('Work item updated.');
    } catch (error) {
      toast.error('Failed to update work item', {
        description: getErrorMessage(error),
      });
      throw error;
    }
  };

  return (
    <main className='min-h-0 overflow-y-auto px-5 py-6 sm:px-9'>
      <div className='mx-auto max-w-3xl space-y-8'>
        <section className='space-y-4'>
          <InlineSummaryEditor
            value={item.summary}
            disabled={updateState.isLoading}
            onSave={(summary) => handleUpdate({ summary })}
          />
          <div className='flex items-center gap-2'>
            <Button variant='outline' size='icon' className='h-8 w-8' disabled>
              <Plus className='h-4 w-4' />
            </Button>
            <Button variant='outline' size='icon' className='h-8 w-8'>
              <MoreHorizontal className='h-4 w-4' />
            </Button>
          </div>
        </section>

        <DetailSection title='Description'>
          <InlineDescriptionEditor
            value={item.description}
            disabled={updateState.isLoading}
            onSave={(description) => handleUpdate({ description })}
          />
        </DetailSection>

        <DetailSection
          title={`Subtasks${item.subtaskTotal !== undefined ? ` (${item.subtaskDone ?? 0}/${item.subtaskTotal})` : ''}`}
        >
          <WorkItemChildrenList query={childrenQuery} />
        </DetailSection>

        <DetailSection
          title={`Linked work items${item.linkTotal !== undefined ? ` (${item.linkTotal})` : ''}`}
        >
          <WorkItemLinksList query={linksQuery} />
        </DetailSection>

        <DetailSection
          title={`Activity${item.commentTotal !== undefined ? ` (${item.commentTotal} comments)` : ''}`}
        >
          <Tabs
            value={activityTab}
            onValueChange={(value) => onActivityTabChange(value as ActivityTab)}
            className='space-y-4'
          >
            <div className='flex items-center justify-between gap-3'>
              <TabsList>
                <TabsTrigger value='all'>All</TabsTrigger>
                <TabsTrigger value='comments'>Comments</TabsTrigger>
                <TabsTrigger value='history'>History</TabsTrigger>
              </TabsList>
              <SlidersHorizontal className='h-4 w-4 text-muted-foreground' />
            </div>
            <CommentComposer
              projectId={projectId}
              workItemId={workItemId}
              reporterName={item.reporterName}
            />
            {activityTab === 'comments' ? (
              <CommentsList
                projectId={projectId}
                workItemId={workItemId}
                query={commentsQuery}
              />
            ) : (
              <ActivitiesList query={activitiesQuery} />
            )}
          </Tabs>
        </DetailSection>
      </div>
    </main>
  );
}

function PMWorkItemDetailSidebar({
  projectId,
  workItemId,
  item,
}: {
  projectId: number;
  workItemId?: number;
  item: WorkItemDetailModel;
}) {
  const organizationId = useAppSelector(selectOrganizationId);
  const [updateWorkItem, updateState] = useUpdatePmWorkItemMutation();

  const { data: usersResponse, isLoading: isUserLoading } =
    useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
      },
      { skip: !organizationId || !workItemId }
    );

  const { data: meta, isFetching: isMetaFetching } =
    useGetPmWorkItemCreateMetaQuery(
      {
        projectId,
        issueTypeId: item.issueTypeId ?? undefined,
      },
      { skip: !workItemId || !item.issueTypeId }
    );

  const assigneeOptions = useMemo<ComboboxItem[]>(() => {
    const options =
      usersResponse?.data.items.map((user) => {
        const name =
          `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
          user.email ||
          `User #${user.id}`;
        return { value: user.id, label: name };
      }) || [];

    if (
      item.assigneeId &&
      item.assigneeName &&
      !options.some((option) => Number(option.value) === item.assigneeId)
    ) {
      return [{ value: item.assigneeId, label: item.assigneeName }, ...options];
    }

    return options;
  }, [item.assigneeId, item.assigneeName, usersResponse]);

  const priorityOptions = useMemo<ComboboxItem[]>(() => {
    const options =
      meta?.priorities.map((priority) => ({
        value: priority.id,
        label: priority.name,
      })) || [];

    if (
      item.priorityId &&
      item.priorityName &&
      !options.some((option) => Number(option.value) === item.priorityId)
    ) {
      return [{ value: item.priorityId, label: item.priorityName }, ...options];
    }

    return options;
  }, [item.priorityId, item.priorityName, meta]);

  const handleUpdate = async (body: PMUpdateWorkItemRequest) => {
    if (!workItemId) return;

    try {
      await updateWorkItem({ projectId, workItemId, body }).unwrap();
      toast.success('Work item updated.');
    } catch (error) {
      toast.error('Failed to update work item', {
        description: getErrorMessage(error),
      });
      throw error;
    }
  };

  return (
    <aside className='min-h-0 overflow-y-auto border-t bg-muted/10 p-4 lg:border-l lg:border-t-0 lg:p-5'>
      <div className='mb-3 flex items-center gap-2'>
        <Button
          variant='secondary'
          size='sm'
          className='gap-1 font-semibold'
          disabled
        >
          {item.statusName ?? 'To Do'}
          <ChevronDown className='h-4 w-4' />
        </Button>
        <Button variant='outline' size='icon' className='h-8 w-8' disabled>
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
            <InlineComboboxField
              value={item.assigneeId}
              display={
                <UserValue
                  name={item.assigneeName ?? 'Unassigned'}
                  avatarUrl={item.assigneeAvatarUrl}
                />
              }
              items={assigneeOptions}
              placeholder='Unassigned'
              loading={isUserLoading}
              disabled={updateState.isLoading}
              onSave={(assigneeId) => handleUpdate({ assigneeId })}
            />
          </DetailField>
          <DetailField label='Priority'>
            <InlineComboboxField
              value={item.priorityId}
              display={
                <span className='inline-flex items-center gap-2'>
                  <Flag
                    className='h-4 w-4'
                    style={
                      item.priorityColor
                        ? { color: item.priorityColor }
                        : undefined
                    }
                  />
                  {item.priorityName ?? 'None'}
                </span>
              }
              items={priorityOptions}
              placeholder='None'
              loading={isMetaFetching}
              disabled={updateState.isLoading}
              onSave={(priorityId) => handleUpdate({ priorityId })}
            />
          </DetailField>
          <DetailField label='Parent'>
            {item.parentId ? (
              <span className='inline-flex min-w-0 flex-col'>
                <span className='font-medium'>
                  {item.parentKey ?? `#${item.parentId}`}
                </span>
                {item.parentSummary ? (
                  <span className='truncate text-xs text-muted-foreground'>
                    {item.parentSummary}
                  </span>
                ) : null}
              </span>
            ) : (
              'None'
            )}
          </DetailField>
          <DetailField label='Due date'>
            <InlineDateField
              value={item.dueDate}
              disabled={updateState.isLoading}
              onSave={(dueDate) => handleUpdate({ dueDate })}
            />
          </DetailField>
          <DetailField label='Labels'>None</DetailField>
          <DetailField label='Team'>None</DetailField>
          <DetailField label='Start date'>
            <InlineDateField
              value={item.startDate}
              disabled={updateState.isLoading}
              onSave={(startDate) => handleUpdate({ startDate })}
            />
          </DetailField>
          <DetailField label='Original estimate'>
            <InlineNumberField
              value={item.timeOriginalEstimate}
              disabled={updateState.isLoading}
              onSave={(timeOriginalEstimate) =>
                handleUpdate({ timeOriginalEstimate })
              }
            />
          </DetailField>
          <DetailField label='Remaining'>
            <InlineNumberField
              value={item.timeRemainingEstimate}
              disabled={updateState.isLoading}
              onSave={(timeRemainingEstimate) =>
                handleUpdate({ timeRemainingEstimate })
              }
            />
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

function InlineSummaryEditor({
  value,
  disabled,
  onSave,
}: {
  value: string;
  disabled?: boolean;
  onSave: (value: string) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(value);

  if (!editing) {
    return (
      <button
        type='button'
        className='group flex w-full min-w-0 items-start gap-2 text-left'
        onClick={() => {
          setDraft(value);
          setEditing(true);
        }}
      >
        <h1 className='min-w-0 flex-1 break-words text-2xl font-semibold tracking-tight sm:text-3xl'>
          {value}
        </h1>
        <Pencil className='mt-2 h-4 w-4 shrink-0 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100' />
      </button>
    );
  }

  const save = async () => {
    const nextValue = draft.trim();
    if (!nextValue) {
      toast.error('Summary is required.');
      return;
    }
    await onSave(nextValue);
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Textarea
        value={draft}
        rows={2}
        className='text-2xl font-semibold sm:text-3xl'
        onChange={(event) => setDraft(event.target.value)}
      />
      <InlineEditorActions
        disabled={disabled}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

function InlineDescriptionEditor({
  value,
  disabled,
  onSave,
}: {
  value?: string | null;
  disabled?: boolean;
  onSave: (value: string | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(value ?? '');

  if (!editing) {
    return (
      <button
        type='button'
        className='w-full text-left text-sm text-muted-foreground hover:text-foreground'
        onClick={() => {
          setDraft(value ?? '');
          setEditing(true);
        }}
      >
        {value ? (
          <span className='whitespace-pre-wrap leading-6'>{value}</span>
        ) : (
          'Add a description...'
        )}
      </button>
    );
  }

  const save = async () => {
    await onSave(draft.trim() || null);
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Textarea
        value={draft}
        rows={8}
        placeholder='Add context, scope, acceptance notes, or delivery details.'
        onChange={(event) => setDraft(event.target.value)}
      />
      <InlineEditorActions
        disabled={disabled}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

function InlineComboboxField({
  value,
  display,
  items,
  placeholder,
  loading,
  disabled,
  onSave,
}: {
  value?: number | null;
  display: ReactNode;
  items: ComboboxItem[];
  placeholder: string;
  loading?: boolean;
  disabled?: boolean;
  onSave: (value: number | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState<string | number | undefined>(
    value ?? undefined
  );

  if (!editing) {
    return (
      <button
        type='button'
        className='group inline-flex min-w-0 items-center gap-2 text-left hover:text-primary'
        onClick={() => {
          setDraft(value ?? undefined);
          setEditing(true);
        }}
      >
        {display}
        <Pencil className='h-3.5 w-3.5 shrink-0 opacity-0 group-hover:opacity-100' />
      </button>
    );
  }

  const save = async () => {
    await onSave(draft === undefined ? null : Number(draft));
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Combobox
        value={draft}
        onChange={setDraft}
        items={items}
        placeholder={placeholder}
        emptyText='No options found'
        loading={loading}
      />
      <InlineEditorActions
        disabled={disabled || loading}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

function InlineDateField({
  value,
  disabled,
  onSave,
}: {
  value?: number | string | null;
  disabled?: boolean;
  onSave: (value: number | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(toDateInputValue(value));

  if (!editing) {
    return (
      <button
        type='button'
        className='group inline-flex items-center gap-2 text-left hover:text-primary'
        onClick={() => {
          setDraft(toDateInputValue(value));
          setEditing(true);
        }}
      >
        <CalendarDays className='h-4 w-4 text-muted-foreground' />
        {formatDetailDate(value)}
        <Pencil className='h-3.5 w-3.5 opacity-0 group-hover:opacity-100' />
      </button>
    );
  }

  const save = async () => {
    await onSave(draft ? new Date(`${draft}T00:00:00`).getTime() : null);
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Input
        type='date'
        value={draft}
        onChange={(event) => setDraft(event.target.value)}
      />
      <InlineEditorActions
        disabled={disabled}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

function InlineNumberField({
  value,
  disabled,
  onSave,
}: {
  value?: number | null;
  disabled?: boolean;
  onSave: (value: number | null) => Promise<void>;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(
    value === null || value === undefined ? '' : String(value)
  );

  if (!editing) {
    return (
      <button
        type='button'
        className='group inline-flex items-center gap-2 text-left hover:text-primary'
        onClick={() => {
          setDraft(value === null || value === undefined ? '' : String(value));
          setEditing(true);
        }}
      >
        <Clock3 className='h-4 w-4 text-muted-foreground' />
        {value === null || value === undefined ? 'None' : `${value} min`}
        <Pencil className='h-3.5 w-3.5 opacity-0 group-hover:opacity-100' />
      </button>
    );
  }

  const save = async () => {
    const trimmed = draft.trim();
    if (!trimmed) {
      await onSave(null);
      setEditing(false);
      return;
    }
    const nextValue = Number(trimmed);
    if (!Number.isFinite(nextValue) || nextValue < 0) {
      toast.error('Estimate must be zero or greater.');
      return;
    }
    await onSave(nextValue);
    setEditing(false);
  };

  return (
    <div className='space-y-2'>
      <Input
        type='number'
        min='0'
        step='1'
        value={draft}
        placeholder='Minutes'
        onChange={(event) => setDraft(event.target.value)}
      />
      <InlineEditorActions
        disabled={disabled}
        onCancel={() => setEditing(false)}
        onSave={save}
      />
    </div>
  );
}

function InlineEditorActions({
  disabled,
  onCancel,
  onSave,
}: {
  disabled?: boolean;
  onCancel: () => void;
  onSave: () => void;
}) {
  return (
    <div className='flex items-center gap-2'>
      <Button
        size='sm'
        className='h-8 gap-1'
        disabled={disabled}
        onClick={onSave}
      >
        <Save className='h-3.5 w-3.5' />
        Save
      </Button>
      <Button
        size='sm'
        variant='ghost'
        className='h-8'
        disabled={disabled}
        onClick={onCancel}
      >
        Cancel
      </Button>
    </div>
  );
}

function WorkItemChildrenList({
  query,
}: {
  query: DetailQueryState<PMWorkItemChildApi[]>;
}) {
  if (query.isLoading) return <ListSkeleton rows={3} />;
  if (query.error) return <InlineError error={query.error} />;

  const children = query.data ?? [];
  if (children.length === 0) {
    return (
      <div className='rounded-md border border-dashed p-4 text-sm text-muted-foreground'>
        No subtasks yet.
      </div>
    );
  }

  return (
    <div className='space-y-2'>
      {children.map((child) => (
        <div
          key={child.id}
          className='flex items-start justify-between gap-3 rounded-md border p-3'
        >
          <div className='min-w-0 space-y-1'>
            <div className='flex min-w-0 items-center gap-2'>
              <CheckSquare className='h-4 w-4 shrink-0 text-primary' />
              <span className='shrink-0 text-xs font-semibold text-primary'>
                {child.key}
              </span>
              <span className='truncate text-sm font-medium'>
                {child.summary}
              </span>
            </div>
            <div className='flex flex-wrap items-center gap-2 text-xs text-muted-foreground'>
              <span>{child.issueType?.name ?? 'Work item'}</span>
              <span>{child.assignee?.displayName ?? 'Unassigned'}</span>
            </div>
          </div>
          <div className='flex shrink-0 flex-col items-end gap-2'>
            <Badge variant='secondary'>{child.status?.name ?? 'Status'}</Badge>
            <PriorityValue priority={child.priority} />
          </div>
        </div>
      ))}
    </div>
  );
}

function WorkItemLinksList({
  query,
}: {
  query: DetailQueryState<PMWorkItemLinkApi[]>;
}) {
  if (query.isLoading) return <ListSkeleton rows={2} />;
  if (query.error) return <InlineError error={query.error} />;

  const links = query.data ?? [];
  if (links.length === 0) {
    return (
      <div className='rounded-md border border-dashed p-4 text-sm text-muted-foreground'>
        No linked work items yet.
      </div>
    );
  }

  return (
    <div className='space-y-2'>
      {links.map((link) => (
        <div key={link.id} className='rounded-md border p-3'>
          <div className='mb-2 flex items-center gap-2 text-xs text-muted-foreground'>
            <Link2 className='h-3.5 w-3.5' />
            <span>
              {link.linkType?.description ?? link.linkType?.name ?? 'Linked'}
            </span>
            <Badge variant='outline'>{link.direction}</Badge>
          </div>
          <div className='flex items-start justify-between gap-3'>
            <div className='min-w-0'>
              <p className='text-xs font-semibold text-primary'>
                {link.workItem?.key ?? `#${link.workItem?.id ?? link.id}`}
              </p>
              <p className='truncate text-sm font-medium'>
                {link.workItem?.summary ?? 'Linked work item'}
              </p>
            </div>
            <div className='flex shrink-0 flex-col items-end gap-2'>
              <Badge variant='secondary'>
                {link.workItem?.status?.name ?? 'Status'}
              </Badge>
              <PriorityValue priority={link.workItem?.priority} />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

function CommentComposer({
  projectId,
  workItemId,
  reporterName,
}: {
  projectId: number;
  workItemId?: number;
  reporterName?: string | null;
}) {
  const [body, setBody] = useState('');
  const [createComment, createState] = useCreatePmWorkItemCommentMutation();

  const handleSubmit = async () => {
    const nextBody = body.trim();
    if (!workItemId || !nextBody) return;

    try {
      await createComment({ projectId, workItemId, body: nextBody }).unwrap();
      setBody('');
      toast.success('Comment added.');
    } catch (error) {
      toast.error('Failed to add comment', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='flex gap-3'>
      <PMUserAvatar name={reporterName ?? 'User'} />
      <div className='flex-1 space-y-2 rounded-md border p-3'>
        <Textarea
          value={body}
          rows={3}
          placeholder='Add a comment...'
          onChange={(event) => setBody(event.target.value)}
        />
        <div className='flex justify-end'>
          <Button
            size='sm'
            className='gap-2'
            disabled={!body.trim() || createState.isLoading}
            onClick={handleSubmit}
          >
            {createState.isLoading ? (
              <Loader2 className='h-4 w-4 animate-spin' />
            ) : (
              <MessageSquare className='h-4 w-4' />
            )}
            Comment
          </Button>
        </div>
      </div>
    </div>
  );
}

function CommentsList({
  projectId,
  workItemId,
  query,
}: {
  projectId: number;
  workItemId?: number;
  query: DetailQueryState<{ data: { items: PMWorkItemCommentApi[] } }>;
}) {
  if (query.isLoading) return <ListSkeleton rows={3} />;
  if (query.error) return <InlineError error={query.error} />;

  const comments = query.data?.data.items ?? [];
  if (comments.length === 0) {
    return (
      <p className='pl-12 text-sm text-muted-foreground'>No comments yet.</p>
    );
  }

  return (
    <div className='space-y-3'>
      {comments.map((comment) => (
        <CommentItem
          key={comment.id}
          projectId={projectId}
          workItemId={workItemId}
          comment={comment}
        />
      ))}
    </div>
  );
}

function CommentItem({
  projectId,
  workItemId,
  comment,
}: {
  projectId: number;
  workItemId?: number;
  comment: PMWorkItemCommentApi;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(comment.body);
  const [updateComment, updateState] = useUpdatePmWorkItemCommentMutation();
  const [deleteComment, deleteState] = useDeletePmWorkItemCommentMutation();

  const authorName =
    comment.author?.displayName ?? `User #${comment.author?.id ?? ''}`;

  const handleUpdate = async () => {
    const nextBody = draft.trim();
    if (!workItemId || !nextBody) return;

    try {
      await updateComment({
        projectId,
        workItemId,
        commentId: comment.id,
        body: nextBody,
      }).unwrap();
      setEditing(false);
      toast.success('Comment updated.');
    } catch (error) {
      toast.error('Failed to update comment', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDelete = async () => {
    if (!workItemId) return;

    try {
      await deleteComment({
        projectId,
        workItemId,
        commentId: comment.id,
      }).unwrap();
      toast.success('Comment deleted.');
    } catch (error) {
      toast.error('Failed to delete comment', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='flex gap-3'>
      <PMUserAvatar name={authorName} avatarUrl={comment.author?.avatarUrl} />
      <div className='min-w-0 flex-1 rounded-md border p-3'>
        <div className='mb-2 flex flex-wrap items-center justify-between gap-2'>
          <div className='min-w-0'>
            <p className='truncate text-sm font-semibold'>{authorName}</p>
            <p className='text-xs text-muted-foreground'>
              {formatRelativeTime(comment.createdAt)}
              {comment.edited ? ' · edited' : ''}
            </p>
          </div>
          <div className='flex shrink-0 items-center gap-1'>
            <Button
              variant='ghost'
              size='icon'
              className='h-7 w-7'
              onClick={() => {
                setDraft(comment.body);
                setEditing(true);
              }}
            >
              <Pencil className='h-3.5 w-3.5' />
            </Button>
            <Button
              variant='ghost'
              size='icon'
              className='h-7 w-7 text-destructive hover:text-destructive'
              disabled={deleteState.isLoading}
              onClick={handleDelete}
            >
              <Trash2 className='h-3.5 w-3.5' />
            </Button>
          </div>
        </div>
        {editing ? (
          <div className='space-y-2'>
            <Textarea
              value={draft}
              rows={4}
              onChange={(event) => setDraft(event.target.value)}
            />
            <InlineEditorActions
              disabled={updateState.isLoading}
              onCancel={() => setEditing(false)}
              onSave={handleUpdate}
            />
          </div>
        ) : (
          <p className='whitespace-pre-wrap text-sm leading-6 text-muted-foreground'>
            {comment.body}
          </p>
        )}
      </div>
    </div>
  );
}

function ActivitiesList({
  query,
}: {
  query: DetailQueryState<{ data: { items: PMWorkItemActivityApi[] } }>;
}) {
  if (query.isLoading) return <ListSkeleton rows={3} />;
  if (query.error) return <InlineError error={query.error} />;

  const activities = query.data?.data.items ?? [];
  if (activities.length === 0) {
    return (
      <p className='pl-12 text-sm text-muted-foreground'>No activity yet.</p>
    );
  }

  return (
    <div className='space-y-3'>
      {activities.map((activity) => (
        <div key={activity.id} className='flex gap-3'>
          <PMUserAvatar
            name={
              activity.actor?.displayName ?? `User #${activity.actor?.id ?? ''}`
            }
            avatarUrl={activity.actor?.avatarUrl}
          />
          <div className='min-w-0 flex-1 rounded-md border p-3'>
            <div className='mb-1 flex flex-wrap items-center gap-2'>
              <span className='text-sm font-semibold'>
                {activity.actor?.displayName ?? 'Unknown user'}
              </span>
              <Badge variant='outline'>{activity.type}</Badge>
              <span className='text-xs text-muted-foreground'>
                {formatRelativeTime(activity.createdAt)}
              </span>
            </div>
            {activity.type === 'COMMENT' ? (
              <p className='whitespace-pre-wrap text-sm leading-6 text-muted-foreground'>
                {activity.body}
              </p>
            ) : (
              <p className='text-sm text-muted-foreground'>
                Changed{' '}
                <span className='font-medium text-foreground'>
                  {activity.fieldName ?? activity.fieldKey ?? 'field'}
                </span>{' '}
                from{' '}
                <span className='font-medium text-foreground'>
                  {activity.fromValue || 'empty'}
                </span>{' '}
                to{' '}
                <span className='font-medium text-foreground'>
                  {activity.toValue || 'empty'}
                </span>
              </p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

function DetailSection({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
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
  children: ReactNode;
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

function PriorityValue({
  priority,
}: {
  priority?: { name?: string | null; color?: string | null } | null;
}) {
  return (
    <span className='inline-flex items-center gap-1.5 text-xs text-muted-foreground'>
      <Flag
        className='h-3.5 w-3.5'
        style={priority?.color ? { color: priority.color } : undefined}
      />
      {priority?.name ?? 'None'}
    </span>
  );
}

function CollapsedPanel({
  title,
  subtitle,
  icon,
}: {
  title: string;
  subtitle?: string;
  icon: ReactNode;
}) {
  return (
    <button className='mt-3 flex w-full items-center justify-between rounded-lg border bg-background px-4 py-3 text-left hover:bg-muted/40'>
      <span className='inline-flex min-w-0 items-center gap-3 font-semibold'>
        <ChevronRight className='h-4 w-4 shrink-0 text-muted-foreground' />
        <span className='truncate'>{title}</span>
        <span className='shrink-0 text-muted-foreground'>{icon}</span>
        {subtitle ? (
          <span className='truncate text-xs font-normal text-muted-foreground'>
            {subtitle}
          </span>
        ) : null}
      </span>
    </button>
  );
}

function InlineError({ error }: { error: unknown }) {
  return (
    <Alert variant='destructive'>
      <AlertTitle>Unable to load data</AlertTitle>
      <AlertDescription>{getErrorMessage(error)}</AlertDescription>
    </Alert>
  );
}

function ListSkeleton({ rows }: { rows: number }) {
  return (
    <div className='space-y-2'>
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} className='h-16 w-full' />
      ))}
    </div>
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

function toDateInputValue(value?: number | string | null): string {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toISOString().slice(0, 10);
}
