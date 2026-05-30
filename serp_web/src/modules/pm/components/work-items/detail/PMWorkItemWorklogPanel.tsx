/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item worklog panel
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { Loader2, Pencil, Plus, Save, Trash2, X } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { Badge, Button, Input, Textarea } from '@/shared/components/ui';
import {
  useCreatePmWorkItemWorklogMutation,
  useDeletePmWorkItemWorklogMutation,
  useGetPmWorkItemWorklogsQuery,
  useUpdatePmWorkItemWorklogMutation,
} from '../../../api/workItemApi';
import type { PMWorklogApi } from '../../../types/api';

interface PMWorkItemWorklogPanelProps {
  projectId: number;
  workItemId?: number;
}

interface WorklogDraft {
  timeSpentMinutes: string;
  startDate: string;
  comment: string;
}

const emptyDraft = (): WorklogDraft => ({
  timeSpentMinutes: '',
  startDate: '',
  comment: '',
});

const toDateInputValue = (value?: number | string | null) => {
  if (!value) {
    return '';
  }

  return new Date(value).toISOString().slice(0, 10);
};

const fromDateInputValue = (value: string) => {
  return new Date(`${value}T00:00:00`).getTime();
};

const minutesToSeconds = (value: string) => {
  return Math.max(1, Math.round(Number(value || 0))) * 60;
};

const secondsToMinutes = (value?: number | null) => {
  return value ? String(Math.max(1, Math.round(value / 60))) : '';
};

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

  const worklogs = data?.items ?? [];

  useEffect(() => {
    if (editingWorklog) {
      setDraft({
        timeSpentMinutes: secondsToMinutes(editingWorklog.timeSpent),
        startDate: toDateInputValue(editingWorklog.startDate),
        comment: editingWorklog.comment ?? '',
      });
      return;
    }

    setDraft(emptyDraft());
  }, [editingWorklog]);

  const isBusy =
    createState.isLoading || updateState.isLoading || deleteState.isLoading;

  const title = useMemo(() => {
    if (editingWorklog) {
      return `Edit worklog #${editingWorklog.id}`;
    }

    return 'New worklog';
  }, [editingWorklog]);

  const handleSubmit = async () => {
    if (!workItemId || !draft.startDate || !draft.timeSpentMinutes) {
      return;
    }

    const body = {
      timeSpent: minutesToSeconds(draft.timeSpentMinutes),
      startDate: fromDateInputValue(draft.startDate),
      comment: draft.comment.trim() || null,
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
      setEditingWorklog(null);
      setDraft(emptyDraft());
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
        setEditingWorklog(null);
        setDraft(emptyDraft());
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
        {editingWorklog ? (
          <Button
            type='button'
            variant='ghost'
            size='sm'
            className='gap-2'
            onClick={() => setEditingWorklog(null)}
          >
            <X className='h-4 w-4' />
            Cancel edit
          </Button>
        ) : null}
      </div>

      <div className='rounded-md border bg-muted/20 p-3 space-y-3'>
        <div className='flex items-center gap-2 text-sm font-medium'>
          <Plus className='h-4 w-4' />
          {title}
        </div>
        <div className='grid gap-3 md:grid-cols-[120px_180px_minmax(0,1fr)_auto]'>
          <Input
            type='number'
            min='1'
            step='1'
            placeholder='Minutes'
            value={draft.timeSpentMinutes}
            onChange={(event) =>
              setDraft((current) => ({
                ...current,
                timeSpentMinutes: event.target.value,
              }))
            }
          />
          <Input
            type='date'
            value={draft.startDate}
            onChange={(event) =>
              setDraft((current) => ({
                ...current,
                startDate: event.target.value,
              }))
            }
          />
          <Textarea
            rows={3}
            placeholder='Comment'
            value={draft.comment}
            onChange={(event) =>
              setDraft((current) => ({
                ...current,
                comment: event.target.value,
              }))
            }
          />
          <div className='flex items-start gap-2'>
            <Button
              type='button'
              onClick={handleSubmit}
              disabled={!draft.timeSpentMinutes || !draft.startDate || isBusy}
              className='gap-2'
            >
              {isBusy ? (
                <Loader2 className='h-4 w-4 animate-spin' />
              ) : editingWorklog ? (
                <Save className='h-4 w-4' />
              ) : (
                <Plus className='h-4 w-4' />
              )}
              {editingWorklog ? 'Update' : 'Add'}
            </Button>
          </div>
        </div>
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
                      onClick={() => setEditingWorklog(worklog)}
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
                    Editing this worklog in the form above.
                  </div>
                ) : null}
              </div>
            );
          })
        ) : (
          <div className='rounded-md border border-dashed p-4 text-sm text-muted-foreground'>
            No worklogs yet.
          </div>
        )}
      </div>
    </div>
  );
}
