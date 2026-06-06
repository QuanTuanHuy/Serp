/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization run item table
 */

'use client';

import { Check, PenLine, X } from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  ScrollArea,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  PMOptimizationDecision,
  PMOptimizationRunItemApi,
  PMOptimizationUserSummaryApi,
} from '../../types/api';

type PMOptimizationRunItemTableProps = {
  title: string;
  mode: 'assignment' | 'schedule';
  items: PMOptimizationRunItemApi[];
  selectedIds: number[];
  onToggleApply: (workItemId: number) => void;
  onAccept: (item: PMOptimizationRunItemApi) => void;
  onReject: (item: PMOptimizationRunItemApi) => void;
  onOverride: (item: PMOptimizationRunItemApi) => void;
  disabled?: boolean;
  disabledMessage?: string;
};

const DECISION_LABELS: Record<PMOptimizationDecision, string> = {
  ACCEPTED: 'Accept',
  REJECTED: 'Reject',
  OVERRIDDEN: 'Override',
  PENDING: 'Pending',
};

export function PMOptimizationRunItemTable({
  title,
  mode,
  items,
  selectedIds,
  onToggleApply,
  onAccept,
  onReject,
  onOverride,
  disabled = false,
  disabledMessage,
}: PMOptimizationRunItemTableProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <div className='space-y-1'>
          <CardTitle className='text-base'>{title}</CardTitle>
          {disabledMessage ? (
            <p className='text-sm text-muted-foreground'>{disabledMessage}</p>
          ) : null}
        </div>
      </CardHeader>
      <CardContent className='p-0'>
        <ScrollArea className='h-[640px]'>
          <div className='divide-y'>
            {items.length ? (
              items.map((item) => {
                const currentValue =
                  mode === 'assignment'
                    ? formatAssignee(
                        item.currentAssignee,
                        item.currentAssigneeId
                      )
                    : `${formatDate(item.currentPlannedStart)} -> ${formatDate(
                        item.currentPlannedEnd
                      )}`;
                const suggestedValue =
                  mode === 'assignment'
                    ? formatAssignee(
                        item.suggestedAssignee,
                        item.suggestedAssigneeId
                      )
                    : `${formatDate(item.suggestedPlannedStart)} -> ${formatDate(
                        item.suggestedPlannedEnd
                      )}`;
                const decision =
                  mode === 'assignment'
                    ? item.assignmentDecision
                    : item.scheduleDecision;
                const reasons =
                  mode === 'assignment'
                    ? item.assignmentReasons || []
                    : item.scheduleReasons || [];

                return (
                  <div
                    key={item.id}
                    className={cn(
                      'grid gap-3 px-4 py-3 xl:grid-cols-[28px_minmax(0,1fr)_240px_220px_180px]',
                      disabled && 'opacity-60'
                    )}
                  >
                    <div className='pt-1'>
                      <Checkbox
                        checked={selectedIds.includes(item.workItemId)}
                        disabled={disabled}
                        onCheckedChange={() => onToggleApply(item.workItemId)}
                      />
                    </div>
                    <div className='min-w-0'>
                      <div className='flex items-center gap-2'>
                        <span className='text-xs font-semibold text-primary'>
                          {item.workItem?.key || `#${item.workItemId}`}
                        </span>
                        <Badge variant='secondary'>
                          {DECISION_LABELS[decision || 'PENDING']}
                        </Badge>
                      </div>
                      {item.workItem?.summary ? (
                        <div className='mt-1 truncate text-sm font-medium'>
                          {item.workItem.summary}
                        </div>
                      ) : null}
                      <div className='mt-2 flex flex-wrap gap-1.5'>
                        {[
                          item.workItem?.issueTypeName,
                          item.workItem?.statusName,
                          item.workItem?.priorityName,
                        ]
                          .filter((label): label is string => Boolean(label))
                          .map((label, index) => (
                            <Badge
                              key={`${label}-${index}`}
                              variant='outline'
                              className='h-5 px-1.5 text-xs'
                            >
                              {label}
                            </Badge>
                          ))}
                      </div>
                      <div className='mt-2 flex flex-wrap gap-2 text-xs text-muted-foreground'>
                        <span>Score {formatValue(item.score)}</span>
                        <span>Cost {formatValue(item.cost)}</span>
                        <span>Confidence {formatValue(item.confidence)}</span>
                      </div>
                    </div>
                    <div className='text-sm text-muted-foreground'>
                      {mode === 'assignment'
                        ? `Current assignee ${currentValue}`
                        : `Current dates ${currentValue}`}
                    </div>
                    <div className='text-sm text-muted-foreground'>
                      {mode === 'assignment'
                        ? `Suggested assignee ${suggestedValue}`
                        : `Suggested dates ${suggestedValue}`}
                    </div>
                    <div className='flex flex-wrap gap-2'>
                      <Button
                        type='button'
                        size='sm'
                        variant='outline'
                        onClick={() => onAccept(item)}
                        disabled={disabled}
                      >
                        <Check className='mr-2 h-4 w-4' />
                        Accept
                      </Button>
                      <Button
                        type='button'
                        size='sm'
                        variant='outline'
                        onClick={() => onReject(item)}
                        disabled={disabled}
                      >
                        <X className='mr-2 h-4 w-4' />
                        Reject
                      </Button>
                      <Button
                        type='button'
                        size='sm'
                        variant='ghost'
                        onClick={() => onOverride(item)}
                        disabled={disabled}
                      >
                        <PenLine className='mr-2 h-4 w-4' />
                        Override
                      </Button>
                    </div>
                    <div className='xl:col-span-4'>
                      <div className='grid gap-2 md:grid-cols-2'>
                        <DetailList title='Reasons' items={reasons} />
                        <DetailList
                          title='Violations'
                          items={item.violations || []}
                        />
                      </div>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className='px-4 py-10 text-sm text-muted-foreground'>
                No suggestions.
              </div>
            )}
          </div>
        </ScrollArea>
      </CardContent>
    </Card>
  );
}

function DetailList({ title, items }: { title: string; items: string[] }) {
  return (
    <div className='rounded-md border bg-muted/20 p-3'>
      <div className='mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
        {title}
        <Badge variant='secondary' className='h-5 px-1.5'>
          {items.length}
        </Badge>
      </div>
      <div className='space-y-1 text-sm'>
        {items.length ? (
          items.slice(0, 4).map((item) => (
            <div key={item} className='truncate text-muted-foreground'>
              {item}
            </div>
          ))
        ) : (
          <div className='text-muted-foreground'>-</div>
        )}
      </div>
    </div>
  );
}

function formatDate(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleDateString();
}

function formatValue(value?: string | number | null) {
  if (value === undefined || value === null || value === '') return '-';
  return String(value);
}

function formatAssignee(
  user?: PMOptimizationUserSummaryApi | null,
  userId?: number | null
) {
  if (!userId) return '-';
  return user?.displayName || `User #${userId}`;
}
