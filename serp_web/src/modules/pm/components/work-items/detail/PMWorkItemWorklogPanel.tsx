/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item worklog panel
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { HelpCircle, Loader2, Pencil, Timer, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import {
  useCreatePmWorkItemWorklogMutation,
  useDeletePmWorkItemWorklogMutation,
  useGetPmWorkItemWorklogsQuery,
  useUpdatePmWorkItemMutation,
  useUpdatePmWorkItemWorklogMutation,
} from '../../../api/workItemApi';
import type { PMWorklogApi } from '../../../types/api';

interface PMWorkItemWorklogPanelProps {
  projectId: number;
  workItemId?: number;
}

interface WorklogDraft {
  timeSpent: string;
  timeRemaining: string;
}

const emptyDraft = (): WorklogDraft => ({
  timeSpent: '',
  timeRemaining: '',
});

const formatTimestamp = (value?: number | string | null) => {
  if (!value) {
    return 'Unknown';
  }

  return new Date(value).toLocaleString();
};

export function PMWorkItemWorklogPanel({
  projectId,
  workItemId,
}: PMWorkItemWorklogPanelProps) {
  const [editingWorklog, setEditingWorklog] = useState<PMWorklogApi | null>(
    null
  );
  const [timeDialogOpen, setTimeDialogOpen] = useState(false);
  const [draft, setDraft] = useState<WorklogDraft>(() => emptyDraft());

  const { data, isFetching } = useGetPmWorkItemWorklogsQuery(
    {
      projectId,
      workItemId: workItemId ?? 0,
      page: 0,
      pageSize: 20,
      sortBy: 'startDate',
      sortDirection: 'desc',
    },
    { skip: !workItemId }
  );

  const [createWorklog, createState] = useCreatePmWorkItemWorklogMutation();
  const [updateWorklog, updateState] = useUpdatePmWorkItemWorklogMutation();
  const [deleteWorklog, deleteState] = useDeletePmWorkItemWorklogMutation();
  const [updateWorkItem, updateWorkItemState] = useUpdatePmWorkItemMutation();

  const worklogs = data?.items ?? [];

  useEffect(() => {
    if (editingWorklog) {
      setDraft({
        timeSpent: formatDurationInput(editingWorklog.timeSpent),
        timeRemaining: formatDurationInput(data?.workItemTimeRemainingEstimate),
      });
      return;
    }

    setDraft((current) => ({
      timeSpent: current.timeSpent,
      timeRemaining: formatDurationInput(data?.workItemTimeRemainingEstimate),
    }));
  }, [data?.workItemTimeRemainingEstimate, editingWorklog]);

  const isBusy =
    createState.isLoading ||
    updateState.isLoading ||
    deleteState.isLoading ||
    updateWorkItemState.isLoading;

  const title = useMemo(() => {
    if (editingWorklog) {
      return 'Edit time tracking';
    }

    return 'Time tracking';
  }, [editingWorklog]);

  const timeSpentSeconds = parseDurationToSeconds(draft.timeSpent);
  const timeRemainingSeconds = parseDurationToSeconds(draft.timeRemaining);
  const canSave = Boolean(timeSpentSeconds) && !isBusy;

  const openNewDialog = () => {
    setEditingWorklog(null);
    setDraft({
      timeSpent: '',
      timeRemaining: formatDurationInput(data?.workItemTimeRemainingEstimate),
    });
    setTimeDialogOpen(true);
  };

  const openEditDialog = (worklog: PMWorklogApi) => {
    setEditingWorklog(worklog);
    setTimeDialogOpen(true);
  };

  const closeDialog = () => {
    setTimeDialogOpen(false);
    setEditingWorklog(null);
    setDraft(emptyDraft());
  };

  const handleSubmit = async () => {
    if (!workItemId || !timeSpentSeconds) {
      return;
    }

    const body = {
      timeSpent: timeSpentSeconds,
      startDate: editingWorklog?.startDate ?? Date.now(),
      comment: editingWorklog?.comment ?? null,
    };

    try {
      if (editingWorklog) {
        await updateWorklog({
          projectId,
          workItemId,
          worklogId: editingWorklog.id,
          body,
        }).unwrap();
        toast.success('Worklog updated.');
      } else {
        await createWorklog({ projectId, workItemId, body }).unwrap();
        toast.success('Worklog added.');
      }

      if (timeRemainingSeconds !== null) {
        await updateWorkItem({
          projectId,
          workItemId,
          body: { timeRemainingEstimate: timeRemainingSeconds },
        }).unwrap();
      }

      closeDialog();
    } catch (error) {
      toast.error(
        editingWorklog ? 'Failed to update worklog' : 'Failed to add worklog',
        {
          description: getErrorMessage(error),
        }
      );
    }
  };

  const handleDelete = async (worklogId: number) => {
    if (!workItemId) {
      return;
    }

    try {
      await deleteWorklog({ projectId, workItemId, worklogId }).unwrap();
      toast.success('Worklog deleted.');
      if (editingWorklog?.id === worklogId) {
        closeDialog();
      }
    } catch (error) {
      toast.error('Failed to delete worklog', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!workItemId) {
    return null;
  }

  return (
    <div className='space-y-3'>
      <div className='flex items-center justify-between gap-3'>
        <div className='flex items-center gap-2'>
          <Badge variant='secondary'>
            {data?.workItemTimeSpent
              ? `${Math.round(data.workItemTimeSpent / 60)}m logged`
              : 'No logs'}
          </Badge>
          {isFetching ? (
            <Loader2 className='h-4 w-4 animate-spin text-muted-foreground' />
          ) : null}
        </div>
        {worklogs.length ? (
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={openNewDialog}
            disabled={isBusy}
          >
            Log time
          </Button>
        ) : null}
      </div>

      <div className='space-y-2'>
        {worklogs.length ? (
          worklogs.map((worklog) => {
            const isEditing = editingWorklog?.id === worklog.id;

            return (
              <div key={worklog.id} className='rounded-md border p-3'>
                <div className='flex items-start justify-between gap-3'>
                  <div className='min-w-0 space-y-1'>
                    <div className='flex flex-wrap items-center gap-2 text-xs text-muted-foreground'>
                      <span>{Math.round(worklog.timeSpent / 60)}m</span>
                      <span>Started {formatTimestamp(worklog.startDate)}</span>
                      <span>
                        By{' '}
                        {worklog.authorId
                          ? `User #${worklog.authorId}`
                          : 'Unknown'}
                      </span>
                    </div>
                    <p className='whitespace-pre-wrap text-sm'>
                      {worklog.comment || 'No comment'}
                    </p>
                    <div className='flex flex-wrap gap-3 text-xs text-muted-foreground'>
                      <span>Created {formatTimestamp(worklog.createdAt)}</span>
                      <span>Updated {formatTimestamp(worklog.updatedAt)}</span>
                    </div>
                  </div>
                  <div className='flex shrink-0 items-center gap-1'>
                    <Button
                      type='button'
                      variant='ghost'
                      size='icon'
                      className='h-8 w-8'
                      onClick={() => openEditDialog(worklog)}
                    >
                      <Pencil className='h-4 w-4' />
                    </Button>
                    <Button
                      type='button'
                      variant='ghost'
                      size='icon'
                      className='h-8 w-8'
                      onClick={() => handleDelete(worklog.id)}
                      disabled={deleteState.isLoading}
                    >
                      <Trash2 className='h-4 w-4' />
                    </Button>
                  </div>
                </div>
                {isEditing ? (
                  <div className='mt-3 rounded-md border bg-muted/10 p-3 text-xs text-muted-foreground'>
                    Editing this worklog in the time tracking dialog.
                  </div>
                ) : null}
              </div>
            );
          })
        ) : (
          <div className='flex min-h-[320px] flex-col items-center justify-center rounded-md border border-dashed px-6 py-10 text-center'>
            <div className='relative mb-8 flex h-32 w-32 items-center justify-center rounded-full border-[10px] border-primary/70 bg-background'>
              <div className='absolute -top-8 h-8 w-8 rounded-t-full border-4 border-primary/70 border-b-0' />
              <div className='absolute -top-2 h-4 w-8 rounded-t-md bg-primary/70' />
              <Timer className='h-14 w-14 text-primary' />
              <div className='absolute right-6 top-6 h-2 w-2 rounded-full bg-destructive' />
              <div className='absolute bottom-6 left-7 h-2 w-2 rounded-full bg-destructive' />
              <div className='absolute bottom-8 right-8 h-2 w-2 rounded-full bg-destructive' />
            </div>
            <p className='max-w-sm text-sm font-medium text-muted-foreground'>
              No time was logged for this Task yet. Logging time lets you track
              and report on the time spent on the work.
            </p>
            <Button
              type='button'
              variant='link'
              className='mt-8 h-auto p-0'
              onClick={openNewDialog}
              disabled={isBusy}
            >
              Log time
            </Button>
          </div>
        )}
      </div>

      <Dialog
        open={timeDialogOpen}
        onOpenChange={(open) => {
          if (open) {
            setTimeDialogOpen(true);
            return;
          }

          closeDialog();
        }}
      >
        <DialogContent className='max-w-[400px] gap-0 p-0'>
          <DialogHeader className='border-b px-6 py-5'>
            <DialogTitle>{title}</DialogTitle>
          </DialogHeader>
          <div className='space-y-5 px-6 py-5'>
            <div className='grid gap-3 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='worklog-time-spent'>Time spent</Label>
                <Input
                  id='worklog-time-spent'
                  autoFocus
                  value={draft.timeSpent}
                  onChange={(event) =>
                    setDraft((current) => ({
                      ...current,
                      timeSpent: event.target.value,
                    }))
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label
                  htmlFor='worklog-time-remaining'
                  className='inline-flex items-center gap-1'
                >
                  Time remaining
                  <HelpCircle className='h-3.5 w-3.5 text-muted-foreground' />
                </Label>
                <Input
                  id='worklog-time-remaining'
                  value={draft.timeRemaining}
                  onChange={(event) =>
                    setDraft((current) => ({
                      ...current,
                      timeRemaining: event.target.value,
                    }))
                  }
                />
              </div>
            </div>
            <div className='space-y-2 text-sm font-semibold text-muted-foreground'>
              <p>Use the format: 2w 4d 6h 45m</p>
              <ul className='list-disc space-y-1 pl-5 font-medium'>
                <li>w = weeks</li>
                <li>d = days</li>
                <li>h = hours</li>
                <li>m = minutes</li>
              </ul>
            </div>
          </div>
          <DialogFooter className='border-t px-6 py-4'>
            <Button type='button' variant='ghost' onClick={closeDialog}>
              Cancel
            </Button>
            <Button type='button' onClick={handleSubmit} disabled={!canSave}>
              {isBusy ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : null}
              Save
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function parseDurationToSeconds(value: string): number | null {
  const normalized = value.trim().toLowerCase();
  if (!normalized) {
    return null;
  }

  const unitSeconds: Record<string, number> = {
    w: 5 * 8 * 60 * 60,
    d: 8 * 60 * 60,
    h: 60 * 60,
    m: 60,
  };

  const matches = [...normalized.matchAll(/(\d+(?:\.\d+)?)\s*([wdhm])/g)];
  if (!matches.length) {
    const minutes = Number(normalized);
    return Number.isFinite(minutes) && minutes > 0
      ? Math.round(minutes * 60)
      : null;
  }

  const consumed = matches.map((match) => match[0]).join('');
  if (consumed.replace(/\s/g, '') !== normalized.replace(/\s/g, '')) {
    return null;
  }

  return Math.max(
    1,
    Math.round(
      matches.reduce((total, match) => {
        return total + Number(match[1]) * unitSeconds[match[2]];
      }, 0)
    )
  );
}

function formatDurationInput(value?: number | null): string {
  if (!value) {
    return '';
  }

  let remainingMinutes = Math.round(value / 60);
  const weeks = Math.floor(remainingMinutes / (5 * 8 * 60));
  remainingMinutes -= weeks * 5 * 8 * 60;
  const days = Math.floor(remainingMinutes / (8 * 60));
  remainingMinutes -= days * 8 * 60;
  const hours = Math.floor(remainingMinutes / 60);
  const minutes = remainingMinutes - hours * 60;

  return [
    weeks ? `${weeks}w` : '',
    days ? `${days}d` : '',
    hours ? `${hours}h` : '',
    minutes ? `${minutes}m` : '',
  ]
    .filter(Boolean)
    .join(' ');
}
