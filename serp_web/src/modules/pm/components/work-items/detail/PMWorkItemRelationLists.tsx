/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item relation lists
 */

import type { KeyboardEvent } from 'react';
import { useRouter } from 'next/navigation';
import { CheckSquare, Loader2, Trash2 } from 'lucide-react';
import { Badge, Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMWorkItemChildApi, PMWorkItemLinkApi } from '../../../types/api';
import { PriorityValue } from './PMWorkItemDetailPrimitives';
import { InlineError, ListSkeleton } from './PMWorkItemDetailStates';
import type { DetailQueryState } from './pmWorkItemDetail.types';

export function WorkItemChildrenList({
  projectId,
  query,
}: {
  projectId: number;
  query: DetailQueryState<PMWorkItemChildApi[]>;
}) {
  const router = useRouter();

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

  const navigateToWorkItem = (childProjectId: number, workItemId: number) => {
    router.push(`/pm/projects/${childProjectId}/work-items/${workItemId}`);
  };

  const handleRowKeyDown = (
    event: KeyboardEvent<HTMLDivElement>,
    childProjectId: number,
    workItemId: number
  ) => {
    if (event.key !== 'Enter' && event.key !== ' ') return;

    event.preventDefault();
    navigateToWorkItem(childProjectId, workItemId);
  };

  return (
    <div className='space-y-2'>
      {children.map((child) => {
        const childProjectId = child.projectId ?? projectId;

        return (
          <div
            key={child.id}
            role='link'
            tabIndex={0}
            className='group flex cursor-pointer items-start justify-between gap-3 rounded-md border p-3 hover:bg-muted/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'
            onClick={() => navigateToWorkItem(childProjectId, child.id)}
            onKeyDown={(event) =>
              handleRowKeyDown(event, childProjectId, child.id)
            }
          >
            <div className='min-w-0 space-y-1'>
              <div className='flex min-w-0 items-center gap-2'>
                <CheckSquare className='h-4 w-4 shrink-0 text-primary' />
                <span className='shrink-0 text-xs font-semibold text-primary underline-offset-4 group-hover:underline'>
                  {child.key}
                </span>
                <span className='truncate text-sm font-medium underline-offset-4 group-hover:underline'>
                  {child.summary}
                </span>
              </div>
              <div className='flex flex-wrap items-center gap-2 text-xs text-muted-foreground'>
                <span>{child.issueType?.name ?? 'Work item'}</span>
                <span>{child.assignee?.displayName ?? 'Unassigned'}</span>
              </div>
            </div>
            <div className='flex shrink-0 flex-col items-end gap-2'>
              <Badge variant='secondary'>
                {child.status?.name ?? 'Status'}
              </Badge>
              <PriorityValue priority={child.priority} />
            </div>
          </div>
        );
      })}
    </div>
  );
}

type WorkItemLinkGroup = {
  label: string;
  links: PMWorkItemLinkApi[];
};

function getWorkItemLinkLabel(link: PMWorkItemLinkApi): string {
  return (
    link.linkType?.description?.trim() ||
    link.linkType?.name?.trim() ||
    'Linked'
  );
}

function groupWorkItemLinks(links: PMWorkItemLinkApi[]): WorkItemLinkGroup[] {
  const groups = new Map<string, PMWorkItemLinkApi[]>();

  for (const link of links) {
    const label = getWorkItemLinkLabel(link);
    const current = groups.get(label) ?? [];
    current.push(link);
    groups.set(label, current);
  }

  return Array.from(groups.entries()).map(([label, groupedLinks]) => ({
    label,
    links: groupedLinks,
  }));
}

export function WorkItemLinksList({
  projectId,
  query,
  onDeleteLink,
  deletingLinkId,
}: {
  projectId: number;
  query: DetailQueryState<PMWorkItemLinkApi[]>;
  onDeleteLink?: (linkId: number) => void;
  deletingLinkId?: number | null;
}) {
  const router = useRouter();

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

  const groups = groupWorkItemLinks(links);

  const navigateToWorkItem = (linkedProjectId: number, workItemId: number) => {
    router.push(`/pm/projects/${linkedProjectId}/work-items/${workItemId}`);
  };

  const handleRowKeyDown = (
    event: KeyboardEvent<HTMLDivElement>,
    linkedProjectId: number,
    workItemId?: number
  ) => {
    if (!workItemId) return;
    if (event.key !== 'Enter' && event.key !== ' ') return;

    event.preventDefault();
    navigateToWorkItem(linkedProjectId, workItemId);
  };

  return (
    <div className='space-y-4'>
      {groups.map((group) => (
        <div key={group.label} className='space-y-2'>
          <h3 className='text-sm font-semibold text-muted-foreground'>
            {group.label}
          </h3>
          <div className='space-y-2'>
            {group.links.map((link) => {
              const linkedWorkItemId = link.workItem?.id;
              const linkedProjectId = link.workItem?.projectId ?? projectId;
              const canNavigate = Boolean(linkedWorkItemId);

              return (
                <div
                  key={link.id}
                  role={canNavigate ? 'link' : undefined}
                  tabIndex={canNavigate ? 0 : undefined}
                  className={cn(
                    'group flex w-full items-center justify-between gap-3 rounded-md border bg-card px-3 py-2 text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
                    canNavigate && 'cursor-pointer hover:bg-muted/40'
                  )}
                  onClick={() => {
                    if (!linkedWorkItemId) return;
                    navigateToWorkItem(linkedProjectId, linkedWorkItemId);
                  }}
                  onKeyDown={(event) =>
                    handleRowKeyDown(event, linkedProjectId, linkedWorkItemId)
                  }
                >
                  <span className='flex min-w-0 items-center gap-2'>
                    <CheckSquare className='h-4 w-4 shrink-0 text-primary' />
                    <span
                      className={cn(
                        'shrink-0 text-sm font-semibold text-primary underline-offset-4',
                        canNavigate && 'group-hover:underline'
                      )}
                    >
                      {link.workItem?.key ?? `#${linkedWorkItemId ?? link.id}`}
                    </span>
                    <span
                      className={cn(
                        'truncate text-sm font-medium underline-offset-4',
                        canNavigate && 'group-hover:underline'
                      )}
                    >
                      {link.workItem?.summary ?? 'Linked work item'}
                    </span>
                  </span>
                  <span className='flex shrink-0 items-center gap-2'>
                    <Badge variant='secondary'>
                      {link.workItem?.status?.name ?? 'Status'}
                    </Badge>
                    <PriorityValue priority={link.workItem?.priority} />
                    {onDeleteLink ? (
                      <Button
                        type='button'
                        variant='ghost'
                        size='icon'
                        className='h-7 w-7'
                        onClick={(event) => {
                          event.preventDefault();
                          event.stopPropagation();
                          onDeleteLink(link.id);
                        }}
                        disabled={deletingLinkId === link.id}
                      >
                        {deletingLinkId === link.id ? (
                          <Loader2 className='h-3.5 w-3.5 animate-spin' />
                        ) : (
                          <Trash2 className='h-3.5 w-3.5' />
                        )}
                      </Button>
                    ) : null}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
}
