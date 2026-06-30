/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization override dialog
 */

'use client';

import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import {
  Badge,
  Button,
  Checkbox,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
} from '@/shared/components/ui';
import { PMDatePicker } from '../shared';
import { toLocalDateInputValue } from '../../utils/date';
import {
  fromCalendarDateTimeInputValue,
  toCalendarDateTimeInputValue,
} from '../projects/calendar/pmProjectCalendar.utils';
import type {
  PMOptimizationDecision,
  PMOptimizationRunItemApi,
  PMOptimizationScheduleAllocationApi,
  PMOptimizationUserSummaryApi,
  PMProjectPersonApi,
} from '../../types/api';

type PMOptimizationRunOverrideDialogProps = {
  open: boolean;
  item: PMOptimizationRunItemApi | null;
  users: { id: number; label: string }[];
  assignmentDecision: PMOptimizationDecision;
  scheduleDecision: PMOptimizationDecision;
  overrideAssigneeId: string;
  overridePlannedStart: string;
  overridePlannedEnd: string;
  overrideAllocationChunks: PMOptimizationScheduleAllocationApi[];
  projectPeople: PMProjectPersonApi[];
  onAssignmentDecisionChange: (value: PMOptimizationDecision) => void;
  onScheduleDecisionChange: (value: PMOptimizationDecision) => void;
  onOverrideAssigneeIdChange: (value: string) => void;
  onOverridePlannedStartChange: (value: string) => void;
  onOverridePlannedEndChange: (value: string) => void;
  onOverrideAllocationChunksChange: (
    value: PMOptimizationScheduleAllocationApi[]
  ) => void;
  onSave: () => void;
  onClose: () => void;
  isSaving?: boolean;
};

export function PMOptimizationRunOverrideDialog({
  open,
  item,
  users,
  assignmentDecision,
  scheduleDecision,
  overrideAssigneeId,
  overridePlannedStart,
  overridePlannedEnd,
  overrideAllocationChunks,
  projectPeople,
  onAssignmentDecisionChange,
  onScheduleDecisionChange,
  onOverrideAssigneeIdChange,
  onOverridePlannedStartChange,
  onOverridePlannedEndChange,
  onOverrideAllocationChunksChange,
  onSave,
  onClose,
  isSaving,
}: PMOptimizationRunOverrideDialogProps) {
  const [showAllProjectMembers, setShowAllProjectMembers] = useState(false);
  const projectMemberOptions = projectPeople
    .map((person) => ({
      id: Number(person.userId),
      label: person.name || person.email || `User #${person.userId}`,
    }))
    .sort((left, right) => left.label.localeCompare(right.label));
  const preferredUserIds = new Set(
    [
      item?.currentAssigneeId,
      item?.suggestedAssigneeId,
      item?.overrideAssigneeId,
      Number(overrideAssigneeId) || null,
      ...overrideAllocationChunks.map((chunk) => chunk.assigneeId),
    ].filter((value): value is number => Boolean(value))
  );
  const preferredUsers = projectMemberOptions.filter((user) =>
    preferredUserIds.has(user.id)
  );
  const visibleUsers = showAllProjectMembers
    ? projectMemberOptions
    : preferredUsers.length
      ? preferredUsers
      : users;

  const updateChunk = (
    index: number,
    patch: Partial<PMOptimizationScheduleAllocationApi>
  ) => {
    onOverrideAllocationChunksChange(
      overrideAllocationChunks.map((chunk, chunkIndex) =>
        chunkIndex === index ? { ...chunk, ...patch } : chunk
      )
    );
  };

  const addChunk = () => {
    const lastChunk = overrideAllocationChunks.at(-1);
    const start = lastChunk?.end || Date.now();
    onOverrideAllocationChunksChange([
      ...overrideAllocationChunks,
      {
        assigneeId:
          Number(overrideAssigneeId) || item?.suggestedAssigneeId || 0,
        start,
        end: start + 60 * 60 * 1000,
        effortMillis: 60 * 60 * 1000,
      },
    ]);
  };

  const removeChunk = (index: number) => {
    onOverrideAllocationChunksChange(
      overrideAllocationChunks.filter(
        (_chunk, chunkIndex) => chunkIndex !== index
      )
    );
  };

  const derivedStart = overrideAllocationChunks.length
    ? Math.min(...overrideAllocationChunks.map((chunk) => chunk.start))
    : null;
  const derivedEnd = overrideAllocationChunks.length
    ? Math.max(...overrideAllocationChunks.map((chunk) => chunk.end))
    : null;

  return (
    <Dialog open={open} onOpenChange={(nextOpen) => !nextOpen && onClose()}>
      <DialogContent className='max-h-[85vh] max-w-4xl overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Override suggestion</DialogTitle>
        </DialogHeader>
        {item ? (
          <div className='space-y-4'>
            <div className='flex flex-wrap items-center gap-2 text-sm text-muted-foreground'>
              <Badge variant='secondary'>
                {item.workItem?.key || `Work item #${item.workItemId}`}
              </Badge>
              {item.workItem?.summary ? (
                <span className='font-medium text-foreground'>
                  {item.workItem.summary}
                </span>
              ) : null}
              <span>
                Current assignee:{' '}
                {formatAssignee(item.currentAssignee, item.currentAssigneeId)}
              </span>
              <span>
                Suggested assignee:{' '}
                {formatAssignee(
                  item.suggestedAssignee,
                  item.suggestedAssigneeId
                )}
              </span>
            </div>

            <div className='grid gap-3 md:grid-cols-2'>
              <label className='space-y-1'>
                <span className='text-sm font-medium'>Assignment decision</span>
                <select
                  value={assignmentDecision}
                  onChange={(event) =>
                    onAssignmentDecisionChange(
                      event.target.value as PMOptimizationDecision
                    )
                  }
                  className='h-10 w-full rounded-md border bg-background px-3 text-sm'
                >
                  <option value='OVERRIDDEN'>Override</option>
                  <option value='ACCEPTED'>Accept</option>
                  <option value='REJECTED'>Reject</option>
                  <option value='PENDING'>Pending</option>
                </select>
              </label>
              <label className='space-y-1'>
                <span className='text-sm font-medium'>Schedule decision</span>
                <select
                  value={scheduleDecision}
                  onChange={(event) =>
                    onScheduleDecisionChange(
                      event.target.value as PMOptimizationDecision
                    )
                  }
                  className='h-10 w-full rounded-md border bg-background px-3 text-sm'
                >
                  <option value='OVERRIDDEN'>Override</option>
                  <option value='ACCEPTED'>Accept</option>
                  <option value='REJECTED'>Reject</option>
                  <option value='PENDING'>Pending</option>
                </select>
              </label>
            </div>

            <label className='space-y-1'>
              <span className='text-sm font-medium'>Assignee</span>
              <select
                value={overrideAssigneeId}
                onChange={(event) =>
                  onOverrideAssigneeIdChange(event.target.value)
                }
                className='h-10 w-full rounded-md border bg-background px-3 text-sm'
              >
                <option value=''>No override</option>
                {users.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.label}
                  </option>
                ))}
              </select>
            </label>

            {scheduleDecision === 'OVERRIDDEN' ? (
              <div className='space-y-3'>
                <div className='flex items-center justify-between gap-3'>
                  <div>
                    <p className='text-sm font-medium'>Schedule allocations</p>
                    <p className='text-xs text-muted-foreground'>
                      Planned range is derived from the earliest start and
                      latest end.
                    </p>
                  </div>
                  <Button
                    type='button'
                    variant='outline'
                    size='sm'
                    onClick={addChunk}
                  >
                    <Plus className='mr-2 h-4 w-4' />
                    Add chunk
                  </Button>
                </div>

                <label className='flex items-center gap-2 text-sm text-muted-foreground'>
                  <Checkbox
                    checked={showAllProjectMembers}
                    onCheckedChange={(checked) =>
                      setShowAllProjectMembers(Boolean(checked))
                    }
                  />
                  Show all project members
                </label>

                <div className='rounded-md border bg-muted/20 px-3 py-2 text-sm'>
                  Derived range: {formatDateTime(derivedStart)} -{' '}
                  {formatDateTime(derivedEnd)}
                </div>

                <div className='space-y-2'>
                  {overrideAllocationChunks.map((chunk, index) => (
                    <div
                      key={`${chunk.assigneeId}-${chunk.start}-${index}`}
                      className='grid gap-2 rounded-md border p-3 md:grid-cols-[1fr_1fr_1fr_120px_40px]'
                    >
                      <select
                        value={chunk.assigneeId || ''}
                        onChange={(event) =>
                          updateChunk(index, {
                            assigneeId: Number(event.target.value),
                          })
                        }
                        className='h-10 rounded-md border bg-background px-3 text-sm'
                      >
                        <option value=''>Assignee</option>
                        {visibleUsers.map((user) => (
                          <option key={user.id} value={user.id}>
                            {user.label}
                          </option>
                        ))}
                      </select>
                      <Input
                        type='datetime-local'
                        value={toCalendarDateTimeInputValue(chunk.start)}
                        onChange={(event) =>
                          updateChunk(index, {
                            start:
                              fromCalendarDateTimeInputValue(
                                event.target.value
                              ) || 0,
                          })
                        }
                      />
                      <Input
                        type='datetime-local'
                        value={toCalendarDateTimeInputValue(chunk.end)}
                        onChange={(event) =>
                          updateChunk(index, {
                            end:
                              fromCalendarDateTimeInputValue(
                                event.target.value
                              ) || 0,
                          })
                        }
                      />
                      <Input
                        type='number'
                        min={1}
                        value={Math.round(chunk.effortMillis / 60000)}
                        onChange={(event) =>
                          updateChunk(index, {
                            effortMillis: Number(event.target.value) * 60000,
                          })
                        }
                      />
                      <Button
                        type='button'
                        size='icon'
                        variant='ghost'
                        onClick={() => removeChunk(index)}
                      >
                        <Trash2 className='h-4 w-4' />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className='grid gap-3 md:grid-cols-2'>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Planned start</span>
                  <PMDatePicker
                    value={overridePlannedStart}
                    onChange={(date) =>
                      onOverridePlannedStartChange(
                        date ? toLocalDateInputValue(date) : ''
                      )
                    }
                    className='w-full'
                    buttonClassName='flex-1'
                  />
                </label>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Planned end</span>
                  <PMDatePicker
                    value={overridePlannedEnd}
                    onChange={(date) =>
                      onOverridePlannedEndChange(
                        date ? toLocalDateInputValue(date) : ''
                      )
                    }
                    className='w-full'
                    buttonClassName='flex-1'
                  />
                </label>
              </div>
            )}
          </div>
        ) : null}
        <DialogFooter>
          <Button type='button' variant='outline' onClick={onClose}>
            Cancel
          </Button>
          <Button type='button' onClick={onSave} disabled={isSaving}>
            Save override
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function formatAssignee(
  user?: PMOptimizationUserSummaryApi | null,
  userId?: number | null
) {
  if (!userId) return '-';
  return user?.displayName || `User #${userId}`;
}

function formatDateTime(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}
