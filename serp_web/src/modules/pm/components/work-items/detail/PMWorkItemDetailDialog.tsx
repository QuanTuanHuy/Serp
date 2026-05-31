/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail dialog
 */

'use client';

import { useMemo, useState } from 'react';
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
  X,
  Zap,
} from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Tabs,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import type { ComboboxItem } from '@/shared/components/ui/combobox';
import { PMWorkItemSkillPanel } from '../../skills';
import { useGetPmProjectPeopleQuery } from '../../../api/projectApi';
import {
  useGetPmWorkItemActivitiesQuery,
  useGetPmWorkItemByIdQuery,
  useGetPmWorkItemChildrenQuery,
  useGetPmWorkItemCommentsQuery,
  useGetPmWorkItemCreateMetaQuery,
  useGetPmWorkItemTransitionsQuery,
  useGetPmWorkItemLinksQuery,
  useDeletePmWorkItemLinkMutation,
  useTransitionPmWorkItemStatusMutation,
  useUpdatePmWorkItemMutation,
} from '../../../api/workItemApi';
import type {
  PMUpdateWorkItemRequest,
  PMWorkItemActivityApi,
  PMWorkItemChildApi,
  PMWorkItemCommentApi,
  PMWorkItemLinkApi,
} from '../../../types/api';
import {
  ActivitiesList,
  CommentComposer,
  CommentsList,
} from './PMWorkItemActivity';
import {
  CollapsedPanel,
  DetailField,
  DetailSection,
  UserValue,
} from './PMWorkItemDetailPrimitives';
import { PMWorkItemDetailSkeleton } from './PMWorkItemDetailStates';
import { PMWorkItemScheduleSection } from './PMWorkItemScheduleSection';
import {
  InlineComboboxField,
  InlineDateField,
  InlineDescriptionEditor,
  InlineNumberField,
  InlineSummaryEditor,
} from './PMWorkItemInlineEditors';
import {
  WorkItemChildrenList,
  WorkItemLinksList,
} from './PMWorkItemRelationLists';
import { PMWorkItemSubtaskActions } from './PMWorkItemSubtaskActions';
import { PMWorkItemLinkActions } from './PMWorkItemLinkActions';
import { PMWorkItemWorklogPanel } from './PMWorkItemWorklogPanel';
import {
  formatRelativeTime,
  getActivityType,
  toDetailModel,
} from './pmWorkItemDetail.utils';
import type {
  ActivityTab,
  DetailQueryState,
  PMWorkItemDetailFallback,
  WorkItemDetailModel,
} from './pmWorkItemDetail.types';

export type { PMWorkItemDetailFallback } from './pmWorkItemDetail.types';

interface PMWorkItemDetailDialogProps {
  projectId: number;
  workItemId?: number;
  open: boolean;
  fallbackItem?: PMWorkItemDetailFallback;
  onOpenChange: (open: boolean) => void;
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
  const showComments = shouldFetch && activityTab === 'comments';
  const showHistory = shouldFetch && activityTab === 'history';

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
    { skip: !showComments }
  );
  const activitiesQuery = useGetPmWorkItemActivitiesQuery(
    {
      projectId,
      workItemId: workItemId ?? 0,
      page: 0,
      size: 20,
      type: getActivityType(activityTab),
    },
    { skip: !showHistory }
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
  const [deleteWorkItemLink, deleteWorkItemLinkState] =
    useDeletePmWorkItemLinkMutation();
  const [deletingLinkId, setDeletingLinkId] = useState<number | null>(null);

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

  const handleDeleteLink = async (linkId: number) => {
    if (!workItemId) return;

    setDeletingLinkId(linkId);
    try {
      await deleteWorkItemLink({
        projectId,
        workItemId,
        linkId,
      }).unwrap();
      toast.success('Link removed.');
    } catch (error) {
      toast.error('Failed to delete link', {
        description: getErrorMessage(error),
      });
    } finally {
      setDeletingLinkId(null);
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
          <div className='flex items-center justify-between gap-3'>
            <p className='text-sm text-muted-foreground'>
              Add child issues under this work item.
            </p>
            <PMWorkItemSubtaskActions
              projectId={projectId}
              workItemId={workItemId}
            />
          </div>
          <WorkItemChildrenList query={childrenQuery} />
        </DetailSection>

        <DetailSection
          title={`Linked work items${item.linkTotal !== undefined ? ` (${item.linkTotal})` : ''}`}
        >
          <PMWorkItemLinkActions
            projectId={projectId}
            workItemId={workItemId}
          />
          <WorkItemLinksList
            query={linksQuery}
            onDeleteLink={handleDeleteLink}
            deletingLinkId={
              deleteWorkItemLinkState.isLoading ? deletingLinkId : null
            }
          />
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
                <TabsTrigger value='comments'>Comments</TabsTrigger>
                <TabsTrigger value='history'>History</TabsTrigger>
                <TabsTrigger value='worklogs'>Work logs</TabsTrigger>
              </TabsList>
              <SlidersHorizontal className='h-4 w-4 text-muted-foreground' />
            </div>
            {activityTab === 'comments' ? (
              <>
                <CommentComposer
                  projectId={projectId}
                  workItemId={workItemId}
                  reporterName={item.reporterName}
                />
                <CommentsList
                  projectId={projectId}
                  workItemId={workItemId}
                  query={commentsQuery}
                />
              </>
            ) : activityTab === 'history' ? (
              <ActivitiesList query={activitiesQuery} />
            ) : (
              <PMWorkItemWorklogPanel
                projectId={projectId}
                workItemId={workItemId}
              />
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
  const [updateWorkItem, updateState] = useUpdatePmWorkItemMutation();
  const [transitionWorkItem, transitionState] =
    useTransitionPmWorkItemStatusMutation();
  const [detailPanelOpen, setDetailPanelOpen] = useState(true);

  const { data: projectPeople = [], isLoading: isUserLoading } =
    useGetPmProjectPeopleQuery(projectId, { skip: !workItemId });

  const { data: meta, isFetching: isMetaFetching } =
    useGetPmWorkItemCreateMetaQuery(
      {
        projectId,
        issueTypeId: item.issueTypeId ?? undefined,
      },
      { skip: !workItemId || !item.issueTypeId }
    );

  const { data: transitions = [], isFetching: isTransitionsFetching } =
    useGetPmWorkItemTransitionsQuery(
      { projectId, workItemId: workItemId ?? 0 },
      { skip: !workItemId }
    );

  const assigneeOptions = useMemo<ComboboxItem[]>(() => {
    const options = projectPeople
      .map((person) => ({
        value: person.userId,
        label: person.name || person.email || `User #${person.userId}`,
      }))
      .sort((left, right) => left.label.localeCompare(right.label));

    if (
      item.assigneeId &&
      item.assigneeName &&
      !options.some((option) => Number(option.value) === item.assigneeId)
    ) {
      return [{ value: item.assigneeId, label: item.assigneeName }, ...options];
    }

    return options;
  }, [item.assigneeId, item.assigneeName, projectPeople]);

  const priorities = meta?.priorities;
  const priorityOptions = useMemo<ComboboxItem[]>(() => {
    const options =
      priorities?.map((priority) => ({
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
  }, [item.priorityId, item.priorityName, priorities]);

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

  const handleTransition = async (transitionId: number) => {
    if (!workItemId) return;

    try {
      await transitionWorkItem({
        projectId,
        workItemId,
        body: { transitionId },
      }).unwrap();
      toast.success('Work item status updated.');
    } catch (error) {
      toast.error('Failed to update status', {
        description: getErrorMessage(error),
      });
    }
  };

  const isStatusUpdating =
    transitionState.isLoading || isTransitionsFetching || !workItemId;

  return (
    <aside className='min-h-0 overflow-y-auto border-t bg-muted/10 p-4 lg:border-l lg:border-t-0 lg:p-5'>
      <div className='mb-3 flex items-center gap-2'>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant='secondary'
              size='sm'
              className='gap-1 font-semibold'
              disabled={isStatusUpdating}
            >
              {transitionState.isLoading ? (
                <Loader2 className='h-4 w-4 animate-spin' />
              ) : null}
              {item.statusName ?? 'To Do'}
              <ChevronDown className='h-4 w-4' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='start' className='w-56'>
            <DropdownMenuLabel>Move to</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {transitions.length ? (
              transitions.map((transition) => (
                <DropdownMenuItem
                  key={transition.id}
                  className='flex items-center justify-between gap-3'
                  onSelect={() => handleTransition(transition.id)}
                >
                  <span className='min-w-0 truncate'>
                    {transition.targetStatus?.name ?? transition.name}
                  </span>
                  <span className='shrink-0 text-xs text-muted-foreground'>
                    {transition.name}
                  </span>
                </DropdownMenuItem>
              ))
            ) : (
              <DropdownMenuItem disabled>
                No transitions available
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
        <Button variant='outline' size='icon' className='h-8 w-8' disabled>
          <Zap className='h-4 w-4' />
        </Button>
      </div>

      <section className='rounded-lg border bg-background'>
        <div className='flex items-center justify-between gap-3 border-b px-4 py-3'>
          <button
            type='button'
            className='flex min-w-0 items-center gap-2 text-left font-semibold'
            aria-expanded={detailPanelOpen}
            onClick={() => setDetailPanelOpen((current) => !current)}
          >
            {detailPanelOpen ? (
              <ChevronDown className='h-4 w-4 shrink-0 text-muted-foreground' />
            ) : (
              <ChevronRight className='h-4 w-4 shrink-0 text-muted-foreground' />
            )}
            <span>Details</span>
          </button>
          <SlidersHorizontal className='h-4 w-4 text-muted-foreground' />
        </div>
        {detailPanelOpen ? (
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
        ) : null}
      </section>

      <PMWorkItemSkillPanel projectId={projectId} workItemId={workItemId} />
      <PMWorkItemScheduleSection item={item} />

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
