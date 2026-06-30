/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

'use client';

import React, { useState } from 'react';
import {
  useGetNotesQuery,
  useCreateNoteMutation,
  useUpdateNoteMutation,
  useDeleteNoteMutation,
} from '../../api/crmApi';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { selectOrganizationId } from '@/modules/account/store';
import { useAppSelector } from '@/shared/hooks';
import {
  Button,
  Textarea,
  Avatar,
  AvatarFallback,
  Card,
  CardContent,
} from '@/shared/components/ui';
import { MessageSquare, Edit2, Trash2, Check, X, Send } from 'lucide-react';
import { toast } from 'sonner';

interface CRMNotesTabProps {
  entityType: 'LEAD' | 'ACCOUNT' | 'OPPORTUNITY' | 'ACTIVITY';
  entityId: string;
}

export const CRMNotesTab: React.FC<CRMNotesTabProps> = ({
  entityType,
  entityId,
}) => {
  const [newNoteContent, setNewNoteContent] = useState('');
  const [editingNoteId, setEditingNoteId] = useState<string | null>(null);
  const [editContent, setEditContent] = useState('');

  const organizationId = useAppSelector(selectOrganizationId);
  const { data: notesData, isLoading: isNotesLoading } = useGetNotesQuery({
    entityType,
    entityId,
  });
  const { data: orgUsersResponse } = useGetOrganizationUsersQuery(
    { organizationId: organizationId as number, page: 0, pageSize: 100 },
    { skip: !organizationId }
  );

  const [createNote, { isLoading: isCreating }] = useCreateNoteMutation();
  const [updateNote, { isLoading: isUpdating }] = useUpdateNoteMutation();
  const [deleteNote, { isLoading: isDeleting }] = useDeleteNoteMutation();

  const notes = notesData?.data?.data ?? [];
  const users = orgUsersResponse?.data?.items ?? [];

  const getUserName = (userId: string) => {
    const user = users.find((u) => String(u.id) === userId);
    if (!user) return `User #${userId}`;
    const name = [user.firstName, user.lastName].filter(Boolean).join(' ');
    return name || user.email;
  };

  const getUserInitials = (userId: string) => {
    const name = getUserName(userId);
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const handleAddNote = async () => {
    if (!newNoteContent.trim()) return;
    try {
      await createNote({
        entityType,
        entityId: Number(entityId),
        content: newNoteContent.trim(),
      }).unwrap();
      setNewNoteContent('');
      toast.success('Note added successfully');
    } catch {
      toast.error('Failed to add note');
    }
  };

  const handleUpdateNote = async (noteId: string) => {
    if (!editContent.trim()) return;
    try {
      await updateNote({
        id: noteId,
        data: { content: editContent.trim() },
        entityType,
        entityId,
      }).unwrap();
      setEditingNoteId(null);
      toast.success('Note updated successfully');
    } catch {
      toast.error('Failed to update note');
    }
  };

  const handleDeleteNote = async (noteId: string) => {
    if (!confirm('Are you sure you want to delete this note?')) return;
    try {
      await deleteNote({ id: noteId, entityType, entityId }).unwrap();
      toast.success('Note deleted successfully');
    } catch {
      toast.error('Failed to delete note');
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className='space-y-6'>
      <Card className='border-none shadow-sm'>
        <CardContent className='p-4'>
          <div className='flex gap-3'>
            <Textarea
              value={newNoteContent}
              onChange={(e) => setNewNoteContent(e.target.value)}
              placeholder='Type a note or log updates here...'
              disabled={isCreating}
              rows={3}
              className='resize-none'
            />
          </div>
          <div className='mt-3 flex justify-end'>
            <Button
              onClick={handleAddNote}
              disabled={!newNoteContent.trim() || isCreating}
              className='gap-2'
            >
              <Send className='h-4 w-4' />
              Add Note
            </Button>
          </div>
        </CardContent>
      </Card>

      {isNotesLoading ? (
        <div className='text-center py-6 text-muted-foreground'>
          Loading notes...
        </div>
      ) : notes.length > 0 ? (
        <div className='relative pl-6 before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-muted space-y-6'>
          {notes.map((note) => {
            const isEditing = editingNoteId === note.id;
            return (
              <div key={note.id} className='relative group'>
                <div className='absolute -left-9 top-1.5 flex h-6 w-6 items-center justify-center rounded-full border bg-background text-muted-foreground'>
                  <Avatar className='h-6 w-6'>
                    <AvatarFallback className='text-[8px]'>
                      {getUserInitials(note.createdBy)}
                    </AvatarFallback>
                  </Avatar>
                </div>

                <div className='rounded-lg border bg-card p-4 shadow-sm transition-shadow hover:shadow-md'>
                  <div className='flex items-center justify-between gap-3 mb-2'>
                    <div>
                      <span className='font-semibold text-sm text-foreground mr-2'>
                        {getUserName(note.createdBy)}
                      </span>
                      <span className='text-xs text-muted-foreground'>
                        {formatDate(note.createdAt)}
                      </span>
                    </div>
                    <div className='flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity'>
                      {!isEditing && (
                        <>
                          <Button
                            variant='ghost'
                            size='icon'
                            className='h-7 w-7 text-muted-foreground hover:text-foreground'
                            onClick={() => {
                              setEditingNoteId(note.id);
                              setEditContent(note.content);
                            }}
                          >
                            <Edit2 className='h-3.5 w-3.5' />
                          </Button>
                          <Button
                            variant='ghost'
                            size='icon'
                            className='h-7 w-7 text-destructive hover:text-destructive/80'
                            onClick={() => handleDeleteNote(note.id)}
                            disabled={isDeleting}
                          >
                            <Trash2 className='h-3.5 w-3.5' />
                          </Button>
                        </>
                      )}
                    </div>
                  </div>

                  {isEditing ? (
                    <div className='space-y-3 mt-2'>
                      <Textarea
                        value={editContent}
                        onChange={(e) => setEditContent(e.target.value)}
                        rows={3}
                        disabled={isUpdating}
                      />
                      <div className='flex justify-end gap-2'>
                        <Button
                          variant='outline'
                          size='sm'
                          onClick={() => setEditingNoteId(null)}
                          disabled={isUpdating}
                        >
                          <X className='mr-1.5 h-3.5 w-3.5' />
                          Cancel
                        </Button>
                        <Button
                          size='sm'
                          onClick={() => handleUpdateNote(note.id)}
                          disabled={!editContent.trim() || isUpdating}
                        >
                          <Check className='mr-1.5 h-3.5 w-3.5' />
                          Save
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <p className='text-sm text-foreground/80 whitespace-pre-wrap'>
                      {note.content}
                    </p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className='flex flex-col items-center justify-center rounded-lg border border-dashed py-12 text-center text-muted-foreground'>
          <MessageSquare className='mb-3 h-8 w-8 text-muted-foreground/40' />
          <p className='text-sm'>No notes added yet.</p>
        </div>
      )}
    </div>
  );
};

export default CRMNotesTab;
