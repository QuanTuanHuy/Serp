/**
 * PTM v2 - Note List Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - List of notes with filters
 */

'use client';

import { useState } from 'react';
import { Plus, StickyNote, Search } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Card } from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { NoteCard } from './NoteCard';
import { NoteEditorNovel } from './NoteEditorNovel';
import {
  useGetNotesByTaskQuery,
  useGetNotesByProjectQuery,
  useCreateNoteMutation,
} from '../../api';
import type { Note } from '../../types';
import { toast } from 'sonner';

interface NoteListProps {
  taskId?: number | string;
  projectId?: number | string;
  onNoteClick?: (noteId: number) => void;
}

export function NoteList({ taskId, projectId, onNoteClick }: NoteListProps) {
  const [isCreating, setIsCreating] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Convert to numbers for API calls
  const numericTaskId = taskId
    ? typeof taskId === 'string'
      ? parseInt(taskId, 10)
      : taskId
    : undefined;
  const numericProjectId = projectId
    ? typeof projectId === 'string'
      ? parseInt(projectId, 10)
      : projectId
    : undefined;

  // Use appropriate query based on taskId or projectId
  const { data: taskNotes = [], isLoading: isLoadingTask } =
    useGetNotesByTaskQuery(numericTaskId!, { skip: !numericTaskId });
  const { data: projectNotes = [], isLoading: isLoadingProject } =
    useGetNotesByProjectQuery(numericProjectId!, { skip: !numericProjectId });

  const notes = taskId ? taskNotes : projectNotes;
  const isLoading = taskId ? isLoadingTask : isLoadingProject;

  const [createNote] = useCreateNoteMutation();

  const handleCreateNote = async (content: string, isPinned: boolean) => {
    try {
      // Content is already JSON string from Novel editor
      await createNote({
        taskId: numericTaskId,
        projectId: numericProjectId,
        content, // JSON content for full editing capability
        isPinned,
      }).unwrap();

      toast.success('Note created successfully');
      setIsCreating(false);
    } catch (error) {
      toast.error('Failed to create note');
    }
  };

  // Filter notes based on search
  const filteredNotes = notes.filter((note) =>
    note.contentPlain.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Separate pinned and unpinned notes
  const pinnedNotes = filteredNotes.filter((note) => note.isPinned);
  const unpinnedNotes = filteredNotes.filter((note) => !note.isPinned);

  if (isLoading) {
    return (
      <div className='space-y-4'>
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} className='h-32 w-full' />
        ))}
      </div>
    );
  }

  return (
    <div className='space-y-4'>
      {/* Header */}
      <div className='flex items-center justify-between gap-3'>
        <div className='flex-1'>
          <div className='relative'>
            <Search className='absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground' />
            <Input
              type='text'
              placeholder='Search notes...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-9'
            />
          </div>
        </div>
        <Button
          onClick={() => setIsCreating(!isCreating)}
          variant={isCreating ? 'outline' : 'default'}
        >
          <Plus className='h-4 w-4 mr-2' />
          {isCreating ? 'Cancel' : 'New Note'}
        </Button>
      </div>

      {/* Note Editor */}
      {isCreating && (
        <NoteEditorNovel
          onSave={handleCreateNote}
          onCancel={() => setIsCreating(false)}
        />
      )}

      {/* Notes List */}
      {filteredNotes.length === 0 ? (
        <Card className='p-12 text-center'>
          <div className='flex flex-col items-center gap-3 text-muted-foreground'>
            <StickyNote className='h-12 w-12' />
            <p className='text-lg font-medium'>
              {searchQuery ? 'No notes found' : 'No notes yet'}
            </p>
            <p className='text-sm'>
              {searchQuery
                ? 'Try a different search term'
                : 'Create your first note to get started'}
            </p>
          </div>
        </Card>
      ) : (
        <div className='space-y-4'>
          {/* Pinned Notes */}
          {pinnedNotes.length > 0 && (
            <div className='space-y-3'>
              <h3 className='text-sm font-semibold text-muted-foreground uppercase tracking-wide'>
                Pinned Notes
              </h3>
              <div className='grid gap-3'>
                {pinnedNotes.map((note: Note) => (
                  <NoteCard key={note.id} note={note} onClick={onNoteClick} />
                ))}
              </div>
            </div>
          )}

          {/* Unpinned Notes */}
          {unpinnedNotes.length > 0 && (
            <div className='space-y-3'>
              {pinnedNotes.length > 0 && (
                <h3 className='text-sm font-semibold text-muted-foreground uppercase tracking-wide'>
                  Other Notes
                </h3>
              )}
              <div className='grid gap-3'>
                {unpinnedNotes.map((note: Note) => (
                  <NoteCard key={note.id} note={note} onClick={onNoteClick} />
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
