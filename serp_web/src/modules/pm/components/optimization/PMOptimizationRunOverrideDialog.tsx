/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization override dialog
 */

'use client';

import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { PMDatePicker } from '../shared';
import { toLocalDateInputValue } from '../../utils/date';
import type {
  PMOptimizationDecision,
  PMOptimizationRunItemApi,
  PMOptimizationUserSummaryApi,
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
  onAssignmentDecisionChange: (value: PMOptimizationDecision) => void;
  onScheduleDecisionChange: (value: PMOptimizationDecision) => void;
  onOverrideAssigneeIdChange: (value: string) => void;
  onOverridePlannedStartChange: (value: string) => void;
  onOverridePlannedEndChange: (value: string) => void;
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
  onAssignmentDecisionChange,
  onScheduleDecisionChange,
  onOverrideAssigneeIdChange,
  onOverridePlannedStartChange,
  onOverridePlannedEndChange,
  onSave,
  onClose,
  isSaving,
}: PMOptimizationRunOverrideDialogProps) {
  return (
    <Dialog open={open} onOpenChange={(nextOpen) => !nextOpen && onClose()}>
      <DialogContent className='max-w-2xl'>
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
