/**
 * PTM v2 - Note Card Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Note card for list display
 */

'use client';

import { useState } from 'react';
import { Pin, Trash2, Clock, Paperclip } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { cn } from '@/shared/utils';
import {
  useUpdateNoteMutation,
  useDeleteNoteMutation,
} from '../../services/noteApi';
import type { Note } from '../../types';
import { toast } from 'sonner';
import { RichTextPreview } from './RichTextPreview';
import { NoteDetailDialog } from './NoteDetailDialog';

interface NoteCardProps {
  note: Note;
  onClick?: (noteId: number) => void;
  className?: string;
}

export function NoteCard({ note, onClick, className }: NoteCardProps) {
  const [updateNote] = useUpdateNoteMutation();
  const [deleteNote] = useDeleteNoteMutation();
  const [isHovered, setIsHovered] = useState(false);
  const [showDetail, setShowDetail] = useState(false);

  const handleTogglePin = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await updateNote({
        id: note.id,
        isPinned: !note.isPinned,
      }).unwrap();
      toast.success(note.isPinned ? 'Note unpinned' : 'Note pinned');
    } catch (error) {
      toast.error('Failed to update note');
    }
  };

  const handleDelete = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
      await deleteNote(note.id).unwrap();
      toast.success('Note deleted');
    } catch (error) {
      toast.error('Failed to delete note');
    }
  };

  const truncateContent = (content: string, maxLength: number = 150) => {
    if (content.length <= maxLength) return content;
    return content.substring(0, maxLength) + '...';
  };

  const handleCardClick = () => {
    if (onClick) {
      onClick(note.id);
    } else {
      setShowDetail(true);
    }
  };

  return (
    <>
      <Card
        className={cn(
          'cursor-pointer transition-all hover:shadow-md',
          note.isPinned && 'border-yellow-400 dark:border-yellow-600',
          className
        )}
        onClick={handleCardClick}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        <CardContent className='p-4 space-y-3'>
          {/* Header */}
          <div className='flex items-start justify-between gap-2'>
            <div className='flex-1 min-w-0'>
              {note.isPinned && (
                <div className='flex items-center gap-1 text-yellow-600 dark:text-yellow-500 text-xs font-medium mb-1'>
                  <Pin className='h-3 w-3 fill-current' />
                  <span>Pinned</span>
                </div>
              )}
            </div>
            {isHovered && (
              <div
                className='flex items-center gap-1'
                onClick={(e) => e.stopPropagation()}
              >
                <Button
                  variant='ghost'
                  size='icon'
                  className='h-7 w-7'
                  onClick={handleTogglePin}
                >
                  <Pin
                    className={cn(
                      'h-3.5 w-3.5',
                      note.isPinned && 'fill-yellow-600 text-yellow-600'
                    )}
                  />
                </Button>
                <Button
                  variant='ghost'
                  size='icon'
                  className='h-7 w-7 text-red-600 hover:text-red-700'
                  onClick={handleDelete}
                >
                  <Trash2 className='h-3.5 w-3.5' />
                </Button>
              </div>
            )}
          </div>

          {/* Content - Rich Text Preview */}
          <div className='space-y-2 line-clamp-4 overflow-hidden'>
            <RichTextPreview
              content={note.content}
              maxLength={150}
              className='text-sm'
            />
          </div>

          {/* Attachments */}
          {note.attachments && note.attachments.length > 0 && (
            <div className='flex items-center gap-1 text-xs text-muted-foreground'>
              <Paperclip className='h-3 w-3' />
              <span>
                {note.attachments.length} attachment
                {note.attachments.length > 1 ? 's' : ''}
              </span>
            </div>
          )}

          {/* Footer */}
          <div className='flex items-center gap-2 text-xs text-muted-foreground pt-2 border-t'>
            <Clock className='h-3 w-3' />
            <span>
              {new Date(note.updatedAt).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric',
              })}
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Detail Dialog */}
      <NoteDetailDialog
        note={note}
        open={showDetail}
        onOpenChange={setShowDetail}
      />
    </>
  );
}
