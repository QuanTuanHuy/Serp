/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization combined review table
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
import {
  getEffectiveAllocationChunks,
  getEffectiveAssigneeId,
  getEffectiveScheduleRange,
  hasMeaningfulAssignmentChange,
  hasMeaningfulScheduleChange,
} from '../../utils/optimizationReview';
import type {
  PMOptimizationDecision,
  PMOptimizationRunItemApi,
  PMOptimizationScheduleAllocationApi,
  PMOptimizationUserSummaryApi,
} from '../../types/api';

type PMOptimizationRunReviewTableProps = {
  items: PMOptimizationRunItemApi[];
  selectedIds: number[];
  canEditAssignment: boolean;
  canEditSchedule: boolean;
  onToggleApply: (workItemId: number) => void;
  onAccept: (item: PMOptimizationRunItemApi) => void;
  onReject: (item: PMOptimizationRunItemApi) => void;
  onOverride: (item: PMOptimizationRunItemApi) => void;
  disabled?: boolean;
};

const DECISION_LABELS: Record<PMOptimizationDecision, string> = {
  ACCEPTED: 'Accept',
  REJECTED: 'Reject',
  OVERRIDDEN: 'Override',
  PENDING: 'Pending',
};

export function PMOptimizationRunReviewTable({
  items,
  selectedIds,
  canEditAssignment,
  canEditSchedule,
  onToggleApply,
  onAccept,
  onReject,
  onOverride,
  disabled = false,
}: PMOptimizationRunReviewTableProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>Review work items</CardTitle>
      </CardHeader>
      <CardContent className='p-0'>
        <ScrollArea className='h-[640px]'>
          <div className='divide-y'>
            {items.length ? (
              items.map((item) => {
                const effectiveAssigneeId = getEffectiveAssigneeId(item);
                const effectiveRange = getEffectiveScheduleRange(item);
                const allocationChunks = getEffectiveAllocationChunks(item);
                const meaningfulAssignment = hasMeaningfulAssignmentChange(
                  item,
                  canEditAssignment
                );
                const meaningfulSchedule = hasMeaningfulScheduleChange(
                  item,
                  canEditSchedule
                );
                const hasMeaningfulChange =
                  meaningfulAssignment || meaningfulSchedule;
                const targetAssignee =
                  item.assignmentDecision === 'OVERRIDDEN'
                    ? item.overrideAssignee
                    : item.suggestedAssignee;

                return (
                  <div
                    key={item.id}
                    className={cn(
                      'grid gap-3 px-4 py-3 xl:grid-cols-[28px_minmax(0,1.2fr)_minmax(220px,0.8fr)_minmax(260px,1fr)_170px]',
                      disabled && 'opacity-60'
                    )}
                  >
                    <div className='pt-1'>
                      <Checkbox
                        checked={selectedIds.includes(item.workItemId)}
                        disabled={disabled}
                        aria-label={`Select work item ${
                          item.workItem?.key || item.workItemId
                        }`}
                        onCheckedChange={() => onToggleApply(item.workItemId)}
                      />
                    </div>

                    <div className='min-w-0'>
                      <div className='flex flex-wrap items-center gap-2'>
                        <span className='text-xs font-semibold text-primary'>
                          {item.workItem?.key || `#${item.workItemId}`}
                        </span>
                        <DecisionBadge
                          label='A'
                          decision={item.assignmentDecision}
                        />
                        <DecisionBadge
                          label='S'
                          decision={item.scheduleDecision}
                        />
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
                    </div>

                    <ReviewLane
                      title='Assignment'
                      current={formatAssignee(
                        item.currentAssignee,
                        item.currentAssigneeId
                      )}
                      target={formatAssignee(
                        targetAssignee,
                        effectiveAssigneeId
                      )}
                      active={meaningfulAssignment}
                      disabled={!canEditAssignment}
                    />

                    <div className='space-y-2'>
                      <ReviewLane
                        title='Schedule'
                        current={`${formatDateTime(
                          item.currentPlannedStart
                        )} -> ${formatDateTime(item.currentPlannedEnd)}`}
                        target={`${formatDateTime(
                          effectiveRange.start
                        )} -> ${formatDateTime(effectiveRange.end)}`}
                        active={meaningfulSchedule}
                        disabled={!canEditSchedule}
                      />
                      <AllocationPreview chunks={allocationChunks} />
                    </div>

                    <div className='flex flex-wrap gap-2 xl:justify-end'>
                      <Button
                        type='button'
                        size='sm'
                        variant='outline'
                        onClick={() => onAccept(item)}
                        disabled={disabled || !hasMeaningfulChange}
                      >
                        <Check className='mr-2 h-4 w-4' />
                        Accept
                      </Button>
                      <Button
                        type='button'
                        size='sm'
                        variant='outline'
                        onClick={() => onReject(item)}
                        disabled={disabled || !hasMeaningfulChange}
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

function DecisionBadge({
  label,
  decision,
}: {
  label: string;
  decision?: PMOptimizationDecision | null;
}) {
  return (
    <Badge variant='secondary' className='h-5 px-1.5 text-xs'>
      {label}: {DECISION_LABELS[decision || 'PENDING']}
    </Badge>
  );
}

function ReviewLane({
  title,
  current,
  target,
  active,
  disabled,
}: {
  title: string;
  current: string;
  target: string;
  active: boolean;
  disabled: boolean;
}) {
  return (
    <div
      className={cn(
        'rounded-md border bg-muted/20 px-3 py-2 text-sm',
        active && 'border-primary/40 bg-primary/5',
        disabled && 'opacity-60'
      )}
    >
      <div className='mb-1 flex items-center justify-between gap-2'>
        <p className='font-medium'>{title}</p>
        {active ? (
          <Badge variant='secondary' className='h-5 px-1.5 text-xs'>
            Changed
          </Badge>
        ) : null}
      </div>
      <p className='truncate text-muted-foreground'>Current: {current}</p>
      <p className='truncate text-muted-foreground'>Target: {target}</p>
    </div>
  );
}

function AllocationPreview({
  chunks,
}: {
  chunks: PMOptimizationScheduleAllocationApi[];
}) {
  if (!chunks.length) {
    return null;
  }

  return (
    <div className='rounded-md border bg-muted/20 px-3 py-2 text-xs text-muted-foreground'>
      {chunks.slice(0, 2).map((chunk, index) => (
        <div key={`${chunk.assigneeId}-${chunk.start}-${index}`}>
          User #{chunk.assigneeId}: {formatDateTime(chunk.start)}
          {' -> '}
          {formatDateTime(chunk.end)} ({formatEffort(chunk.effortMillis)})
        </div>
      ))}
      {chunks.length > 2 ? <div>+{chunks.length - 2} more chunks</div> : null}
    </div>
  );
}

function formatDateTime(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}

function formatEffort(value?: number | null) {
  if (!value) return '0m';
  const minutes = Math.round(value / 60000);
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return remainder ? `${hours}h ${remainder}m` : `${hours}h`;
}

function formatAssignee(
  user?: PMOptimizationUserSummaryApi | null,
  userId?: number | null
) {
  if (!userId) return '-';
  return user?.displayName || `User #${userId}`;
}
