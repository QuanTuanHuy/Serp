/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item comments and activity
 */

import { useState } from 'react';
import { Loader2, MessageSquare, Pencil, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { Badge, Button, Textarea } from '@/shared/components/ui';
import {
  useCreatePmWorkItemCommentMutation,
  useDeletePmWorkItemCommentMutation,
  useUpdatePmWorkItemCommentMutation,
} from '../../../api/workItemApi';
import type {
  PMWorkItemActivityApi,
  PMWorkItemCommentApi,
} from '../../../types/api';
import { PMUserAvatar } from './PMWorkItemDetailPrimitives';
import { InlineError, ListSkeleton } from './PMWorkItemDetailStates';
import { InlineEditorActions } from './PMWorkItemInlineEditors';
import { formatRelativeTime } from './pmWorkItemDetail.utils';
import type { DetailQueryState } from './pmWorkItemDetail.types';

export function CommentComposer({
  projectId,
  workItemId,
  reporterName,
}: {
  projectId: number;
  workItemId?: number;
  reporterName?: string | null;
}) {
  const [body, setBody] = useState('');
  const [createComment, createState] = useCreatePmWorkItemCommentMutation();

  const handleSubmit = async () => {
    const nextBody = body.trim();
    if (!workItemId || !nextBody) return;

    try {
      await createComment({ projectId, workItemId, body: nextBody }).unwrap();
      setBody('');
      toast.success('Comment added.');
    } catch (error) {
      toast.error('Failed to add comment', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='flex gap-3'>
      <PMUserAvatar name={reporterName ?? 'User'} />
      <div className='flex-1 space-y-2 rounded-md border p-3'>
        <Textarea
          value={body}
          rows={3}
          placeholder='Add a comment...'
          onChange={(event) => setBody(event.target.value)}
        />
        <div className='flex justify-end'>
          <Button
            size='sm'
            className='gap-2'
            disabled={!body.trim() || createState.isLoading}
            onClick={handleSubmit}
          >
            {createState.isLoading ? (
              <Loader2 className='h-4 w-4 animate-spin' />
            ) : (
              <MessageSquare className='h-4 w-4' />
            )}
            Comment
          </Button>
        </div>
      </div>
    </div>
  );
}

export function CommentsList({
  projectId,
  workItemId,
  query,
}: {
  projectId: number;
  workItemId?: number;
  query: DetailQueryState<{ data: { items: PMWorkItemCommentApi[] } }>;
}) {
  if (query.isLoading) return <ListSkeleton rows={3} />;
  if (query.error) return <InlineError error={query.error} />;

  const comments = query.data?.data.items ?? [];
  if (comments.length === 0) {
    return (
      <p className='pl-12 text-sm text-muted-foreground'>No comments yet.</p>
    );
  }

  return (
    <div className='space-y-3'>
      {comments.map((comment) => (
        <CommentItem
          key={comment.id}
          projectId={projectId}
          workItemId={workItemId}
          comment={comment}
        />
      ))}
    </div>
  );
}

function CommentItem({
  projectId,
  workItemId,
  comment,
}: {
  projectId: number;
  workItemId?: number;
  comment: PMWorkItemCommentApi;
}) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(comment.body);
  const [updateComment, updateState] = useUpdatePmWorkItemCommentMutation();
  const [deleteComment, deleteState] = useDeletePmWorkItemCommentMutation();

  const authorName =
    comment.author?.displayName ?? `User #${comment.author?.id ?? ''}`;

  const handleUpdate = async () => {
    const nextBody = draft.trim();
    if (!workItemId || !nextBody) return;

    try {
      await updateComment({
        projectId,
        workItemId,
        commentId: comment.id,
        body: nextBody,
      }).unwrap();
      setEditing(false);
      toast.success('Comment updated.');
    } catch (error) {
      toast.error('Failed to update comment', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDelete = async () => {
    if (!workItemId) return;

    try {
      await deleteComment({
        projectId,
        workItemId,
        commentId: comment.id,
      }).unwrap();
      toast.success('Comment deleted.');
    } catch (error) {
      toast.error('Failed to delete comment', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='flex gap-3'>
      <PMUserAvatar name={authorName} avatarUrl={comment.author?.avatarUrl} />
      <div className='min-w-0 flex-1 rounded-md border p-3'>
        <div className='mb-2 flex flex-wrap items-center justify-between gap-2'>
          <div className='min-w-0'>
            <p className='truncate text-sm font-semibold'>{authorName}</p>
            <p className='text-xs text-muted-foreground'>
              {formatRelativeTime(comment.createdAt)}
              {comment.edited ? ' - edited' : ''}
            </p>
          </div>
          <div className='flex shrink-0 items-center gap-1'>
            <Button
              variant='ghost'
              size='icon'
              className='h-7 w-7'
              onClick={() => {
                setDraft(comment.body);
                setEditing(true);
              }}
            >
              <Pencil className='h-3.5 w-3.5' />
            </Button>
            <Button
              variant='ghost'
              size='icon'
              className='h-7 w-7 text-destructive hover:text-destructive'
              disabled={deleteState.isLoading}
              onClick={handleDelete}
            >
              <Trash2 className='h-3.5 w-3.5' />
            </Button>
          </div>
        </div>
        {editing ? (
          <div className='space-y-2'>
            <Textarea
              value={draft}
              rows={4}
              onChange={(event) => setDraft(event.target.value)}
            />
            <InlineEditorActions
              disabled={updateState.isLoading}
              onCancel={() => setEditing(false)}
              onSave={handleUpdate}
            />
          </div>
        ) : (
          <p className='whitespace-pre-wrap text-sm leading-6 text-muted-foreground'>
            {comment.body}
          </p>
        )}
      </div>
    </div>
  );
}

export function ActivitiesList({
  query,
}: {
  query: DetailQueryState<{ data: { items: PMWorkItemActivityApi[] } }>;
}) {
  if (query.isLoading) return <ListSkeleton rows={3} />;
  if (query.error) return <InlineError error={query.error} />;

  const activities = query.data?.data.items ?? [];
  if (activities.length === 0) {
    return (
      <p className='pl-12 text-sm text-muted-foreground'>No activity yet.</p>
    );
  }

  return (
    <div className='space-y-3'>
      {activities.map((activity) => (
        <div key={activity.id} className='flex gap-3'>
          <PMUserAvatar
            name={
              activity.actor?.displayName ?? `User #${activity.actor?.id ?? ''}`
            }
            avatarUrl={activity.actor?.avatarUrl}
          />
          <div className='min-w-0 flex-1 rounded-md border p-3'>
            <div className='mb-1 flex flex-wrap items-center gap-2'>
              <span className='text-sm font-semibold'>
                {activity.actor?.displayName ?? 'Unknown user'}
              </span>
              <Badge variant='outline'>{activity.type}</Badge>
              <span className='text-xs text-muted-foreground'>
                {formatRelativeTime(activity.createdAt)}
              </span>
            </div>
            {activity.type === 'COMMENT' ? (
              <p className='whitespace-pre-wrap text-sm leading-6 text-muted-foreground'>
                {activity.body}
              </p>
            ) : (
              <p className='text-sm text-muted-foreground'>
                Changed{' '}
                <span className='font-medium text-foreground'>
                  {activity.fieldName ?? activity.fieldKey ?? 'field'}
                </span>{' '}
                from{' '}
                <span className='font-medium text-foreground'>
                  {activity.fromValue || 'empty'}
                </span>{' '}
                to{' '}
                <span className='font-medium text-foreground'>
                  {activity.toValue || 'empty'}
                </span>
              </p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
