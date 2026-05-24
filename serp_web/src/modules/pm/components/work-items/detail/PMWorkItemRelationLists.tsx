/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item relation lists
 */

import { CheckSquare, Link2 } from 'lucide-react';
import { Badge } from '@/shared/components/ui';
import type { PMWorkItemChildApi, PMWorkItemLinkApi } from '../../../types/api';
import { PriorityValue } from './PMWorkItemDetailPrimitives';
import { InlineError, ListSkeleton } from './PMWorkItemDetailStates';
import type { DetailQueryState } from './pmWorkItemDetail.types';

export function WorkItemChildrenList({
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

export function WorkItemLinksList({
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
