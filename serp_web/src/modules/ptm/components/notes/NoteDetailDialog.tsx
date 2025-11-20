/**
 * PTM v2 - Note Detail Dialog
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - View and edit note in dialog
 */

'use client';

import { useState } from 'react';
import { X, Edit2, Eye } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { RichTextPreview } from './RichTextPreview';
import { NoteEditorNovel } from './NoteEditorNovel';
import { useUpdateNoteMutation } from '../../services/noteApi';
import type { Note } from '../../types';
import { toast } from 'sonner';

interface NoteDetailDialogProps {
  note: Note;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function NoteDetailDialog({
  note,
  open,
  onOpenChange,
}: NoteDetailDialogProps) {
  const [activeTab, setActiveTab] = useState<'view' | 'edit'>('view');
  const [updateNote] = useUpdateNoteMutation();

  const handleSave = async (content: string, isPinned: boolean) => {
    try {
      await updateNote({
        id: note.id,
        content,
        isPinned,
      }).unwrap();

      toast.success('Note updated successfully');
      setActiveTab('view');
    } catch (error) {
      toast.error('Failed to update note');
    }
  };

  const handleCancel = () => {
    setActiveTab('view');
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-4xl max-h-[80vh] overflow-hidden flex flex-col'>
        <DialogHeader>
          <div className='flex items-center justify-between'>
            <DialogTitle>Note Details</DialogTitle>
            <Tabs
              value={activeTab}
              onValueChange={(v) => setActiveTab(v as 'view' | 'edit')}
              className='w-auto'
            >
              <TabsList>
                <TabsTrigger value='view' className='flex items-center gap-2'>
                  <Eye className='h-4 w-4' />
                  View
                </TabsTrigger>
                <TabsTrigger value='edit' className='flex items-center gap-2'>
                  <Edit2 className='h-4 w-4' />
                  Edit
                </TabsTrigger>
              </TabsList>
            </Tabs>
          </div>
        </DialogHeader>

        <div className='flex-1 overflow-y-auto'>
          {activeTab === 'view' ? (
            <div className='p-4'>
              <RichTextPreview
                content={note.content}
                maxLength={10000} // No limit for detail view
                className='min-h-[200px]'
              />

              {/* Note metadata */}
              <div className='mt-6 pt-4 border-t text-sm text-muted-foreground space-y-1'>
                <p>
                  <span className='font-medium'>Created:</span>{' '}
                  {new Date(note.createdAt).toLocaleString()}
                </p>
                <p>
                  <span className='font-medium'>Updated:</span>{' '}
                  {new Date(note.updatedAt).toLocaleString()}
                </p>
                {note.isPinned && (
                  <p className='text-yellow-600 dark:text-yellow-500 font-medium'>
                    📌 Pinned
                  </p>
                )}
              </div>
            </div>
          ) : (
            <NoteEditorNovel
              initialContent={note.content}
              initialPinned={note.isPinned}
              onSave={handleSave}
              onCancel={handleCancel}
            />
          )}
        </div>

        {activeTab === 'view' && (
          <div className='flex justify-end gap-2 pt-4 border-t'>
            <Button variant='outline' onClick={() => onOpenChange(false)}>
              <X className='h-4 w-4 mr-2' />
              Close
            </Button>
            <Button onClick={() => setActiveTab('edit')}>
              <Edit2 className='h-4 w-4 mr-2' />
              Edit Note
            </Button>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
