/**
 * PTM - Note Operations Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Centralized note CRUD operations
 */

'use client';

import { toast } from 'sonner';
import {
  useGetNotesByTaskQuery,
  useCreateNoteMutation,
  useUpdateNoteMutation,
  useDeleteNoteMutation,
} from '../api';

export interface UseNoteOperationsOptions {
  taskId: number | null;
}

export function useNoteOperations({ taskId }: UseNoteOperationsOptions) {
  const { data: notes = [] } = useGetNotesByTaskQuery(taskId!, {
    skip: !taskId,
  });

  const [createNote] = useCreateNoteMutation();
  const [updateNote] = useUpdateNoteMutation();
  const [deleteNote] = useDeleteNoteMutation();

  /**
   * Create new note
   */
  const handleCreate = async (content: string, isPinned: boolean) => {
    if (!taskId) return;

    try {
      await createNote({
        taskId,
        content,
        isPinned,
      }).unwrap();

      toast.success('Note created');
      return true;
    } catch (error) {
      toast.error('Failed to create note');
      throw error;
    }
  };

  /**
   * Update existing note
   */
  const handleUpdate = async (
    noteId: number,
    content: string,
    isPinned: boolean
  ) => {
    try {
      await updateNote({
        id: noteId,
        content,
        isPinned,
      }).unwrap();

      toast.success('Note updated');
    } catch (error) {
      toast.error('Failed to update note');
      throw error;
    }
  };

  /**
   * Delete note with confirmation
   */
  const handleDelete = async (noteId: number) => {
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
      await deleteNote(noteId).unwrap();
      toast.success('Note deleted');
    } catch (error) {
      toast.error('Failed to delete note');
    }
  };

  /**
   * Sort notes: pinned first, then by date (newest first)
   */
  const sortedNotes = [...notes].sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1;
    if (!a.isPinned && b.isPinned) return 1;
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
  });

  return {
    notes: sortedNotes,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}
