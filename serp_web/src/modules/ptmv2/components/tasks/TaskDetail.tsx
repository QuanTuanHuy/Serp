/**
 * PTM v2 - Task Detail Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task detail slide-over panel
 */

'use client';

import { useState } from 'react';
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
import {
  useGetTaskQuery,
  useUpdateTaskMutation,
  useDeleteTaskMutation,
} from '../../services/taskApi';
import {
  useGetNotesByTaskQuery,
  useCreateNoteMutation,
  useUpdateNoteMutation,
  useDeleteNoteMutation,
} from '../../services/noteApi';
import { NoteCard } from '../notes/NoteCard';
import { NoteEditorNovel } from '../notes/NoteEditorNovel';
import type { Task, TaskPriority } from '../../types';
import { toast } from 'sonner';

interface TaskDetailProps {
  taskId: string | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function TaskDetail({ taskId, open, onOpenChange }: TaskDetailProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [activeTab, setActiveTab] = useState<'details' | 'notes'>('details');
  const [showNoteEditor, setShowNoteEditor] = useState(false);

  const { data: task, isLoading } = useGetTaskQuery(taskId!, {
    skip: !taskId,
  });
  const { data: notes = [] } = useGetNotesByTaskQuery(taskId!, {
    skip: !taskId,
  });

  const [updateTask] = useUpdateTaskMutation();
  const [deleteTask] = useDeleteTaskMutation();
  const [createNote] = useCreateNoteMutation();
  const [updateNote] = useUpdateNoteMutation();
  const [deleteNote] = useDeleteNoteMutation();

  // Edit form state
  const [editForm, setEditForm] = useState<Partial<Task>>({});

  const handleEdit = () => {
    if (task) {
      setEditForm({
        title: task.title,
        description: task.description,
        priority: task.priority,
        estimatedDurationHours: task.estimatedDurationHours,
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

  const handleProgressChange = async (value: number[]) => {
    if (!task) return;

    try {
      await updateTask({
        id: task.id,
        progressPercentage: value[0],
      }).unwrap();
    } catch (error) {
      toast.error('Failed to update progress');
    }
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

    const url = `${window.location.origin}/ptmv2/tasks/${task.id}`;
    navigator.clipboard.writeText(url);
    toast.success('Link copied to clipboard');
  };

  const handleCreateNote = async (content: string, isPinned: boolean) => {
    if (!task) return;

    try {
      await createNote({
        taskId: task.id,
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
    noteId: string,
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
    }
  };

  const handleDeleteNote = async (noteId: string) => {
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
      await deleteNote(noteId).unwrap();
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
                  <SheetTitle className='text-2xl'>{task.title}</SheetTitle>
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
                onValueChange={(v) => setActiveTab(v as 'details' | 'notes')}
              >
                <TabsList className='grid w-full grid-cols-2'>
                  <TabsTrigger value='details'>Details</TabsTrigger>
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

                  {/* Progress */}
                  <div className='space-y-3'>
                    <div className='flex items-center justify-between'>
                      <Label>Progress</Label>
                      <span className='text-sm font-medium'>
                        {task.progressPercentage}%
                      </span>
                    </div>
                    <Slider
                      value={[task.progressPercentage]}
                      max={100}
                      step={10}
                      onValueChange={handleProgressChange}
                      className='cursor-pointer'
                      disabled={isEditing}
                    />
                    <Progress value={task.progressPercentage} className='h-2' />
                  </div>

                  {/* Subtasks Section */}
                  <div className='p-4 bg-muted/30 rounded-lg'>
                    <SubtaskList parentTaskId={task.id} />
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
                          step='0.5'
                          value={editForm.estimatedDurationHours || ''}
                          onChange={(e) =>
                            setEditForm({
                              ...editForm,
                              estimatedDurationHours: parseFloat(
                                e.target.value
                              ),
                            })
                          }
                        />
                      ) : (
                        <div className='flex items-center gap-2 text-sm'>
                          <Clock className='h-4 w-4' />
                          <span>{task.estimatedDurationHours}h estimated</span>
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

                  {/* Schedule Info (if available) */}
                  {task.deadlineMs && (
                    <Card className='bg-blue-50 dark:bg-blue-950/20 border-blue-200 dark:border-blue-800'>
                      <CardContent className='pt-4'>
                        <div className='flex items-start gap-3'>
                          <Calendar className='h-5 w-5 text-blue-600 flex-shrink-0' />
                          <div>
                            <p className='font-medium text-blue-900 dark:text-blue-100'>
                              Scheduled Task
                            </p>
                            <p className='text-sm text-blue-700 dark:text-blue-300 mt-1'>
                              Deadline:{' '}
                              {new Date(task.deadlineMs).toLocaleString()}
                            </p>
                            {task.isDeepWork && (
                              <p className='text-xs text-blue-600 dark:text-blue-400 mt-1'>
                                🎯 Deep work task - requires focus time
                              </p>
                            )}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  )}
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
