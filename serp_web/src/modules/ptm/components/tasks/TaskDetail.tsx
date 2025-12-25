/**
 * PTM v2 - Task Detail Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task detail slide-over panel
 */

'use client';

import { useState, useEffect } from 'react';
import {
  Calendar,
  Clock,
  Tag,
  Trash2,
  Copy,
  ExternalLink,
  Edit,
  Save,
  X,
  StickyNote,
  Plus,
  Link as LinkIcon,
  AlertCircle,
} from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Label } from '@/shared/components/ui/label';
import { Progress } from '@/shared/components/ui/progress';
import { Slider } from '@/shared/components/ui/slider';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Separator } from '@/shared/components/ui/separator';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { cn } from '@/shared/utils';
import { StatusBadge } from '../shared/StatusBadge';
import { PriorityBadge } from '../shared/PriorityBadge';
import { SubtaskList } from './SubtaskList';
import { DependencyList } from './DependencyList';
import { Badge } from '@/shared/components/ui/badge';
import {
  useGetTaskQuery,
  useUpdateTaskMutation,
  useDeleteTaskMutation,
} from '../../api';
import {
  useGetNotesByTaskQuery,
  useCreateNoteMutation,
  useUpdateNoteMutation,
  useDeleteNoteMutation,
} from '../../api';
import { NoteCard } from '../notes/NoteCard';
import { NoteEditorNovel } from '../notes/NoteEditorNovel';
import type { Task, TaskPriority } from '../../types';
import { toast } from 'sonner';

interface TaskDetailProps {
  taskId: number | string | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function TaskDetail({ taskId, open, onOpenChange }: TaskDetailProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [activeTab, setActiveTab] = useState<
    'details' | 'dependencies' | 'notes'
  >('details');
  const [showNoteEditor, setShowNoteEditor] = useState(false);

  // Convert taskId to number for API calls
  const numericTaskId =
    typeof taskId === 'string' ? parseInt(taskId, 10) : taskId;

  const { data: task, isLoading } = useGetTaskQuery(numericTaskId!, {
    skip: !numericTaskId,
  });
  const { data: notes = [] } = useGetNotesByTaskQuery(numericTaskId!, {
    skip: !numericTaskId,
  });

  const [updateTask] = useUpdateTaskMutation();
  const [deleteTask] = useDeleteTaskMutation();
  const [createNote] = useCreateNoteMutation();
  const [updateNote] = useUpdateNoteMutation();
  const [deleteNote] = useDeleteNoteMutation();

  // Keyboard shortcuts for task detail
  useEffect(() => {
    if (!open || !task) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      // Escape to close
      if (e.key === 'Escape' && !isEditing) {
        onOpenChange(false);
      }

      // Cmd/Ctrl + E to toggle edit mode
      if ((e.metaKey || e.ctrlKey) && e.key === 'e') {
        e.preventDefault();
        if (isEditing) {
          handleSave();
        } else {
          handleEdit();
        }
      }

      // Cmd/Ctrl + D to delete (with confirmation)
      if ((e.metaKey || e.ctrlKey) && e.key === 'd') {
        e.preventDefault();
        handleDelete();
      }

      // Ctrl + Tab 1, 2 to switch tabs
      if (e.key === '1' && (e.metaKey || e.ctrlKey) && !isEditing) {
        e.preventDefault();
        setActiveTab('details');
      }
      if (e.key === '2' && (e.metaKey || e.ctrlKey) && !isEditing) {
        e.preventDefault();
        setActiveTab('notes');
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [open, task, isEditing, activeTab]);

  // Edit form state
  const [editForm, setEditForm] = useState<Partial<Task>>({});

  const handleEdit = () => {
    if (task) {
      setEditForm({
        title: task.title,
        description: task.description,
        priority: task.priority,
        estimatedDurationMin: task.estimatedDurationMin,
      });
      setIsEditing(true);
    }
  };

  const handleSave = async () => {
    if (!task) return;

    try {
      await updateTask({
        id: task.id,
        ...editForm,
      }).unwrap();

      toast.success('Task updated successfully!');
      setIsEditing(false);
    } catch (error) {
      toast.error('Failed to update task');
    }
  };

  const handleCancel = () => {
    setEditForm({});
    setIsEditing(false);
  };

  const handleDelete = async () => {
    if (!task) return;

    if (!confirm('Are you sure you want to delete this task?')) return;

    try {
      await deleteTask(task.id).unwrap();
      toast.success('Task deleted');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to delete task');
    }
  };

  const handleCopyLink = () => {
    if (!task) return;

    const url = `${window.location.origin}/ptm/tasks/${task.id}`;
    navigator.clipboard.writeText(url);
    toast.success('Link copied to clipboard');
  };

  const handleCreateNote = async (content: string, isPinned: boolean) => {
    if (!task) return;

    try {
      await createNote({
        taskId: numericTaskId!,
        content,
        isPinned,
      }).unwrap();

      toast.success('Note created');
      setShowNoteEditor(false);
    } catch (error) {
      toast.error('Failed to create note');
    }
  };

  const handleUpdateNote = async (
    noteId: number | string,
    content: string,
    isPinned: boolean
  ) => {
    try {
      const numericNoteId =
        typeof noteId === 'string' ? parseInt(noteId, 10) : noteId;
      await updateNote({
        id: numericNoteId,
        content,
        isPinned,
      }).unwrap();

      toast.success('Note updated');
    } catch (error) {
      toast.error('Failed to update note');
    }
  };

  const handleDeleteNote = async (noteId: number | string) => {
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
      const numericNoteId =
        typeof noteId === 'string' ? parseInt(noteId, 10) : noteId;
      await deleteNote(numericNoteId).unwrap();
      toast.success('Note deleted');
    } catch (error) {
      toast.error('Failed to delete note');
    }
  };

  if (!task && !isLoading) {
    return null;
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className='w-full sm:max-w-2xl overflow-y-auto'>
        {isLoading ? (
          <>
            <SheetTitle className='sr-only'>Loading task...</SheetTitle>
            <div className='space-y-4'>
              <div className='h-8 w-3/4 bg-muted animate-pulse rounded' />
              <div className='h-4 w-1/2 bg-muted animate-pulse rounded' />
            </div>
          </>
        ) : task ? (
          <>
            <SheetHeader>
              <div className='space-y-3'>
                {isEditing ? (
                  <>
                    <SheetTitle className='sr-only'>{task.title}</SheetTitle>
                    <Input
                      value={editForm.title}
                      onChange={(e) =>
                        setEditForm({ ...editForm, title: e.target.value })
                      }
                      className='text-2xl font-bold'
                    />
                  </>
                ) : (
                  <>
                    <div className='flex items-start justify-between'>
                      <SheetTitle className='text-2xl'>{task.title}</SheetTitle>
                      <div className='hidden md:flex items-center gap-2 text-xs text-muted-foreground'>
                        <kbd className='px-1.5 py-0.5 bg-muted rounded border'>
                          Ctrl+E
                        </kbd>
                        <span>Edit</span>
                        <kbd className='px-1.5 py-0.5 bg-muted rounded border ml-2'>
                          Ctrl+1/2
                        </kbd>
                        <span>Tabs</span>
                      </div>
                    </div>
                  </>
                )}

                <div className='flex items-center gap-2'>
                  <StatusBadge status={task.status} />
                  <PriorityBadge priority={task.priority} />
                </div>
              </div>
            </SheetHeader>

            <div className='space-y-6 mt-6'>
              {/* Tabs for Details and Notes */}
              <Tabs
                value={activeTab}
                onValueChange={(v) =>
                  setActiveTab(v as 'details' | 'dependencies' | 'notes')
                }
              >
                <TabsList className='grid w-full grid-cols-3'>
                  <TabsTrigger value='details'>Details</TabsTrigger>
                  <TabsTrigger
                    value='dependencies'
                    className='flex items-center gap-2'
                  >
                    <LinkIcon className='h-4 w-4' />
                    Dependencies
                    {task.isBlocked && (
                      <AlertCircle className='h-3 w-3 text-red-600' />
                    )}
                  </TabsTrigger>
                  <TabsTrigger
                    value='notes'
                    className='flex items-center gap-2'
                  >
                    <StickyNote className='h-4 w-4' />
                    Notes {notes.length > 0 && `(${notes.length})`}
                  </TabsTrigger>
                </TabsList>

                {/* Details Tab */}
                <TabsContent value='details' className='space-y-6 mt-4'>
                  {/* Description */}
                  <div className='space-y-2'>
                    <Label>Description</Label>
                    {isEditing ? (
                      <Textarea
                        value={editForm.description || ''}
                        onChange={(e) =>
                          setEditForm({
                            ...editForm,
                            description: e.target.value,
                          })
                        }
                        rows={4}
                      />
                    ) : (
                      <p className='text-sm text-muted-foreground'>
                        {task.description || 'No description provided'}
                      </p>
                    )}
                  </div>

                  {/* Subtasks Section */}
                  <div className='p-4 bg-muted/30 rounded-lg'>
                    <SubtaskList
                      parentTaskId={task.id}
                      allowNesting={true}
                      showFullDetails={true}
                      onTaskClick={(subtaskId) => {
                        // Open subtask in new detail sheet (future: nested sheets)
                        toast.info(
                          'Click to open subtask details (coming soon)'
                        );
                      }}
                    />
                  </div>

                  <Separator />

                  {/* Details Grid */}
                  <div className='grid grid-cols-2 gap-4'>
                    {/* Duration */}
                    <div className='space-y-1'>
                      <Label className='text-muted-foreground'>Duration</Label>
                      {isEditing ? (
                        <Input
                          type='number'
                          step='15'
                          placeholder='Minutes'
                          value={editForm.estimatedDurationMin || ''}
                          onChange={(e) =>
                            setEditForm({
                              ...editForm,
                              estimatedDurationMin: parseFloat(e.target.value),
                            })
                          }
                        />
                      ) : (
                        <div className='flex items-center gap-2 text-sm'>
                          <Clock className='h-4 w-4' />
                          <span>
                            {task.estimatedDurationMin
                              ? `${Math.floor(task.estimatedDurationMin / 60)}h ${task.estimatedDurationMin % 60}m estimated`
                              : 'Not set'}
                          </span>
                        </div>
                      )}
                    </div>

                    {/* Deadline */}
                    {task.deadlineMs && (
                      <div className='space-y-1'>
                        <Label className='text-muted-foreground'>
                          Deadline
                        </Label>
                        <div className='flex items-center gap-2 text-sm'>
                          <Calendar className='h-4 w-4' />
                          <span>
                            {new Date(task.deadlineMs).toLocaleDateString()}
                          </span>
                        </div>
                      </div>
                    )}

                    {/* Created */}
                    <div className='space-y-1'>
                      <Label className='text-muted-foreground'>Created</Label>
                      <p className='text-sm'>
                        {new Date(task.createdAt).toLocaleDateString()}
                      </p>
                    </div>

                    {/* Updated */}
                    <div className='space-y-1'>
                      <Label className='text-muted-foreground'>Updated</Label>
                      <p className='text-sm'>
                        {new Date(task.updatedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>

                  {/* Tags */}
                  {task.tags && task.tags.length > 0 && (
                    <div className='space-y-2'>
                      <Label className='flex items-center gap-2'>
                        <Tag className='h-4 w-4' />
                        Tags
                      </Label>
                      <div className='flex flex-wrap gap-2'>
                        {task.tags.map((tag: string, index: number) => (
                          <span
                            key={index}
                            className='px-3 py-1 bg-muted rounded-full text-sm'
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </TabsContent>

                {/* Dependencies Tab */}
                <TabsContent value='dependencies' className='mt-4'>
                  <DependencyList taskId={task.id} />
                </TabsContent>

                {/* Notes Tab */}
                <TabsContent value='notes' className='space-y-4 mt-4'>
                  <div className='flex items-center justify-between'>
                    <h3 className='text-lg font-semibold'>Task Notes</h3>
                    <Button
                      size='sm'
                      onClick={() => setShowNoteEditor(!showNoteEditor)}
                      variant={showNoteEditor ? 'outline' : 'default'}
                    >
                      {showNoteEditor ? (
                        <>
                          <X className='h-4 w-4 mr-2' />
                          Cancel
                        </>
                      ) : (
                        <>
                          <Plus className='h-4 w-4 mr-2' />
                          Add Note
                        </>
                      )}
                    </Button>
                  </div>

                  {showNoteEditor && (
                    <NoteEditorNovel
                      onSave={handleCreateNote}
                      onCancel={() => setShowNoteEditor(false)}
                      placeholder='Add notes, ideas, or important information about this task...'
                    />
                  )}

                  {notes.length === 0 ? (
                    <Card className='p-8 text-center border-dashed'>
                      <div className='flex flex-col items-center gap-2 text-muted-foreground'>
                        <StickyNote className='h-12 w-12' />
                        <p className='text-lg font-medium'>No notes yet</p>
                        <p className='text-sm'>
                          Add notes to capture important information about this
                          task
                        </p>
                      </div>
                    </Card>
                  ) : (
                    <div className='space-y-3'>
                      {[...notes]
                        .sort((a, b) => {
                          // Pinned notes first
                          if (a.isPinned && !b.isPinned) return -1;
                          if (!a.isPinned && b.isPinned) return 1;
                          // Then by date (newest first)
                          return (
                            new Date(b.createdAt).getTime() -
                            new Date(a.createdAt).getTime()
                          );
                        })
                        .map((note) => (
                          <NoteCard key={note.id} note={note} />
                        ))}
                    </div>
                  )}
                </TabsContent>
              </Tabs>

              <Separator />

              {/* Actions */}
              <div className='flex items-center justify-between pt-2'>
                {isEditing ? (
                  <div className='flex gap-2 w-full'>
                    <Button
                      variant='outline'
                      onClick={handleCancel}
                      className='flex-1'
                    >
                      <X className='mr-2 h-4 w-4' />
                      Cancel
                    </Button>
                    <Button onClick={handleSave} className='flex-1'>
                      <Save className='mr-2 h-4 w-4' />
                      Save Changes
                    </Button>
                  </div>
                ) : (
                  <>
                    <div className='flex gap-2'>
                      <Button variant='outline' size='sm' onClick={handleEdit}>
                        <Edit className='mr-2 h-4 w-4' />
                        Edit
                      </Button>
                      <Button
                        variant='outline'
                        size='sm'
                        onClick={handleCopyLink}
                      >
                        <Copy className='mr-2 h-4 w-4' />
                        Copy Link
                      </Button>
                    </div>
                    <Button
                      variant='destructive'
                      size='sm'
                      onClick={handleDelete}
                    >
                      <Trash2 className='mr-2 h-4 w-4' />
                      Delete
                    </Button>
                  </>
                )}
              </div>
            </div>
          </>
        ) : null}
      </SheetContent>
    </Sheet>
  );
}
