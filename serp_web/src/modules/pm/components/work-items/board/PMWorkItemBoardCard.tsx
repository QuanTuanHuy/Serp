/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board card
 */

import {
  AlertTriangle,
  CalendarDays,
  Flag,
  GitBranch,
  UserRound,
} from 'lucide-react';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
  Card,
  CardContent,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMWorkItemBoardCardApi } from '../../../types/api';
import { formatDate, getInitials } from '../workItemView.utils';

interface PMWorkItemBoardCardProps {
  item: PMWorkItemBoardCardApi;
  onSelect: (workItemId: number) => void;
}

function formatBoardDate(value?: string | number | null): string | null {
  return formatDate(value, '', { month: 'short', day: 'numeric' }) || null;
}

function getDueDateState(
  value?: string | number | null
): 'normal' | 'soon' | 'overdue' | null {
  if (!value) return null;

  const dueDate = new Date(value);
  if (Number.isNaN(dueDate.getTime())) return null;

  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const dueDay = new Date(
    dueDate.getFullYear(),
    dueDate.getMonth(),
    dueDate.getDate()
  );
  const diffInDays = Math.round(
    (dueDay.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
  );

  if (diffInDays < 0) return 'overdue';
  if (diffInDays <= 3) return 'soon';
  return 'normal';
}

function getAssigneeLabel(assigneeId?: number | null): string {
  return assigneeId ? `Assignee ${assigneeId}` : 'Unassigned';
}

export function PMWorkItemBoardCard({
  item,
  onSelect,
}: PMWorkItemBoardCardProps) {
  const dueDate = formatBoardDate(item.dueDate);
  const dueDateState = getDueDateState(item.dueDate);
  const priorityColor = item.priority?.color || undefined;
  const assigneeLabel = item.assigneeName || getAssigneeLabel(item.assigneeId);

  return (
    <Card
      role='button'
      tabIndex={0}
      onClick={() => onSelect(item.id)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onSelect(item.id);
        }
      }}
      className='group cursor-pointer border-border/70 bg-background shadow-sm transition-all duration-150 hover:-translate-y-0.5 hover:border-primary/35 hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'
    >
      <CardContent className='space-y-3 p-3'>
        <div className='flex items-start justify-between gap-3'>
          <div className='flex min-w-0 items-center gap-2'>
            <span
              className='flex h-5 w-5 shrink-0 items-center justify-center rounded-md border border-border/70 bg-muted/60 text-[10px] font-semibold uppercase text-muted-foreground'
              title={item.issueType?.name ?? 'Work item'}
            >
              {item.issueType?.name?.charAt(0) ?? 'W'}
            </span>

            <span className='truncate text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
              {item.key}
            </span>
          </div>

          {item.issueType?.name ? (
            <Badge
              variant='secondary'
              className='max-w-24 shrink-0 truncate px-2 py-0 text-[10px]'
            >
              {item.issueType.name}
            </Badge>
          ) : null}
        </div>

        <p className='line-clamp-2 text-sm font-medium leading-5 text-foreground transition-colors group-hover:text-primary'>
          {item.summary}
        </p>

        <div className='flex flex-wrap items-center gap-2 text-xs text-muted-foreground'>
          {item.parentId ? (
            <span className='inline-flex items-center gap-1 rounded-full border border-border/60 bg-muted/60 px-2 py-1'>
              <GitBranch className='h-3 w-3' />
              Parent
            </span>
          ) : null}

          {dueDate ? (
            <span
              className={cn(
                'inline-flex items-center gap-1 rounded-full border px-2 py-1',
                dueDateState === 'overdue' &&
                  'border-destructive/30 bg-destructive/10 text-destructive',
                dueDateState === 'soon' &&
                  'border-amber-500/30 bg-amber-500/10 text-amber-700 dark:text-amber-300',
                dueDateState === 'normal' &&
                  'border-border/60 bg-muted/60 text-muted-foreground'
              )}
            >
              {dueDateState === 'overdue' ? (
                <AlertTriangle className='h-3 w-3' />
              ) : (
                <CalendarDays className='h-3 w-3' />
              )}
              {dueDate}
            </span>
          ) : null}
        </div>

        <div className='flex items-center justify-between gap-3 border-t border-border/60 pt-3'>
          <div className='flex min-w-0 items-center gap-2'>
            {item.priority?.name ? (
              <span className='inline-flex min-w-0 items-center gap-1.5 text-xs text-muted-foreground'>
                <Flag
                  className='h-3.5 w-3.5 shrink-0'
                  style={priorityColor ? { color: priorityColor } : undefined}
                />
                <span className='max-w-20 truncate font-medium'>
                  {item.priority.name}
                </span>
              </span>
            ) : (
              <span className='text-xs text-muted-foreground'>No priority</span>
            )}
          </div>

          <div className='flex min-w-0 items-center gap-2'>
            <span className='truncate text-xs text-muted-foreground'>
              {assigneeLabel}
            </span>
            <Avatar className='h-6 w-6 border border-border/60'>
              {item.assigneeAvatarUrl ? (
                <AvatarImage src={item.assigneeAvatarUrl} alt={assigneeLabel} />
              ) : null}
              <AvatarFallback
                className={cn(
                  'text-[10px] font-semibold',
                  item.assigneeId
                    ? 'bg-primary/10 text-primary'
                    : 'bg-muted text-muted-foreground'
                )}
              >
                {item.assigneeId ? (
                  getInitials(assigneeLabel)
                ) : (
                  <UserRound className='h-3 w-3' />
                )}
              </AvatarFallback>
            </Avatar>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
