/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board card
 */

import { CalendarDays, Flag, GitBranch, UserRound } from 'lucide-react';
import { Badge, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMWorkItemBoardCardApi } from '../../../types/api';

interface PMWorkItemBoardCardProps {
  item: PMWorkItemBoardCardApi;
}

function formatBoardDate(value?: string | number | null): string | null {
  if (!value) return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
  });
}

export function PMWorkItemBoardCard({ item }: PMWorkItemBoardCardProps) {
  const dueDate = formatBoardDate(item.dueDate);
  const priorityColor = item.priority?.color || undefined;

  return (
    <Card className='group border-border/70 bg-background shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-md'>
      <CardContent className='space-y-3 p-3'>
        <div className='flex items-start justify-between gap-3'>
          <span className='text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
            {item.key}
          </span>
          {item.issueType?.name ? (
            <Badge
              variant='secondary'
              className='max-w-24 truncate px-2 py-0 text-[10px]'
            >
              {item.issueType.name}
            </Badge>
          ) : null}
        </div>

        <p className='line-clamp-3 text-sm font-medium leading-5 text-foreground'>
          {item.summary}
        </p>

        <div className='flex flex-wrap items-center gap-2 text-xs text-muted-foreground'>
          {item.parentId ? (
            <span className='inline-flex items-center gap-1 rounded-full bg-muted px-2 py-1'>
              <GitBranch className='h-3 w-3' />
              Parent
            </span>
          ) : null}
          {dueDate ? (
            <span className='inline-flex items-center gap-1 rounded-full bg-muted px-2 py-1'>
              <CalendarDays className='h-3 w-3' />
              {dueDate}
            </span>
          ) : null}
        </div>

        <div className='flex items-center justify-between gap-3 border-t pt-3'>
          <div className='flex min-w-0 items-center gap-2'>
            <span
              className={cn(
                'inline-flex h-5 w-5 items-center justify-center rounded-full bg-muted text-[10px] font-semibold text-muted-foreground',
                item.assigneeId && 'bg-primary/10 text-primary'
              )}
              title={
                item.assigneeId ? `Assignee ${item.assigneeId}` : 'Unassigned'
              }
            >
              {item.assigneeId ? (
                String(item.assigneeId).slice(-2)
              ) : (
                <UserRound className='h-3 w-3' />
              )}
            </span>
            <span className='truncate text-xs text-muted-foreground'>
              {item.assigneeId ? `Assignee ${item.assigneeId}` : 'Unassigned'}
            </span>
          </div>

          {item.priority?.name ? (
            <span className='inline-flex items-center gap-1 text-xs text-muted-foreground'>
              <Flag
                className='h-3.5 w-3.5'
                style={priorityColor ? { color: priorityColor } : undefined}
              />
              <span className='max-w-16 truncate'>{item.priority.name}</span>
            </span>
          ) : null}
        </div>
      </CardContent>
    </Card>
  );
}
